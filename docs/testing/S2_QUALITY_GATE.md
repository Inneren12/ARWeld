# S2 Local Quality Gate

Run the Sprint 2 local quality gate with a single Gradle task. This gate includes assembling APKs, running unit tests, and performing lint checks. **Instrumentation tests are intentionally excluded** to allow fast local verification.

## Commands
- **Windows:** `gradlew.bat s2QualityGate`
- **macOS/Linux:** `./gradlew s2QualityGate`

Tip for CI-like runs: `./gradlew s2QualityGate --no-daemon --stacktrace`

## What the task runs
`s2QualityGate` wires together the following verification steps:
1. **Build APKs:** `:app:assembleDebug` and `:app:assembleRelease`
2. **Unit tests:** `:app:testDebugUnitTest`
3. **Lint:** `:app:lintDebug`

**Note:** Instrumentation tests are **NOT** included in this gate. Use `./gradlew s2InstrumentationSmoke` to run instrumentation tests separately.

## Difference between S1 and S2 Quality Gates
Currently, `s1QualityGate` and `s2QualityGate` run the same tasks. As the project evolves, Sprint 2 may add additional verification steps (e.g., stricter lint rules, additional test suites, or code quality checks). For now, both gates ensure:
- All variants build successfully
- All unit tests pass
- No lint violations

## Typical failure causes
- **Lint violations:** Style or correctness issues flagged by Android Lint
- **Unit test failures:** Failing assertions in `testDebugUnitTest`
- **Build issues:** Missing resources, dependency conflicts, or compilation errors preventing assembleDebug/assembleRelease

## Running instrumentation tests separately
Instrumentation tests are kept separate to avoid requiring an emulator or physical device for fast local verification. To run instrumentation tests on managed devices:

```bash
./gradlew s2InstrumentationSmoke
```

### Instrumentation smoke test details
The `s2InstrumentationSmoke` task runs instrumentation tests on the configured Gradle Managed Device:
1. `:app:pixel6Api34DebugAndroidTest` - Runs tests on Pixel 6 API 34 managed device

### Requirements for instrumentation tests
- **Managed devices:** Defined in `app/build.gradle.kts` under `testOptions.managedDevices`
- **Android SDK:** Must have the required system images downloaded
- **Time:** First run downloads system images and creates AVD (can take several minutes)

### Troubleshooting instrumentation tests
- **"No tasks available":** No managed device configuration found; check `app/build.gradle.kts`
- **System image download failures:** Network issues or missing Android SDK licenses
- **AVD creation failures:** Insufficient disk space or Android SDK configuration issues

## Dry-run commands
To see what tasks will run without executing them:

```bash
# Windows
gradlew.bat s2QualityGate --dry-run
gradlew.bat s2InstrumentationSmoke --dry-run

# macOS/Linux
./gradlew s2QualityGate --dry-run
./gradlew s2InstrumentationSmoke --dry-run
```
