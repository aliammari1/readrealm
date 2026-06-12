# ReadRealm — iOS Client

[![Swift](https://img.shields.io/badge/Swift-5.0+-FA7343?style=flat-square&logo=swift)](https://swift.org)
[![iOS](https://img.shields.io/badge/iOS-16.0+-000000?style=flat-square&logo=ios)](https://developer.apple.com/ios)
[![SwiftUI](https://img.shields.io/badge/SwiftUI-007ACC?style=flat-square&logo=swift)](https://developer.apple.com/xcode/swiftui/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](../../LICENSE)

The native iOS reader client for **[ReadRealm](../../README.md)** — a SwiftUI app
that talks to the shared NestJS API for auth, the book catalog, EPUB reading,
audiobook (TTS) playback, real-time book chat and the speech-realtime features.

> This client is one of three native front-ends (Android / iOS / Flutter) that
> share a single backend. See the root README for the full architecture and the
> [engineering decisions](../../README.md#-engineering-decisions) on client
> strategy.

## What's actually here

This is a single-target SwiftUI app (no CocoaPods, no Carthage — dependencies are
managed with **Swift Package Manager**, see `Package.resolved`). The real layout:

```
apps/ios/
└── ReadRealm/                       # (was "Application 4" — de-spaced)
    ├── Application.xcodeproj         # Xcode project (scheme: "ReadRealm")
    └── Application/
        ├── ApplicationApp.swift      # @main app entry point
        ├── ContentView.swift
        ├── AuthManager.swift
        ├── Model/                    # Book, User
        ├── Models/                   # ReadingPreferences, ReadingStatistics, BookmarkPage
        ├── View/                     # SwiftUI screens (Home, Sign In/Up, EpubReader,
        │                             #   AudioBookPlayer, BookDetails, Search, Profile, …)
        ├── ViewModel/ ViewModels/    # AuthViewModel, BookViewModel, EpubReaderViewModel
        ├── Helper/                   # AudioStreamService
        ├── Utils/                    # HapticManager, PDFExporter, TranslationManager, VoiceCommand
        └── Assets.xcassets/          # App icon, accent colour, sample book covers
```

## Getting started

### Prerequisites
- macOS with **Xcode 15+** (iOS 16.0+ SDK)
- An Apple Developer team for on-device runs (simulator needs none)

### Run
```bash
open "apps/ios/ReadRealm/Application.xcodeproj"
# In Xcode: pick the "ReadRealm" scheme, choose a simulator, ⌘R
```

### Backend
The app points at the deployed ReadRealm API. The base URL is currently hard-coded
in the view models (e.g. `BookViewModel.baseURL`). Point it at your own API host
(local `http://localhost:3000` or your deployment) before building against a custom
backend. See `apps/api` for running the server locally.

## Identity / branding

- **Bundle identifier:** `com.aliammari.readrealm` (rebranded from the old
  `tn.esprit.Application` placeholder).
- **Scheme:** `ReadRealm`.

> NEEDS-PORT note: the Xcode **target** is still internally named `Application`
> (the `.xcodeproj`, the `Application/` source folder, and the build-product name
> `Application.app`). Renaming the target requires editing the project's
> `project.pbxproj` group/target identifiers and is best done from within Xcode
> (Product → Scheme + target rename) so all internal references stay consistent.
> The space-in-path, the `__MACOSX/` junk, the per-user Xcode state, the scheme
> name and the bundle id have all been cleaned up; the target rename is the one
> remaining cosmetic item.

## Testing

There is no XCTest target yet. Adding `ReadRealmTests` (unit) and
`ReadRealmUITests` (UI) is tracked as a good-first-issue; once present, CI runs
them via `xcodebuild test` on a macOS runner (the iOS CI job is currently
non-blocking until the target/test setup lands).

## License

MIT — see the [root LICENSE](../../LICENSE).
