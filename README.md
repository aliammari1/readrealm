# 📚 ReadRealm

### Where Books Become Worlds

ReadRealm is a cross-platform reading project with native Android and iOS readers, a Flutter admin dashboard, and a NestJS/MongoDB backend. Its visual identity is an enchanted-library palette built around deep brown, mahogany, parchment, and gilded gold.

## What is in the repository

- **Android** — Kotlin + Jetpack Compose reader with EPUB reading, bookmarks, reviews, narration, book chat, and the immersive Experience/Realm UI.
- **iOS** — SwiftUI reader with EPUB reading, narration, book discovery, and reader preferences.
- **Dashboard** — Flutter administration interface using the same ReadRealm color language.
- **API** — NestJS + MongoDB backend for authentication, books, reviews, bookmarks, chat, verification, narration, and voice-session handoff.

## Architecture

```text
Android / iOS / Flutter Admin
            │
      REST + Socket.IO
            │
       ReadRealm API
       NestJS + MongoDB
        │           │
        │           └── Ollama (optional, self-hosted summaries)
        │
        ├── Open Library / Gutendex (book discovery / public-domain metadata)
        └── ElevenLabs (optional enhanced narration / voice agent)
```

### Provider policy

ReadRealm keeps permanent provider credentials on the backend only.

- **ElevenLabs** is the optional cloud provider for enhanced narration and voice-agent sessions.
- **Ollama** is used for local/self-hosted book summaries, so summaries do not require an OpenAI, Gemini, or Hugging Face API key.
- **Stream** may continue to be used by client chat/video features where its free tier is suitable.
- Native Android/iOS speech remains useful as an offline fallback.

Azure Cognitive Services, Azure OpenAI realtime dependencies, Google Generative AI, Hugging Face inference, and OpenAI SDK dependencies are not required by the API.

## Core features

### 📖 Reading
- EPUB reading on Android and iOS
- Reader preferences and local reading progress
- Bookmarks and reviews
- Search and genre discovery through the backend
- Public-domain book discovery through Gutendex / Project Gutenberg metadata

### 🎧 Listen
- Native device narration in the mobile readers
- Optional enhanced ElevenLabs narration through the ReadRealm backend
- Long text is split into bounded chunks before narration

### 💬 Connect
- Socket.IO book-chat foundation
- Stream-based client features can remain where appropriate
- Reviews and shared book conversations

### ✨ Explore
- Android immersive Experience / Realm components
- Optional self-hosted Ollama summaries
- Foundation for spoiler-aware book Q&A, character knowledge, timelines, and Realm Mode

## Visual identity

The canonical ReadRealm palette currently comes from Android and is mirrored into the Flutter dashboard:

| Token | Hex |
|---|---|
| Deep Library Brown | `#1A0F0A` |
| Rich Mahogany | `#4A2C2A` |
| Warm Leather | `#8B5A2B` |
| Gilded Gold | `#D4AF37` |
| Ancient Parchment | `#F5E6C8` |
| Candlelight Glow | `#FFE4B5` |
| Mystic Purple | `#2D1B4E` |

The long-term goal is to keep platform-native interaction patterns while sharing these design tokens across Android, iOS, and the dashboard.

## Quick start

### Requirements

- Node.js 20+
- pnpm through Corepack
- MongoDB
- Android Studio / Android SDK for Android
- Xcode for iOS
- Flutter for the dashboard
- Optional: Ollama for local summaries
- Optional: ElevenLabs account for enhanced cloud narration

### API

```bash
cd apps/api
corepack enable
pnpm install
cp .env.example .env
pnpm start:dev
```

Minimum local environment:

```env
MONGODB_URL=mongodb://localhost:27017/readrealm
JWT_SECRET=replace_with_a_random_secret_at_least_32_characters

# Optional enhanced narration
ELEVENLABS_API_KEY=
ELEVENLABS_VOICE_ID=
ELEVENLABS_MODEL_ID=eleven_multilingual_v2
ELEVENLABS_AGENT_ID=

# Optional local summaries
OLLAMA_URL=http://localhost:11434
OLLAMA_MODEL=qwen3:8b
```

For Ollama summaries:

```bash
ollama pull qwen3:8b
ollama serve
```

Health endpoints:

```text
GET /health
GET /health/live
```

### Dashboard

```bash
cd apps/dashboard
flutter pub get
flutter run --dart-define=API_BASE_URL=http://localhost:3000
```

### Docker

```bash
docker compose up --build
```

When the API runs in Docker and Ollama runs on the host, Compose uses `host.docker.internal:11434` by default.

See [QUICKSTART.md](QUICKSTART.md) for the repository-level workflow.

## Current stabilization status

The current foundation work aligns several previously inconsistent client/backend contracts:

- genre endpoints return normal JSON instead of pseudo-streaming SSE
- iOS search uses `/book/search?q=...`
- Android review deletion supplies its required path parameters
- dashboard bookmarks use a backend-supported route
- Nest registration accepts the normal 201 response
- API health checks match Docker health checks
- the API container uses the repository's pnpm lockfile
- Azure face-recognition placeholders are removed from the dashboard
- API URLs can be configured for the dashboard at build time

## Next product milestones

The next substantial ReadRealm work should build on this foundation:

1. Server-synced reading position, highlights, notes, and reader preferences.
2. User EPUB/PDF import with explicit rights/source metadata.
3. Spoiler-aware “Ask This Book” and “Story So Far”.
4. Text ↔ narration position synchronization.
5. Chapter-locked book-club discussions and moderation tools.
6. Reading goals, streaks, statistics, and collections.
7. Realm Mode: characters, places, relationships, timeline, items, and journal.
8. Shared design tokens for iOS and the remaining dashboard surfaces.
9. OpenAPI contract generation and CI contract tests.

## Content and provider responsibility

Book metadata being discoverable does not automatically grant a right to distribute the full text. Production releases should distinguish public-domain, open-licensed, user-owned, licensed, and unknown-rights content before enabling downloads or generated narration.

ElevenLabs plan terms also matter: use a plan that grants the rights required by the way ReadRealm is distributed or monetized.

## License

See [LICENSE](LICENSE). The root repository currently uses a custom license that restricts commercial use; third-party components retain their own licenses and notices.
