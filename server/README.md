# Exchanging the code on your server

Your app (web or Android) sends the authorization `code` and the PKCE `code_verifier` to
**your** server. Your server exchanges them with Corenexis, using the client secret that
never leaves it.

```
POST https://accounts.corenexis.com/api/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&code=<code>
&code_verifier=<verifier>
&redirect_uri=<the redirect the code was issued for>
&client_id=<client id>
&client_secret=<client secret>
```

`redirect_uri` is your registered callback URL for the web flow, and
`android:<your package>` for one-tap sign-in through the Corenexis app.

The response holds an `access_token` (a short-lived JWT), a `refresh_token` (rotates on
every use) and the `user` with the fields your app was granted. Refresh with
`grant_type=refresh_token`. Details: https://corenexis.com/oauth/docs#token

A minimal Node.js example is in [node/exchange.js](node/exchange.js).
