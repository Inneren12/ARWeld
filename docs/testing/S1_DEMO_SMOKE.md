# Sprint 1 Demo Smoke Tests

Minimal instrumentation coverage for the Sprint 1 navigation flows (Login → Home → stub screens). Tests rely on seeded Room data and avoid camera/AR surfaces.

## Commands

Prerequisites: Android SDK + emulator/physical device available to the build.

Run JVM/unit tests:

```bash
./gradlew test
```

Run the demo smoke instrumentation suite (requires a connected/emulated device):

```bash
./gradlew :app:connectedDebugAndroidTest
```

## Expected Outcomes

- **Login → Home**: Selecting a seeded user (e.g., "Assembler 1") signs in and shows the Home screen with the assembler-specific tile.
- **Home → Timeline**: Tapping **Timeline** on Home opens the stub timeline screen (`"Timeline stub"`).
- **WorkItemSummary fallback**: Opening **Work Item Summary** without a work item ID shows the graceful "Work item not found" state with a retry affordance.

The instrumentation suite uses Hilt with the in-app seed initializer to populate the Room user/work item tables before each test run.
