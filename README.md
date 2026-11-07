# Sign in with Corenexis

Secure sign-in for your website and your Android app, with one Corenexis account.
OAuth 2.0 authorization code flow with PKCE, a consent screen your users can trust,
and **one-tap sign-in through the Corenexis app on Android** (no browser, the
person's fingerprint, like signing in with Google).

- Full documentation: https://corenexis.com/oauth/docs
- Developer console (create your app): https://accounts.corenexis.com/developer
- The Corenexis app on Google Play: https://play.google.com/store/apps/details?id=com.corenexis.app

## Why Sign in with Corenexis

- **Secure by default.** PKCE (S256), `state`, exact redirect matching and a server-side
  client secret are required, not optional.
- **Phone approval.** People who use the Corenexis app approve new sign-ins with their
  fingerprint, and get an alert for every new sign-in.
- **Two-step verification and passkeys** on the account, without any work on your side.
- **Clear consent.** Your users see your app's name, logo and exactly what it receives
  (name, email, account id; optional country, timezone, picture).
- **One-tap on Android.** With the Corenexis app installed, your Android app signs people
  in without opening a browser.
- **Free for developers.** Self-service from the developer console.

## How it works

```
 Your app                 Corenexis                      Your server
    |  1. redirect / intent   |                                |
    |------------------------>|  sign-in, consent              |
    |  2. code (+ state)      |                                |
    |<------------------------|                                |
    |  3. code + code_verifier ------------------------------->|
    |                         |  4. POST /api/token            |
    |                         |<-------------------------------|  (client secret)
    |                         |  tokens + user                 |
    |                         |------------------------------->|
```

The code alone is useless: it is bound to your PKCE verifier, lives one minute, and the
exchange needs your client secret, which only your server holds.

## Quick start

1. Create an app in the [developer console](https://accounts.corenexis.com/developer):
   name, logo, website, privacy policy URL and your redirect URLs. You get a `client_id`
   and a `client_secret` (shown once; keep it on your server).
2. Pick your platform:
   - **Website:** [web/README.md](web/README.md)
   - **Android app (one-tap with the Corenexis app):** [android/README.md](android/README.md)
3. Exchange the code on your server: [server/README.md](server/README.md)
4. Get your app verified (console > Verification) to show the verified badge, release the
   profile picture and switch on one-tap sign-in on Android.

## Endpoints

| What | URL |
|---|---|
| Sign-in (authorization) | `https://accounts.corenexis.com/?redirect=...&state=...&code_challenge=...&code_challenge_method=S256` |
| Token (code exchange, refresh) | `POST https://accounts.corenexis.com/api/token` |
| User info | `GET https://accounts.corenexis.com/api/userinfo` (Bearer access token) |
| OpenID configuration | `https://accounts.corenexis.com/.well-known/openid-configuration` |
| Signing keys (JWKS) | `https://accounts.corenexis.com/.well-known/jwks.json` |

## Security rules (summary)

- Never put the client secret in a browser, a mobile app or a public repository.
- Always send `state` and PKCE `code_challenge` (S256), and check `state` on return.
- Register every redirect URL exactly; matching is byte for byte.
- On Android, check the Corenexis app's signature before you open it (see the helper).
- Treat a refresh token that stops working as "sign in again", not as an error to retry.

More in [SECURITY.md](SECURITY.md) and the [documentation](https://corenexis.com/oauth/docs#security).

## Repository layout

| Path | What it is |
|---|---|
| `android/CorenexisSignIn.kt` | Drop-in helper: Corenexis app check, intent, result parsing |
| `android/README.md` | Android guide: registration, manifest, usage, fallback |
| `server/node/exchange.js` | Minimal Node.js (Express) code exchange |
| `web/README.md` | Website flow with PKCE |

## Support

- Documentation: https://corenexis.com/oauth/docs
- Help: https://helpdesk.corenexis.com

## License

MIT. See [LICENSE](LICENSE).
