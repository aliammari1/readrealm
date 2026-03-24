# Contributing to ReadRealm

Thank you for your interest in contributing to ReadRealm! We welcome contributions from developers of all skill levels. This guide will help you get started.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Architecture Overview](#architecture-overview)
- [Development Guidelines](#development-guidelines)
- [Submitting a Pull Request](#submitting-a-pull-request)
- [Commit Message Format](#commit-message-format)
- [Testing](#testing)

## Code of Conduct

We are committed to providing a welcoming and inspiring community. Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md).

## Getting Started

### 1. Fork and Clone

```bash
# Fork the repository on GitHub
# Clone your fork
git clone https://github.com/YOUR_USERNAME/readrealm.git
cd readrealm

# Add upstream remote
git remote add upstream https://github.com/aliammari1/readrealm.git
```

### 2. Create a Feature Branch

```bash
# Update main branch
git checkout main
git pull upstream main

# Create feature branch
git checkout -b feature/your-feature-name
```

### 3. Install Dependencies

```bash
# First-time setup (installs all dependencies)
task setup

# Or manually:
cd apps/api && npm ci && cd ../..
cd apps/dashboard && flutter pub get && cd ../..
```

## Architecture Overview

ReadRealm is a **three-tier application**:

```
┌─────────────────────────────────────────────┐
│        📱 CLIENT APPS                       │
├──────────────────┬──────────────────────────┤
│ 📱 Android       │ 🍎 iOS                   │
│ (Kotlin)         │ (Swift)                  │
│ Native reader    │ Native reader            │
└──────────────────┴──────────────────────────┘
         ↕   REST/WebSocket
┌─────────────────────────────────────────────┐
│        🟢 BACKEND (API)                     │
├─────────────────────────────────────────────┤
│ NestJS • TypeScript • Socket.IO             │
│ Serves: Android, iOS, Admin Dashboard      │
└─────────────────────────────────────────────┘
         ↕
┌─────────────────────────────────────────────┐
│        📊 ADMIN DASHBOARD                   │
├─────────────────────────────────────────────┤
│ Flutter (Web/Desktop)                       │
│ Management, analytics, moderation           │
└─────────────────────────────────────────────┘
```

### Development Stacks

| Layer | Technology | Location |
|-------|-----------|----------|
| **Backend (API)** | NestJS, Node.js, TypeScript, MongoDB | `apps/api/` |
| **Android Client** | Kotlin, Jetpack Compose, Gradle | `apps/android/` |
| **iOS Client** | Swift, Xcode | `apps/ios/` |
| **Admin Dashboard** | Flutter, Dart | `apps/dashboard/` |

## Development Guidelines

### Backend (API)

Contributing to the NestJS backend:

```bash
# Install backend dependencies
cd apps/api && npm ci

# Start development server
npm run start:dev

# Run tests
npm run test
npm run test:e2e

# Lint and format
npm run lint
npm run format
```

**Best Practices:**
- Follow NestJS module structure (controllers, services, modules)
- Write tests for new features (aim for >80% coverage)
- Use JWT for authentication
- Document API endpoints in OpenAPI spec
- Add database migrations for schema changes

### Android Client

Contributing to the native Android app:

```bash
# Build debug APK
cd apps/android
./gradlew assembleDebug

# Run tests
./gradlew test

# Install to device
./gradlew installDebug
```

**Best Practices:**
- Use Jetpack Compose for UI
- Follow Material Design 3
- Handle API errors gracefully
- Implement offline-first caching
- Test on multiple Android versions (API 24+)

### iOS Client

Contributing to the native iOS app:

```bash
# Open Xcode
cd apps/ios
open Runner.xcworkspace

# Run tests in Xcode or:
xcodebuild test -workspace Runner.xcworkspace -scheme Runner
```

**Best Practices:**
- Use SwiftUI for UI (modern approach)
- Follow Apple Human Interface Guidelines
- Implement proper error handling
- Support iOS 13+
- Test on different device sizes

### Admin Dashboard (Flutter)

Contributing to the Flutter dashboard:

```bash
# Install dependencies
cd apps/dashboard
flutter pub get

# Run on web
flutter run -d chrome

# Run tests
flutter test

# Build for production
flutter build web
```

**Best Practices:**
- Use Provider for state management
- Follow Material Design 3
- Make it responsive (desktop/tablet/web)
- Write widget tests
- Use meaningful variable names

## Submitting a Pull Request

### Before You Start

1. **Check existing issues/PRs** — Don't duplicate work
2. **Open an issue first** (if significant) — Discuss your approach
3. **Test locally** — Ensure your code works
4. **Follow the code style** — Run linters and formatters

### PR Checklist

- [ ] Branch created from latest `main`
- [ ] Changes tested locally
- [ ] Tests added/updated
- [ ] Linting passes (`task lint` or equivalent)
- [ ] Code formatted (`task format` or equivalent)
- [ ] Commit messages follow format (see below)
- [ ] PR description clearly explains changes
- [ ] No breaking changes (or documented if breaking)

### Create Your PR

```bash
# Push your feature branch
git push origin feature/your-feature-name

# Create PR on GitHub with:
# - Clear title
# - Description of changes
# - Issue reference (if applicable)
# - Screenshots (for UI changes)
```

## Commit Message Format

Use clear, descriptive commit messages:

```
[TYPE] Short description (max 50 chars)

Longer explanation if needed. Reference issues with:
- Fixes #123
- Related to #456
```

**Types:**
- `feat:` — New feature
- `fix:` — Bug fix
- `docs:` — Documentation
- `style:` — Code style (formatting, missing semicolons, etc)
- `refactor:` — Code refactoring
- `perf:` — Performance improvement
- `test:` — Adding tests
- `chore:` — Build, dependencies, etc

**Example:**
```
feat: Add batch book import from CSV

- Parse CSV file and validate data
- Show import progress dialog
- Notify user on completion
- Fixes #89
```

## Testing

### Backend Testing

```bash
cd apps/api

# Unit tests
npm run test

# E2E tests
npm run test:e2e

# Test coverage
npm run test:cov
```

### Dashboard Testing

```bash
cd apps/dashboard

# Run all tests
flutter test

# Run specific test
flutter test test/path/to/test.dart

# Generate coverage
flutter test --coverage
```

## Development Workflow

### Quick Start

```bash
# Step 1: Setup everything
task setup

# Step 2: Start backend
task api:dev

# Step 3: In another terminal, start dashboard
task dashboard:run

# Step 4: In another terminal, run tests
task test
```

### Common Tasks

```bash
# Format all code
task format

# Run all linters
task lint

# Run all tests
task test

# Build for production
task api:build
task dashboard:build:web

# Docker deployment
task docker:up
task docker:down
```

## Need Help?

- 💬 **Questions?** Open a [Discussion](https://github.com/aliammari1/readrealm/discussions)
- 🐛 **Found a bug?** Open an [Issue](https://github.com/aliammari1/readrealm/issues)
- 📚 **More docs?** Check [docs/](shared/docs/) folder

## Resources

- [NestJS Documentation](https://docs.nestjs.com/)
- [Flutter Documentation](https://flutter.dev/docs)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Swift Documentation](https://developer.apple.com/swift/)
- [MongoDB Manual](https://docs.mongodb.com/manual/)

---

**Thank you for contributing to ReadRealm!** 🎉
