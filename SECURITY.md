# Security Policy

## Reporting a vulnerability

Please **do not** open a public issue for security vulnerabilities.

Instead, report privately via GitHub's
[**Report a vulnerability**](https://github.com/aliammari1/readrealm/security/advisories/new)
(Security → Advisories), or email **ammari.ali.0001@gmail.com**.

We aim to acknowledge reports within a few days and will keep you updated on the
fix. Please include steps to reproduce and the affected app (`apps/api`,
`apps/android`, `apps/ios`, or `apps/dashboard`) where possible.

## Supported versions

ReadRealm is pre-1.0; security fixes target the `main` branch.

## Automated scanning

CI runs **CodeQL** (TypeScript / Kotlin / Swift), **Trivy** (filesystem,
SHA-pinned), and **gitleaks** (secret scanning) on every push and pull request.

## Known security notes

Technical, tracked security items (CORS, JWT handling, the `rt-client`
raw-tarball dependency, AI-key handling) are documented in
[`shared/docs/security.md`](shared/docs/security.md).
