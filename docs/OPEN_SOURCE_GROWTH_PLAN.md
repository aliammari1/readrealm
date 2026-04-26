# Open Source Growth Plan

This plan turns the repository review into a practical backlog for ReadRealm.

## Quick wins

- Add screenshots and a short demo video for reading, sync, chat, voice commands, and the admin panel.
- Add API docs for the backend and typed contracts between clients and services.
- Add tests for sync, chat, voice-command handling, and offline conflict resolution.
- Add accessibility checks for mobile and web surfaces.
- Add demo data so contributors can run the project without production services.

## Bugs and bad practices to watch

- Client-side token exposure or inconsistent environment configuration.
- Sync conflict bugs when devices edit the same reading state offline.
- Chat or voice-command failures without clear fallback states.
- Business logic duplicated across mobile, admin, and backend clients.
- Missing encryption or privacy documentation for notes and chat content.

## Star growth strategy

1. Put AI-assisted reading and cross-device sync screenshots above the fold.
2. Publish a demo with sample books and synthetic data.
3. Add `good first issue` tasks for UI polish, docs, tests, and sample content.
4. Share a technical article about building an AI reading platform.
5. Link the repository from the profile README and portfolio.

## Trending-library opportunities

- Use LangExtract-style extraction for characters, concepts, and study aids.
- Use MarkItDown-style export for notes and highlights.
- Use Data-Formulator-style analytics for reading progress dashboards.
- Use Pydantic-AI-style schemas for structured AI summaries.

## Suggested next PRs

- Add sync integration tests.
- Add architecture and data-flow documentation.
- Add demo mode with seeded books and reading history.
