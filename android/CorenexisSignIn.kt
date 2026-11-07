// Sign in with Corenexis for Android: one-tap sign-in through the Corenexis app.
// MIT License. https://github.com/Core-Nexis/sign-in-with-corenexis
//
// Copy this file into your app and change the package line. It has no dependencies
// beyond the Android SDK. Requires minSdk 28 for the signature check; on older
// phones isAvailable() returns false and you use the browser flow.

package com.example.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object CorenexisSignIn {
    private const val PACKAGE = "com.corenexis.app"
    private const val ACTION = "com.corenexis.signin.AUTHORIZE"

    // SHA-256 of the certificate Google Play signs the Corenexis app with.
    // Checking it means a look-alike app can never receive your sign-in request.
    private const val CORENEXIS_CERT =
        "D0:A0:31:5D:26:4D:98:C0:C3:12:CE:EA:B7:7A:8F:0D:E3:05:E8:D4:C9:6F:54:4F:4C:E8:1E:02:5F:63:45:75"

    /** A sign-in in progress: keep it until the result comes back. */
    data class Request(val verifier: String, val challenge: String, val state: String)

    sealed class Result {
        /** Send [code], [verifier] and [redirectUri] to your server to exchange. */
        data class Code(val code: String, val verifier: String, val redirectUri: String) : Result()
        /** The person closed the Corenexis screen. Stay where you are. */
        object Cancelled : Result()
        /** Not set up, no account on the phone, or not verified: use the browser flow. */
        data class Fallback(val reason: String) : Result()
    }

    /** True only when the genuine Corenexis app is installed and can sign people in. */
    fun isAvailable(context: Context): Boolean = runCatching {
        if (Build.VERSION.SDK_INT < 28) return false
        val pm = context.packageManager
        val info = pm.getPackageInfo(PACKAGE, PackageManager.GET_SIGNING_CERTIFICATES)
        val signing = info.signingInfo ?: return false
        if (signing.hasMultipleSigners()) return false
        val cert = signing.signingCertificateHistory.last().toByteArray()
        val sha = MessageDigest.getInstance("SHA-256").digest(cert).joinToString(":") { "%02X".format(it) }
        sha == CORENEXIS_CERT && pm.resolveActivity(Intent(ACTION).setPackage(PACKAGE), 0) != null
    }.getOrDefault(false)

    /** A fresh PKCE verifier, its S256 challenge and a state value. */
    fun newRequest(): Request {
        val verifier = random(32)
        val challenge = b64(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII)))
        return Request(verifier, challenge, random(16))
    }

    /** The intent to launch with an ActivityResultLauncher (StartActivityForResult). */
    fun intent(clientId: String, request: Request): Intent =
        Intent(ACTION).setPackage(PACKAGE)
            .putExtra("client_id", clientId)
            .putExtra("code_challenge", request.challenge)
            .putExtra("code_challenge_method", "S256")
            .putExtra("state", request.state)

    /** Turn the activity result into what to do next. */
    fun parse(resultCode: Int, data: Intent?, request: Request): Result {
        val code = data?.getStringExtra("code")
        if (resultCode == Activity.RESULT_OK && code != null) {
            // The answer must belong to the request you made.
            if (data.getStringExtra("state") != request.state) return Result.Fallback("state_mismatch")
            return Result.Code(code, request.verifier, data.getStringExtra("redirect_uri").orEmpty())
        }
        return when (val error = data?.getStringExtra("error")) {
            null, "access_denied" -> Result.Cancelled
            else -> Result.Fallback(error)
        }
    }

    private fun random(bytes: Int): String = ByteArray(bytes).also { SecureRandom().nextBytes(it) }.let(::b64)
    private fun b64(b: ByteArray): String = Base64.encodeToString(b, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}
