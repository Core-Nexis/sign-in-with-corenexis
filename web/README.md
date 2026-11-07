# Website: Sign in with Corenexis

1. On your server, make a fresh PKCE `code_verifier`, its S256 `code_challenge` and a `state`,
   and keep the verifier and state in the visitor's session.
2. Redirect the browser:

```
https://accounts.corenexis.com/?redirect=<your callback, URL-encoded>
  &state=<state>
  &code_challenge=<challenge>
  &code_challenge_method=S256
```

Optional `&provider=google|github|microsoft|facebook` opens that provider directly.

3. Corenexis signs the person in (password, passkey, magic link, Google, GitHub, Microsoft,
   Facebook, two-step, or approval on their phone), shows the consent screen the first time,
   and redirects to:

```
https://yourapp.com/auth/callback?code=<code>&state=<state>&iss=https%3A%2F%2Faccounts.corenexis.com
```

4. Check that `state` and `iss` match, then exchange the code on your server
   ([../server/README.md](../server/README.md)) with the same `redirect_uri`.

PKCE in Node.js:

```js
import crypto from 'node:crypto';
const verifier = crypto.randomBytes(32).toString('base64url');
const challenge = crypto.createHash('sha256').update(verifier).digest('base64url');
const state = crypto.randomBytes(16).toString('base64url');
```

Full guide: https://corenexis.com/oauth/docs
