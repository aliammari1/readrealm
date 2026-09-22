# ReadRealm Store Release Checklist

This checklist covers the remaining manual release work after the repository's CI and store-build hardening.

## Release identity

- Android application ID: `com.aliammari.readrealm`
- iOS bundle identifier: `com.aliammari.readrealm`
- Current first-release version: `1.0.0`
- Increment Android `versionCode` and iOS `CURRENT_PROJECT_VERSION` for every subsequent store upload.

## Production backend

Before building the store binaries, deploy the NestJS API to a long-running HTTPS environment that supports the app's realtime requirements.

Configure at minimum:

- `MONGODB_URL`
- a strong `JWT_SECRET`
- production `CORS_ORIGIN`
- SMTP settings used by verification/deletion flows
- `ELEVENLABS_API_KEY`, `ELEVENLABS_VOICE_ID`, and optional agent/model settings when voice features are enabled
- `OLLAMA_URL` and `OLLAMA_MODEL` when summaries/book chat are enabled
- optional `SENTRY_DSN`

Verify these public endpoints after deployment:

- `/health`
- `/health/live`
- `/privacy`
- `/account-deletion`

Do not submit store builds that still point at a development or temporary backend.

## Android / Google Play

1. Create a Play upload keystore and keep it outside the repository.
2. Provide release signing through:
   - `READREALM_KEYSTORE_PATH`
   - `READREALM_KEYSTORE_PASSWORD`
   - `READREALM_KEY_ALIAS`
   - `READREALM_KEY_PASSWORD`
3. Set `READREALM_API_BASE_URL` to the final production HTTPS API before the signed release build.
4. Build the release bundle with:
   - `cd apps/android`
   - `./gradlew bundleRelease`
5. Upload the resulting AAB to an internal testing track first.
6. Complete Google Play:
   - App name and descriptions
   - app icon / feature graphic / screenshots
   - Data Safety form
   - content rating
   - target audience
   - support/contact details
   - privacy-policy URL: `https://<production-api-domain>/privacy`
   - account-deletion URL: `https://<production-api-domain>/account-deletion`
7. Smoke-test registration, verification, login, EPUB reading, bookmarks, reviews, account deletion, narration, AI features, and realtime chat on a physical device.

## iOS / App Store

1. Add the final branded 1024×1024 PNG to:
   `apps/ios/ReadRealm/Application/Assets.xcassets/AppIcon.appiconset`
   and reference its filename from `Contents.json`.
2. Configure the Apple Developer Team, signing certificate, App ID, and provisioning profile for `com.aliammari.readrealm`.
3. Set the Release build setting `READREALM_API_BASE_URL` to the final production HTTPS API.
4. Archive with the current App Store-supported Xcode/iOS SDK and upload through Xcode Organizer/App Store Connect.
5. Complete App Store Connect:
   - display name, subtitle, description, keywords
   - support URL
   - privacy-policy URL: `https://<production-api-domain>/privacy`
   - screenshots for required device classes
   - age rating
   - App Privacy answers matching `PrivacyInfo.xcprivacy` and the final enabled providers
   - review notes for account creation, voice, AI, and any login requirements
6. Run a TestFlight smoke test on physical hardware before production review.
7. Test in-app account deletion from Profile and verify the external deletion page.

## CI gate

Do not merge a release candidate if any blocking job is red:

- API format/lint/typecheck/unit/E2E/build/OpenAPI sync
- Android unit tests + `bundleRelease`
- Flutter analyzer + tests
- iOS Xcode 26+ simulator build
- CodeQL
- Trivy
- gitleaks

## Final manual checks

- No placeholder domains, API keys, demo credentials, or committed signing secrets.
- Production API is HTTPS and reachable from both mobile platforms.
- Email verification and account-deletion mail delivery work in production.
- Voice/AI provider quotas and failure states are acceptable.
- Store screenshots match the submitted build.
- Privacy/Data Safety/App Privacy answers match the exact production feature set.
- A real support channel is available to store reviewers and users.
