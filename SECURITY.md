# Security

## Reporting a vulnerability

Please report security issues privately by opening a ticket at https://helpdesk.corenexis.com.
Do not open a public issue.

## Integration rules

- Keep the client secret on your server only. Rotate it from the developer console if it
  may have leaked.
- Always use PKCE (S256) and `state`, and reject a callback or result whose `state` does not
  match the one you stored.
- Register exact redirect URLs. On Android, register your package with the SHA-256 of every
  certificate that signs it.
- On Android, check the Corenexis app's signing certificate before opening it (the helper in
  `android/CorenexisSignIn.kt` does this).
- Exchange codes promptly: they are single use and expire after one minute (Android) or
  shortly after issue (web). A code presented twice revokes every token issued from it.
