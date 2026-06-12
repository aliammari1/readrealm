# ReadRealm — Growth & Distribution Kit

Internal playbook for getting ReadRealm discovered. Everything here is **ready to
paste** — the maintainer fires the GitHub-account / posting actions; the in-repo
content (README, badges, footers) is already done on `feat/upgrade`.

Positioning, in one line:
> **Open-source AI book-chat platform — one NestJS backend → Android, iOS & Flutter
> clients. A self-hostable alternative to Speechify / Blinkist-style book AI.**

---

## 1. Repo metadata (set on GitHub → Settings / About)

**About (description):**
> Open-source AI book-chat platform: one NestJS backend → native Android (Kotlin),
> iOS (Swift) & Flutter clients. Self-hostable alternative to Speechify/Blinkist —
> BYOK multi-provider AI, TTS, real-time book rooms. MIT.

**Topics (12 — exact-match search phrases; limit is 20):**

```
nestjs
flutter
kotlin
swift
socket-io
mongodb
ebook
ai-agent
self-hosted
typescript
text-to-speech
open-source
```

**Website field:** the hosted Mintlify docs URL (the interactive API playground).

---

## 2. Launch posts (coordinated 48h window — post Tue–Thu, 13:00–16:00 UTC)

Seed the first comment yourself within the hour; reply to every commenter.

### Show HN

**Title:**
> Show HN: ReadRealm – one NestJS backend serving native Android, iOS and Flutter clients

**Body:**
> I kept seeing book-AI apps (Speechify, Blinkist-style) that are closed,
> subscription-only, and lock you to their AI. So I built ReadRealm: an
> open-source, self-hostable book-chat platform where a single NestJS API powers
> chat-with-your-book (streaming Claude + tool use), AI summaries, TTS narration
> and real-time reading rooms — and three separate clients (native Kotlin
> Android, native Swift iOS, and a Flutter admin dashboard) all talk to that one
> API.
>
> The interesting engineering bit for me was the "one backend → three clients"
> contract: the OpenAPI spec is generated from the live NestJS app (`@nestjs/swagger`),
> CI fails on drift, and the same spec drives Swagger UI, the Mintlify playground,
> and the native clients. Realtime chat runs on Socket.IO, with a Cloudflare
> Durable Object implementation for edge book-rooms.
>
> It's MIT, BYOK (Google/OpenAI/HuggingFace/Azure/Anthropic), and runs with
> `docker compose up`. Honest status: the Workers HTTP bridge is a scoped
> good-first-issue, not deployed yet — the Node app runs today.
>
> Repo + interactive API playground in the README. Feedback very welcome,
> especially on the multi-client API contract.

### r/selfhosted

**Title:** `I built an open-source, self-hostable alternative to Speechify/Blinkist-style book AI`

**Body:**
> ReadRealm self-hosts the whole stack: NestJS + MongoDB backend, `docker compose
> up`, BYOK so you're not locked to one AI provider, and your library/chats stay
> on your box. It serves native Android, iOS and a Flutter dashboard from one API.
> MIT. Would love feedback from folks self-hosting their reading setup.

### r/flutterdev

**Title:** `Flutter dashboard + native iOS/Android against one NestJS API — lessons on the multi-client contract`

**Body:**
> ReadRealm ships a Flutter admin client and native Kotlin/Swift reader apps that
> all consume the same OpenAPI-generated NestJS API. Post covers why I'd now treat
> **Flutter as the primary client** and keep the native apps as reference
> implementations. Code is MIT — curious how others structure the shared API
> contract across Flutter + native.

### Other channels
- **dev.to / Hashnode** cross-post of the Show HN write-up with `canonical_url`.
- **X / LinkedIn:** the "one NestJS backend → 3 native clients" hook + the demo GIF.
- **Product Hunt** (optional) once the GIF banner is finalized (see `BANNER.md`).

---

## 3. Awesome-list & directory submissions (compounding, low-effort)

Each is a small PR / form. Pass `awesome-lint` where applicable.

- **awesome-nestjs** (`nestjs/awesome-nestjs`) — under *Open Source* / *Boilerplate*:
  > `[ReadRealm](https://github.com/aliammari1/readrealm) - Open-source AI book-chat platform: one NestJS backend (REST + Socket.IO, generated OpenAPI) serving native Android, iOS & Flutter clients, with multi-provider AI and TTS. MIT.`
- **awesome-flutter** (`Solido/awesome-flutter`) — under *Open Source Apps*:
  > `[ReadRealm](https://github.com/aliammari1/readrealm) - Flutter admin dashboard for an open-source, self-hostable AI book-chat platform backed by a single NestJS API (also serves native Android & iOS).`
- **awesome-selfhosted** (`awesome-selfhosted/awesome-selfhosted`) — *Document Management - E-books*:
  > `ReadRealm - Self-hostable AI book-chat platform (chat-with-your-book, summaries, TTS, real-time reading rooms); one NestJS backend → Android/iOS/Flutter clients, BYOK multi-provider AI. (MIT)`
- **Open-source-alternative directories:** OpenAlternative, opensource.builders, SaaSHub, LibHunt — list as the open-source alternative to **Speechify / Blinkist**.

---

## 4. Strategic note — "Flutter as primary client"

ReadRealm currently ships **three** clients (native Kotlin Android, native Swift
iOS, Flutter dashboard) against one API. That's real maintenance overlap. The
growth-aligned recommendation, also flagged in the README's *Engineering
decisions*:

- **Treat Flutter as the primary product client** — one codebase → Android, iOS,
  web and desktop. It's the cheapest path to "available on every platform," which
  is itself a star/adoption driver and the cleanest demo story.
- **Keep the native Kotlin/Swift apps as reference implementations** of
  platform-idiomatic patterns (Jetpack Compose, SwiftUI) rather than shipping all
  three in parallel. This doubles as a portfolio signal (proves native chops)
  without tripling ongoing maintenance.
- For launches, **lead with the one-backend-→-three-clients architecture hook**
  (it's the differentiator), then point store/APK/TestFlight artifacts at whichever
  client is most demo-ready.

---

## 5. GROWTH backlog (bigger bets, not blocking)

These are larger items tracked for later — they raise the quality/credibility
signal but are out of scope for the current tooling pass:

- **NestJS 10 → 11** (Express 5, built-in JSON logging) — full migration with test runs.
- **Consolidate the 5 hand-rolled AI SDKs behind Vercel AI SDK v6 + AI Gateway** —
  unifies provider switching, telemetry (token/cost), and retries.
- **Sentry + OpenTelemetry GenAI spans** for the AI book-chat (token usage, latency, cost).
- **A "book-chat MCP" server** wrapping the AI participant so the book-chat is
  installable into other AI clients — a compounding distribution artifact.
- **Finalize the demo GIF banner** (prompts in `BANNER.md`) — the single biggest
  README conversion lever.
- **WebAuthn / passkeys** (`@simplewebauthn/server`) on the auth surface.
