# ReadRealm — Android Client

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![minSdk](https://img.shields.io/badge/minSdk-30-brightgreen?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/about/versions)
[![Material 3](https://img.shields.io/badge/Material-3-673AB7?style=flat-square&logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](../../LICENSE)

The native **Android reader client** for [ReadRealm](../../README.md) — a Kotlin +
Jetpack Compose app that talks to the shared NestJS API for auth, the book
catalog, EPUB reading, audiobook (TTS) playback, real-time book chat and the
speech-realtime / voice-call features.

> This client is one of three native front-ends (Android / iOS / Flutter) that
> share a single backend. See the root README for the full architecture and the
> [engineering decisions](../../README.md#-engineering-decisions) on client
> strategy.

## What's actually here

A single-module Compose app (`:app`), `minSdk 30` / `compileSdk 35`. State lives
in `ViewModel`s; navigation is Compose `NavHost`. The real layout under
`app/src/main/java/tn/esprit/libraryapp/`:

```
libraryapp/
├── MainActivity.kt          # single-activity host
├── AppNavHost.kt            # Compose navigation graph
├── AppNavigation.kt
├── api/                     # Retrofit services (Book, User, ...)
├── repository/              # repositories over the API + local DB
├── database/  dao/          # Room (offline reading progress / bookmarks)
├── models/  enums/          # data models, Genre, reader models
├── screens/                 # Compose screens (Home, Login, EPub reader,
│                            #   BookChat, MyLibrary, Profile, Speech, ...)
│   └── experience/          # immersive "book-to-experience" screens
├── components/              # reusable composables (+ experience renderers)
├── services/                # AuthManager, Chat WebSocket, Audio/Voice, ...
├── viewModel/               # Auth, Book, EPubReader, Speech, ... view models
├── ui/theme/                # Color / Theme / Type
└── utils/                   # EpubReader, ReaderThemes, VoiceCommandHandler, ...
```

Notable libraries: **Stream Chat/Video** Compose SDKs (in-app chat & voice),
Retrofit (REST), and Room (offline cache). Real-time book chat connects to the
backend's Socket.IO gateway via `ChatWebSocketService`.

## Getting started

### Prerequisites

- **Android Studio** (Ladybug / 2024.2+ recommended)
- **JDK 17**
- **Android SDK** API 30+ (the CI build uses Temurin 17)

### Run

```bash
cd apps/android
./gradlew installDebug      # build + install to a connected device/emulator
# or open the folder in Android Studio and press Run
```

### Backend

The app points at the ReadRealm API base URL configured in
`api/RetrofitService.kt`. Point it at your own host (local `http://10.0.2.2:3000`
from the emulator, or your deployment) before building against a custom backend.
See [`apps/api`](../api) for running the server locally.

## Testing & static analysis

```bash
./gradlew testDebugUnitTest   # unit tests (run in CI)
./gradlew detekt              # static analysis (non-blocking in CI until baselined)
```

## Identity / branding

> NEEDS-PORT note: the application id and package are still
> `tn.esprit.libraryapp` (a leftover from the project's origin). Renaming the
> package to a `readrealm` namespace is an 80+ file refactor tracked as a
> good-first-issue in the root README — it touches every Kotlin file, the
> manifest, and the Gradle `namespace`/`applicationId`, so it is deliberately
> deferred rather than done piecemeal.

## License

MIT — see the [root LICENSE](../../LICENSE).
