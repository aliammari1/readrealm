# Security notes

Tracked security items for the ReadRealm API. CI runs CodeQL (ts/kotlin/swift),
Trivy (filesystem, SHA-pinned) and gitleaks; this file records the known issues
those scans don't fully cover.

## Open items

### 1. CORS is wide-open (`origin: '*'`)
Both the HTTP app (`src/main.ts`) and the Socket.IO gateway
(`src/chat/chat.gateway.ts`) allow any origin. Acceptable for development and for
native clients (which aren't subject to CORS), but **restrict to the deployed web
origins before production** if a browser client is added. Marked with `NOTE
(security)` comments in both files.

### 2. JWT handling
- `JWT_SECRET` must be a strong, secret value (min 32 chars) supplied via env —
  never commit it. `gitleaks` guards against accidental commits.
- The `AuthGaurd` verifies the bearer token on protected routes. Audit token
  expiry/refresh handling (`auth.service.ts`) when hardening.

### 3. `rt-client` raw-tarball dependency
`apps/api/package.json` pulls `rt-client` from a GitHub-release `.tgz`, not a
registry. This bypasses registry integrity/provenance and can't be version-pinned
by Renovate. It's disabled in `renovate.json` and the Trivy job notes it. Replace
with a registry-published package when Azure ships one.

### 4. AI keys
`ANTHROPIC_API_KEY`, `GEMINI_API_KEY`, `AZURE_API_*`, `OPENAI`/`HF` keys are
read from env only. The AI book-chat service degrades gracefully when the
Anthropic key is absent (no crash, no key leakage in errors).

## Review hooks

Run `claude-code-action` review + `claude-code-security-review` on PRs touching
auth, the chat gateway, or dependency manifests.
