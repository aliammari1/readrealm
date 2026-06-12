# ReadRealm — API (NestJS)

[![NestJS](https://img.shields.io/badge/NestJS-10-E0234E?style=flat-square&logo=nestjs&logoColor=white)](https://nestjs.com/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6-3178C6?style=flat-square&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![pnpm](https://img.shields.io/badge/pnpm-managed-F69220?style=flat-square&logo=pnpm&logoColor=white)](https://pnpm.io/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Mongoose-47A248?style=flat-square&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Socket.IO](https://img.shields.io/badge/Socket.IO-realtime-010101?style=flat-square&logo=socketdotio&logoColor=white)](https://socket.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](../../LICENSE)

The backend for **[ReadRealm](../../README.md)** — the single source of truth that
serves all three clients (Android / iOS / Flutter). It exposes a REST API + a
Socket.IO realtime layer, persists to MongoDB via Mongoose, and unifies five AI
providers behind config.

> This is one app in the ReadRealm monorepo. See the root README for the full
> architecture and the [engineering decisions](../../README.md#-engineering-decisions).

## What's actually here

A standard NestJS application managed with **pnpm** (committed `pnpm-lock.yaml`,
`packageManager` pinned). The real module layout under `src/`:

| Module | Responsibility |
|--------|----------------|
| `auth/` | JWT auth, sign-up/login, email verification, password reset (Passport, bcrypt) |
| `book/` | Catalog, EPUB parsing, reviews, bookmarks, TTS (audiobook) endpoints |
| `chat/` | Socket.IO gateway + the **AI book-chat participant** (`chat-ai.service.ts`) |
| `user/` | Profiles & preferences |
| `verification/` | OTP / email verification workflows (NodeMailer) |
| `speech-realtime/` | Realtime voice (Azure Cognitive Services, FFmpeg via `node-av`) |
| `guards/` | Auth guards & middleware |
| `logger/` | Structured logging |
| `config/` | Typed configuration |

Supporting pieces:

- `scripts/generate-openapi.ts` — emits the shared OpenAPI spec from the live app.
- `cloudflare/` — Workers + Durable Object deployment design (see below; not deployed).
- `test/` — Jest e2e (supertest + `mongodb-memory-server`).

## AI providers

AI is **multi-provider, unified behind config** — none is load-bearing on its own:

- **Anthropic** (`@anthropic-ai/sdk`) — the in-chat **AI book-chat participant**
  (streaming Claude `claude-haiku-4-5` + tool use), `@`-mentionable in a room.
- **Google Generative AI** — summaries & content analysis.
- **OpenAI** — content generation.
- **HuggingFace** — classification / sentiment.
- **Azure Cognitive Services** — speech-to-text, text-to-speech, translation.

## Getting started

### Prerequisites

- **Node.js 20+**
- **pnpm 10+** (`corepack enable` or `npm i -g pnpm`)
- **MongoDB 5+** (local, Docker, or Atlas)

### Install & run

```bash
# from the repo root
pnpm --filter @readrealm/api install     # or: cd apps/api && pnpm install

cp apps/api/.env.example apps/api/.env    # then fill in your keys

pnpm --filter @readrealm/api start:dev    # http://localhost:3000
```

Only a `MONGODB_URL` and `JWT_SECRET` are required to boot; the AI/mail/Azure
keys are optional and the related features no-op without them. See
[`.env.example`](.env.example) for the full list.

### Common scripts

```bash
pnpm run start:dev        # watch-mode dev server
pnpm run build            # nest build -> dist/
pnpm run test             # unit tests (jest)
pnpm run test:e2e         # e2e (supertest + mongodb-memory-server)
pnpm run test:cov         # coverage
pnpm run lint:check       # eslint
pnpm run format:check     # prettier
pnpm run generate:openapi # regenerate shared/api-spec/openapi.yaml
```

## API docs (OpenAPI)

The spec is **generated, not hand-written**. `@nestjs/swagger` decorators on the
controllers feed `scripts/generate-openapi.ts`, which writes
[`shared/api-spec/openapi.yaml`](../../shared/api-spec/openapi.yaml). CI fails if
that file drifts from the code.

- **Swagger UI** — `http://localhost:3000/api/docs` when the server is running.
- **Hosted playground** — the [Mintlify docs](../../docs/docs.json) render the
  same spec as an interactive API reference.

Regenerate after changing any controller / DTO:

```bash
pnpm --filter @readrealm/api run generate:openapi
```

## Realtime chat

Chat is a NestJS **Socket.IO gateway** (`chat/chat.gateway.ts`) with rooms keyed
per book. The AI participant joins as a member and streams responses. For the
edge story, an additive Cloudflare **Durable Object** port lives in
[`cloudflare/`](cloudflare/README.md).

## Cloudflare (config + scaffold — not deployed)

[`cloudflare/`](cloudflare/) holds the edge deployment design:

- **Workers** (`nodejs_compat`) for the HTTP API — the NestJS→workerd HTTP bridge
  is a clearly-scoped `NEEDS-PORT` (good-first-issue), not a half-working stub.
- **Durable Object** (`chat-room.do.ts`) for realtime — **implemented** with the
  WebSocket Hibernation API.
- **MongoDB Atlas** kept as the datastore (no SQL rewrite).

Nothing is deployed; there is no `wrangler deploy` in CI. Full rationale in
[`cloudflare/README.md`](cloudflare/README.md).

## Docker

A `Dockerfile` is provided and the repo-root `docker-compose.yaml` brings up the
API + MongoDB together:

```bash
# from the repo root
docker compose up
```

## Testing

```bash
pnpm run test       # unit
pnpm run test:e2e   # e2e against an in-memory MongoDB
pnpm run test:cov   # coverage (uploaded to Codecov under flag "api" in CI)
```

## A note on `rt-client`

The Azure realtime-audio SDK (`rt-client`) is pulled from a GitHub-release
`.tgz`, not a registry — a supply-chain surface Renovate/Trivy can't pin. It is
flagged in CI and disabled in Renovate; revisit when Azure publishes to npm.

## License

MIT — see the [root LICENSE](../../LICENSE).
