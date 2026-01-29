# AR Sprint 1 / Task 04 — Move Marker Detection Pipeline to :core-ar Closeout

**Task:** Move marker detection pipeline into `:core-ar` (detector only, no behavior changes)
**Status:** Complete
**Date:** 2026-02-28

## Summary

Moved the marker detection interface, models, and default ML Kit implementation into `:core-ar` so engine code owns detection while `feature-arview` consumes the core APIs. Detection cadence, thresholds, and rotation handling remain unchanged.

## What Changed

### Moved Files

| Original Location | New Location |
|-------------------|--------------|
| `feature-arview/.../marker/MarkerDetector.kt` | `core-ar/.../marker/MarkerDetector.kt` |
| `feature-arview/.../marker/RealMarkerDetector.kt` | `core-ar/.../marker/RealMarkerDetector.kt` |
| `feature-arview/src/test/.../marker/RealMarkerDetectorTest.kt` | `core-ar/src/test/.../marker/RealMarkerDetectorTest.kt` |

### Modified Files

| File | Change |
|------|--------|
| `feature-arview/.../arcore/ARViewController.kt` | Updated marker imports to `core-ar` |
| `feature-arview/.../marker/SimulatedMarkerDetector.kt` | Implement `core-ar` marker interface |
| `feature-arview/.../marker/StubMarkerDetector.kt` | Implement `core-ar` marker interface |
| `feature-arview/.../pose/MarkerPoseEstimator.kt` | Updated `DetectedMarker` import |
| `feature-arview/src/test/.../pose/MarkerPoseEstimatorTest.kt` | Updated `DetectedMarker` import |
| `core-ar/build.gradle.kts` | Added ML Kit dependency + BuildConfig for detector logging |
| `feature-arview/build.gradle.kts` | Removed ML Kit dependency (now in core-ar) |
| `app/src/androidTest/.../ARViewSmokeTest.kt` | Assert marker pipeline state is present |
| `docs/MODULES.md` | Documented marker pipeline moved to core-ar |
| `docs/FILE_OVERVIEW.md` | Updated marker pipeline locations |
| `docs/architecture/core-ar_boundary.md` | Added marker pipeline move note + ML Kit allowed |
| `docs/sprints/AR_S1_CORE_AR_04_CLOSEOUT.md` | This closeout document |

## Package Changes

- **Old package:** `com.example.arweld.feature.arview.marker`
- **New package:** `com.example.arweld.core.ar.marker`

## Classes Moved

1. **MarkerDetector** — Interface for detection on ARCore `Frame` objects.
2. **DetectedMarker** — Marker ID + ordered corner list + timestamp.
3. **RealMarkerDetector** — ML Kit barcode-backed detector with throttle, rotation mapping, and corner ordering.

## What Was NOT Moved

- **Pose estimation** (`MarkerPoseEstimator`) remains in `feature-arview`.
- **Simulated/Stub detectors** stay in `feature-arview` for debug-only triggers.
- **Alignment logic and rendering** remain unchanged.

## Verification Commands

```bash
# Verify core-ar compiles with marker pipeline
./gradlew :core-ar:assembleDebug

# Run core-ar unit tests
./gradlew :core-ar:testDebugUnitTest

# Verify feature-arview compiles against core-ar marker APIs
./gradlew :feature-arview:assembleDebug

# Run instrumentation smoke test
./gradlew :app:connectedAndroidTest
```

## Acceptance Criteria Met

- [x] Marker detector interface + model moved to `core-ar`
- [x] Default ML Kit implementation moved to `core-ar` with no behavior changes
- [x] Feature module consumes core-ar marker APIs only
- [x] Instrumentation smoke test asserts marker pipeline state exists
- [x] Documentation updated (`MODULES.md`, `FILE_OVERVIEW.md`, `core-ar_boundary.md`)

## Constraints Observed

- Detection algorithm, thresholds, and processing cadence unchanged
- Pose estimation not moved
- No new CV libraries introduced
