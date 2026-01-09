# S1 Local Quality Gate

Run the Sprint 1 local quality gate with a single Gradle task. This gate includes assembling APKs, running unit tests, and performing lint checks. **Instrumentation tests are intentionally excluded** to allow fast local verification.

## Commands
- **Windows:** `gradlew.bat s1QualityGate`
- **macOS/Linux:** `./gradlew s1QualityGate`

Tip for CI-like runs: `./gradlew s1QualityGate --no-daemon --stacktrace`

## What the task runs
`s1QualityGate` wires together the following verification steps:
1. **Build APKs:** `:app:assembleDebug` and `:app:assembleRelease`
2. **Unit tests:** `:app:testDebugUnitTest` (falls back to `:app:test` if needed)
3. **Lint:** `:app:lintDebug` (falls back to `:app:lint` if needed)

**Note:** Instrumentation tests are **NOT** included in this gate. Use `./gradlew s2InstrumentationSmoke` to run instrumentation tests separately.

## Typical failure causes
- **Lint violations:** Style or correctness issues flagged by Android Lint
- **Unit test failures:** Failing assertions in `testDebugUnitTest` or `test` tasks
- **Build issues:** Missing resources, dependency conflicts, or compilation errors preventing assembleDebug/assembleRelease

## Running instrumentation tests separately
To run instrumentation tests on managed devices:
```bash
./gradlew s2InstrumentationSmoke
```
See [S2_QUALITY_GATE.md](S2_QUALITY_GATE.md) for more details.
