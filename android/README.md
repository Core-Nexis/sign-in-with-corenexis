# Android: one-tap Sign in with Corenexis

When the Corenexis app is installed, your app opens it, the person sees your app's name
and what it receives, confirms with their fingerprint, and your app gets an authorization
code back. No browser. Without the Corenexis app, use the browser flow (Custom Tab) from the
[documentation](https://corenexis.com/oauth/docs#native).

## 1. Register your Android app

In the [developer console](https://accounts.corenexis.com/developer), open your app >
**Android apps** and add one line per signing certificate:

```
com.example.app AB:CD:EF:...:12   (Google Play app signing key, SHA-256)
com.example.app 12:34:56:...:EF   (upload key, for test builds)
```

- Play Console > Test and release > App integrity > App signing: copy the **SHA-256** of the
  app signing key.
- Upload key: `keytool -list -v -keystore upload.jks -alias <alias>` and copy `SHA256`.

## 2. Get your app verified

One-tap sign-in works only for verified apps (console > Verification). Until then the
Corenexis app answers `unauthorized_client` and your app should fall back to the browser.

## 3. Let your app see the Corenexis app (Android 11+)

```xml
<queries>
  <package android:name="com.corenexis.app" />
</queries>
```

## 4. Sign in

Copy [CorenexisSignIn.kt](CorenexisSignIn.kt) into your app, then:

```kotlin
private var pending: CorenexisSignIn.Request? = null

private val corenexis = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
    val request = pending ?: return@registerForActivityResult
    pending = null
    when (val result = CorenexisSignIn.parse(r.resultCode, r.data, request)) {
        is CorenexisSignIn.Result.Code -> sendToYourServer(result.code, result.verifier, result.redirectUri)
        CorenexisSignIn.Result.Cancelled -> Unit
        is CorenexisSignIn.Result.Fallback -> openBrowserSignIn()
    }
}

fun signIn() {
    if (!CorenexisSignIn.isAvailable(this)) return openBrowserSignIn()
    val request = CorenexisSignIn.newRequest().also { pending = it }
    corenexis.launch(CorenexisSignIn.intent(CLIENT_ID, request))
}
```

## 5. Exchange on your server

Your server posts the code, the verifier and `redirect_uri=android:<your package>` to
`https://accounts.corenexis.com/api/token` with the client secret. See
[../server/README.md](../server/README.md).

## Result

| Extra | When |
|---|---|
| `code`, `state`, `redirect_uri` | `RESULT_OK` |
| `error`: `access_denied` | the person closed the screen |
| `error`: `no_account` | no Corenexis account on the phone |
| `error`: `unauthorized_client` | package or certificate not registered, or app not verified |
| `error`: `invalid_request` | missing `client_id` or PKCE |

## How this stays safe

- The Corenexis app learns which app called it from Android itself and reads that app's
  signing certificate from the system; both must match your registration.
- The code goes back only to your app, is bound to your PKCE challenge and lives one minute.
- The exchange needs your client secret, on your server only.
- Your app checks the Corenexis app's signature before opening it, and compares `state`.
