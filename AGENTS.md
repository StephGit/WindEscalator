
# Project Overview

Wind alerting app for Android in Kotlin. Using Firebase Functions for backend services and alert notifications.

# Architecture

app/
  src/
    alert/        → Alert-related logic
    data/         → Business logic
    di/           → Dependency injection
    utils/        → Common utilities
    webcam/       → Webcam-related logic
    wind/         → Wind-related logic
functions/
  src/
    index.ts      → Entry point for Firebase Functions


# Commands
- Android: `./gradlew installDebug`
- Functions: `pnpm run build` from `functions/`
- Functions lint: `pnpm run lint` from `functions/`

# Constraints
- Do not add AndroidX Core dependencies without approval.
- Do not change exported-component visibility or permissions without confirming the sender/caller contract.
- Do not treat Firestore Rules, Functions endpoints, or cleartext traffic as covered by this skill.

# Dependency policy
- Do not add or upgrade dependencies without explicit approval.
- Keep `pnpm-lock.yaml` synchronized after approved Functions dependency changes.

# Firebase Functions
- Use Node 22.22.3 and pnpm 11.10.0.
- Use mise instead of nvm or other version managers.
- Do not use a pnpm workspace outside `functions/`.
- Run build and lint before a Functions deploy.

# Android
- Use Kotlin idioms and keep ViewModels free of Android framework UI concerns.
