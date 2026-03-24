# 🚀 Quick Start Guide

Get ReadRealm running in minutes!

## 📋 Prerequisites

Choose based on what you want to develop:

### For Backend (API)
- Node.js 20+
- MongoDB 5+ (local or Docker)

### For Android Client
- Android SDK 24+
- Gradle 8+

### For iOS Client
- Xcode 15+ (macOS only)

### For Admin Dashboard
- Flutter 3.20+
- Dart 3.4+

---

## ⚡ 30-Second Setup

```bash
# 1. Clone and enter directory
git clone https://github.com/aliammari1/readrealm.git
cd readrealm

# 2. First-time setup (installs all dependencies)
task setup

# 3. Start backend in terminal 1
task api:dev

# 4. Start dashboard in terminal 2
task dashboard:run

# 5. Open browser to http://localhost:3000
```

**That's it!** 🎉

---

## 🛠️ Common Tasks

### Backend Development
```bash
task api:dev          # Start development server
task api:test         # Run tests
task api:build        # Build for production
```

### Android Development
```bash
task android:build    # Build debug APK
task android:test     # Run unit tests
```

### iOS Development
```bash
task ios:build        # Build for iOS
task ios:test         # Run unit tests
```

### Admin Dashboard
```bash
task dashboard:run    # Run web/desktop admin
task dashboard:test   # Run tests
```

### Docker Deployment
```bash
task docker:up        # Start API + MongoDB in Docker
task docker:down      # Stop services
```

---

## 📁 Project Structure

```
readrealm/
├── apps/
│   ├── api/          🟢 Backend (NestJS) - SERVER
│   ├── android/      📱 Android Client app
│   ├── ios/          🍎 iOS Client app
│   └── dashboard/    🖥️ Admin Dashboard (Flutter)
├── shared/           📦 Shared resources
└── scripts/          🛠️ Utility scripts
```

---

## 🔧 Configuration

### Backend Environment Variables

```bash
# Copy template
cp apps/api/.env.example apps/api/.env

# Edit with your values
MONGODB_URL=mongodb://localhost:27017/readrealm
JWT_SECRET=your_secret_key
GOOGLE_AI_KEY=your_key
AZURE_TTS_KEY=your_key
# ... see .env.example for all variables
```

### Docker Setup

```bash
# Copy environment file if needed
cp .env.example .env

# Start services
docker-compose up -d

# Check status
docker-compose logs -f api
docker-compose logs -f mongodb
```

---

## 🧪 Testing

```bash
# Backend tests
task api:test         # Unit tests
task api:test:e2e     # E2E tests

# Dashboard tests
task dashboard:test

# All tests
task test
```

---

## 📊 Architecture Overview

```
[📱 Android] [🍎 iOS]
     ↓           ↓
[REST/WebSocket]
     ↓           ↓
[🟢 NestJS API]←→[🗄️ MongoDB]
     ↑           ↑
[🖥️ Flutter Admin Dashboard]
```

**All clients connect to one backend API**

---

## 🐛 Troubleshooting

### "Command not found: task"

Install Task from https://taskfile.dev

Alternatively, run commands manually:
```bash
cd apps/api && npm run start:dev
cd apps/dashboard && flutter run
```

### MongoDB Connection Error

```bash
# Check if MongoDB is running
mongosh localhost:27017

# Or use Docker
task docker:up
```

### Port 3000 Already in Use

```bash
# Find what's using port 3000
lsof -i :3000

# Kill the process
kill -9 <PID>

# Or use different port
PORT=3001 npm run start:dev
```

### Flutter Issues

```bash
# Clean and reinstall
flutter clean
flutter pub get
flutter run
```

---

## 📚 Full Documentation

- [README.md](README.md) — Complete project overview
- [CONTRIBUTING.md](CONTRIBUTING.md) — Contribution guidelines
- [API Spec](shared/api-spec/openapi.yaml) — REST API documentation
- [Taskfile.yml](Taskfile.yml) — All available tasks

---

## 💬 Need Help?

- 🐛 [Report a Bug](https://github.com/aliammari1/readrealm/issues)
- 💡 [Start Discussion](https://github.com/aliammari1/readrealm/discussions)
- 📧 Contact the team

---

**Happy coding! 🎉**
