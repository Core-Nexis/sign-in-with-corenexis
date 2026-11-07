// Minimal code exchange for Sign in with Corenexis (Node.js 18+, Express).
// MIT License. https://github.com/Core-Nexis/sign-in-with-corenexis
//
//   CRX_CLIENT_ID=...  CRX_CLIENT_SECRET=...  node exchange.js
//
// Your app posts { code, code_verifier, redirect_uri } here. Create your own
// session from the returned user; never send the Corenexis refresh token to a browser.
import express from 'express';

const BASE = 'https://accounts.corenexis.com';
const CLIENT_ID = process.env.CRX_CLIENT_ID;
const CLIENT_SECRET = process.env.CRX_CLIENT_SECRET;
// The redirect values your app may use: your web callback and android:<your package>.
const ALLOWED_REDIRECTS = new Set(['https://yourapp.com/auth/callback', 'android:com.example.app']);

const app = express();
app.use(express.json());

app.post('/auth/corenexis', async (req, res) => {
  const { code, code_verifier: verifier, redirect_uri: redirectUri } = req.body ?? {};
  if (!/^[a-f0-9]{64}$/.test(String(code)) || !/^[A-Za-z0-9\-._~]{43,128}$/.test(String(verifier))
      || !ALLOWED_REDIRECTS.has(String(redirectUri))) {
    return res.status(400).json({ error: 'bad_request' });
  }
  const r = await fetch(`${BASE}/api/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'authorization_code', code, code_verifier: verifier, redirect_uri: redirectUri,
      client_id: CLIENT_ID, client_secret: CLIENT_SECRET,
    }),
    signal: AbortSignal.timeout(10_000),
  });
  const data = await r.json().catch(() => ({}));
  if (!r.ok || !data.user?.id) return res.status(401).json({ error: data.error ?? 'exchange_failed' });

  // data.user: { id, email, email_verified, name, ... } as granted on the consent screen.
  // Store data.refresh_token server-side if you need to call Corenexis later.
  const session = await createYourOwnSession(data.user);
  return res.json({ session });
});

async function createYourOwnSession(user) {
  return { userId: user.id };
}

app.listen(3000);
