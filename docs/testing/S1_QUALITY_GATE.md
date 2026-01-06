# S1 Local Quality Gate

Run the full Sprint 1 verification stack locally with a single Gradle task.

## Commands
- **Windows:** `gradlew.bat s1QualityGate`
- **macOS/Linux:** `./gradlew s1QualityGate`

Tip for CI-like runs: `./gradlew s1QualityGate --no-daemon --stacktrace`

## What the task runs
`s1QualityGate` wires together the main verification steps:
1. Build APKs: `:app:assembleDebug` and `:app:assembleRelease`.
2. Unit tests: prefers `:app:testDebugUnitTest` (falls back to `:app:test` if needed).
3. Lint: prefers `:app:lintDebug` (falls back to `:app:lint`).
4. Instrumentation smoke: uses Gradle Managed Devices if present (e.g., `:app:allDevicesDebugAndroidTest` or `:app:gmdDebugAndroidTest`); otherwise it runs `:app:connectedDebugAndroidTest`.

## Typical failure causes
- **Android SDK / emulator missing:** instrumentation tasks need either a managed device definition or a connected/emulated device.
- **Device offline or not authorized:** `connectedDebugAndroidTest` cannot deploy the test app.
- **Lint violations:** style or correctness issues flagged by the Android Lint tasks.
- **Unit test failures:** failing assertions in `testDebugUnitTest`/`test`.
- **Build issues:** missing resources or dependency conflicts preventing assembleDebug/assembleRelease.
