# Observability (S1 Lite)

This app ships with a minimal observability stack that is safe for release builds and requires no external secrets. All logging is centralized behind small facades so we can later swap implementations without touching feature code.

## Runtime logging

- **Timber (debug only):** planted from `ArWeldApplication` in debug builds so Logcat shows tagged debug/info/error statements.
- **AppLogger facade:** presentation layers log key moments via `AppLogger` (provided by Hilt):
  - Navigation changes (recorded via a NavController listener).
  - Login attempts and successes (including associating the user with crash reports).
  - Repository failures (caught in viewmodels/use cases).
  - Unhandled UI exceptions (from the Compose error boundary).
- **Where to look:** attach Logcat while running a debug build; filter by the `AppLogger` tag or by `Navigation` to see route transitions.

## Error boundary for Compose routes

- `AppErrorBoundary` wraps the root navigation in `MainActivity`. It catches unexpected exceptions during composition and shows a friendly retry screen instead of silently crashing.
- Captured errors are forwarded to `AppLogger.logUnhandledError`, which also notifies the crash reporter.

## Crash reporting interface

- `CrashReporter` is a lightweight abstraction. The default binding is `NoOpCrashReporter`, which records nothing and is safe for release builds without credentials.
- To wire an external service (e.g., Firebase Crashlytics):
  1. Add the SDK dependency and project-level configuration.
  2. Implement `CrashReporter` to delegate to the SDK (e.g., `Crashlytics.recordException`).
  3. Replace the `bindCrashReporter` binding in `LoggingModule` with the concrete implementation.
  4. Keep `logLoginSuccess` calling `setUserId` so crash trails include user context.

## Signals covered (Sprint 1 screens)

- Login flow: attempts, successes, and repository failures.
- Navigation: every destination change emitted by the NavController.
- Repository failures surfaced from the scanner use case.
- Unhandled UI exceptions via the Compose error boundary.
