# ReadRealm API

NestJS backend for the ReadRealm Android/iOS readers and Flutter administration dashboard.

## Stack

- NestJS 10 / TypeScript
- MongoDB + Mongoose
- JWT authentication
- Socket.IO chat
- Open Library / Gutendex book discovery
- ElevenLabs enhanced narration and optional voice-agent sessions
- Ollama for optional self-hosted summaries
- Nodemailer / SMTP for verification mail

The API no longer requires Azure Cognitive Services, Azure OpenAI realtime, OpenAI SDK, Google Generative AI, or Hugging Face inference packages.

## Setup

```bash
corepack enable
pnpm install
cp .env.example .env
pnpm start:dev
```

Required:

```env
MONGODB_URL=mongodb://localhost:27017/readrealm
JWT_SECRET=replace_with_a_random_secret_at_least_32_characters
```

Optional enhanced narration:

```env
ELEVENLABS_API_KEY=
ELEVENLABS_VOICE_ID=
ELEVENLABS_MODEL_ID=eleven_multilingual_v2
ELEVENLABS_AGENT_ID=
```

Optional local summaries:

```env
OLLAMA_URL=http://localhost:11434
OLLAMA_MODEL=qwen3:8b
```

The ElevenLabs API key stays on the API server. Mobile/web clients should request a short-lived signed voice-agent URL from ReadRealm rather than embedding the permanent key.

## Useful endpoints

### Health

```text
GET /health
GET /health/live
```

### Authentication

```text
POST /auth/register
POST /auth/login
PUT  /auth/change-password
POST /auth/forgot-password
```

Verification endpoints are also provided by the auth/verification modules.

### Books

```text
GET    /book
GET    /book/search?q=...
GET    /book/genre/:genre?offset=0&limit=10
GET    /book/details/:id
GET    /book/summary/:title
GET    /book/tts/stream/:title
POST   /book/ebook
GET    /book/:id
PATCH  /book/:id
DELETE /book/:id
```

### Bookmarks

```text
PUT /book/bookmark
PUT /book/:bookId/bookmark
GET /book/bookmarks/:userId
```

The first route accepts the full book payload and is used by the mobile client. The compatibility route accepts `{ "userId": "..." }` for books already stored by the API.

### Reviews

```text
POST   /book/reviews/:bookId
GET    /book/reviews/:bookId
GET    /book/user-reviews/:userId
DELETE /book/:bookId/reviews/:reviewId
```

Review deletion verifies that the requesting `userId` owns the review.

### Voice agent

```text
GET /speech-realtime/signed-url
```

This endpoint requires JWT authentication and returns an ElevenLabs signed URL. The client then connects directly to the signed ElevenLabs WebSocket session. The legacy Socket.IO `/speech` namespace also emits the signed URL during migration.

## Narration

Book narration is proxied through the API:

```text
reader → ReadRealm API → ElevenLabs
```

Long text is normalized and split into bounded chunks before being streamed back as MPEG audio. Native Android/iOS TTS can remain the offline fallback.

## Summaries

Summaries use a locally reachable Ollama server:

```text
ReadRealm API → Ollama → qwen3:8b
```

No vendor AI API key is required. The current implementation samples beginning/middle/end sections for very long texts, creates section summaries, then reduces them into a short overview.

## Development

```bash
pnpm build
pnpm test
pnpm test:e2e
pnpm lint
```

## Docker

The API Dockerfile uses pnpm and the committed `pnpm-lock.yaml`. The container exposes port 3000 and checks `GET /health`.

From the repository root:

```bash
docker compose up --build
```

## Security notes

- Keep `JWT_SECRET`, SMTP credentials, and ElevenLabs credentials server-side.
- Configure `CORS_ORIGIN` for production instead of using a wildcard.
- Request bodies are bounded with `MAX_JSON_BODY_SIZE`.
- DTO transformation/whitelisting is enabled globally while remaining compatible with existing clients.
- The realtime voice endpoint returns a temporary signed URL instead of exposing the permanent ElevenLabs API key.

## Follow-up work

The API still needs stronger authorization on administrative/user-management endpoints, refresh-token/session rotation, rate limiting, chat socket JWT authentication, moderation/reporting APIs, account deletion, and full OpenAPI contract generation.
