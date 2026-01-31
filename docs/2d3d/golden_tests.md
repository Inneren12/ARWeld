# 2D3D Golden Tests

This document explains how golden tests run in CI, how to update snapshots locally, and where to find mismatch artifacts.

## CI behavior

Golden tests run as part of the CI quality gates. CI will **fail** on any snapshot mismatch and **never** updates snapshots automatically.

If CI detects `-PupdateGolden=true` or `UPDATE_GOLDEN=true`, the build fails fast with a clear error so snapshots cannot be regenerated in CI.

## Running golden tests locally

Golden tests run via the standard unit test entrypoint:

```bash
./gradlew test --no-daemon
```

## Updating golden snapshots locally

To update golden snapshots on your machine:

```bash
./gradlew test -PupdateGolden=true --no-daemon
```

This rewrites the golden files in `src/test/resources/golden/` for the relevant module(s).

## CI mismatch artifacts

When a golden mismatch occurs, tests write the actual output into module build outputs. CI uploads them as the `golden-mismatch-artifacts` artifact. Example path:

```
feature-supervisor/build/outputs/golden_actual/export_report_actual.json
```

Download the artifact to compare the actual output with the committed golden file.
