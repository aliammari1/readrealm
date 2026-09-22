# ReadRealm Privacy Policy

**Effective date:** September 22, 2026

ReadRealm is a reading application that provides book discovery, EPUB reading, bookmarks, reviews, book discussions, optional voice features, and optional AI-assisted reading features.

This policy describes the information ReadRealm processes, why it is used, and the controls available to you.

When the production API is deployed, this policy is also available at `https://YOUR_PRODUCTION_API_DOMAIN/privacy`. Replace the placeholder with the final HTTPS API domain before store submission and use that public URL in App Store Connect and Google Play Console.

## Information ReadRealm processes

ReadRealm may process the following information when you use features that require it:

- **Account information:** username, email address, account identifier, authentication credentials in hashed or tokenized form, and an optional profile image.
- **Reading and library activity:** bookmarks, reviews, reading-related content, book-room participation, and other content you choose to create or save.
- **Messages and user-generated content:** messages you send in book discussions and content you provide to reading-assistance features.
- **Voice/audio data:** microphone audio when you explicitly start a voice feature. Voice features require microphone permission.
- **Technical diagnostics:** server logs needed for security, reliability, abuse prevention, and troubleshooting. Optional error monitoring may be enabled by the ReadRealm operator.

ReadRealm does not use these categories for cross-app advertising tracking.

## Why the information is used

ReadRealm uses information to:

- create and secure your account;
- provide reading, bookmarking, review, discussion, voice, and AI-assisted features;
- synchronize or retrieve content associated with your account;
- send account verification and security messages;
- prevent abuse and protect the service; and
- maintain reliability and diagnose failures.

## Service providers and external services

ReadRealm can use third-party services to provide specific functionality:

- **ElevenLabs** for optional enhanced narration or voice-agent features;
- **Stream** for supported chat/video functionality where enabled;
- **Open Library, Gutendex, and Project Gutenberg metadata sources** for book discovery;
- **Sentry** for optional error monitoring when configured by the deployment operator.

ReadRealm's book-summary and book-chat AI can be operated through a self-hosted **Ollama** instance, which does not require a third-party AI API key.

Third-party providers process information according to their own terms and privacy policies. A ReadRealm deployment should enable only the providers it actually uses.

## Data retention

Account and user-generated data is retained while your account is active unless a shorter retention period is required for a specific operational purpose. Security and diagnostic records may be retained for a limited period where necessary to protect and operate the service.

## Account and data deletion

You can permanently delete your account inside ReadRealm from **Profile → Delete Account**.

You can also request deletion outside the app at:

`https://YOUR_PRODUCTION_API_DOMAIN/account-deletion`

Before publishing ReadRealm, the deployment operator must replace `YOUR_PRODUCTION_API_DOMAIN` with the final HTTPS API domain and use that URL in the Google Play account-deletion field.

Deletion removes the ReadRealm account and associated server-side data handled by the deletion service, including active refresh sessions, reviews, bookmarks, and chat messages. Some information may be retained only where required by law, security obligations, or a legitimate dispute-resolution need.

## Device permissions

ReadRealm requests permissions only for features that need them, such as microphone access for voice features, camera access for book scanning, and notifications where enabled. You can revoke permissions in your device settings.

## Security

ReadRealm uses HTTPS for production mobile traffic, hashed passwords, access tokens, server-side validation, rate limiting, security headers, and other safeguards. No internet service can guarantee absolute security.

## Children

ReadRealm is not intentionally designed to collect personal information from children below the minimum age permitted by applicable law without appropriate consent. Store age ratings and any child-directed distribution settings must reflect the final product and target audience.

## Changes

This policy may be updated as ReadRealm features, providers, or legal requirements change. Material changes should be reflected in the effective date above and in the store privacy disclosures.

## Contact

For project-level privacy or security questions, use the ReadRealm GitHub repository's issue/contact channels **without posting passwords, tokens, private reading content, or other sensitive personal data publicly**.

For account deletion, use the in-app deletion control or the production `/account-deletion` page described above.
