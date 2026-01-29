# AR Sprint 1 / Task 02 — Move Math/Spatial Helpers to :core-ar Closeout

**Task:** Move AR math/spatial helpers from feature-arview into :core-ar (no behavior changes)
**Status:** Complete
**Date:** 2026-01-29

## Summary

Relocated AR-specific math/spatial helper classes from `feature-arview` into `:core-ar` so that AR engine code can live in the core module without depending on feature modules. This is a zero-diff refactor with only package path changes.

## What Changed

### Moved Files

| Original Location | New Location |
|-------------------|--------------|
| `feature-arview/.../geometry/Point2f.kt` | `core-ar/.../spatial/Point2f.kt` |
| `feature-arview/.../geometry/CornerOrdering.kt` | `core-ar/.../spatial/CornerOrdering.kt` |

### New Files

| File | Description |
|------|-------------|
| `core-ar/src/main/kotlin/.../spatial/Point2f.kt` | Pure Kotlin 2D point + Android PointF converters |
| `core-ar/src/main/kotlin/.../spatial/CornerOrdering.kt` | Clockwise corner ordering for markers |
| `core-ar/src/test/kotlin/.../spatial/CornerOrderingTest.kt` | Unit tests for corner ordering |
| `docs/sprints/AR_S1_CORE_AR_02_CLOSEOUT.md` | This closeout document |

### Modified Files

| File | Change |
|------|--------|
| `feature-arview/build.gradle.kts` | Added `implementation(project(":core-ar"))` |
| `feature-arview/.../marker/RealMarkerDetector.kt` | Updated imports to `core.ar.spatial` |
| `feature-arview/.../pose/MarkerPoseEstimator.kt` | Updated imports to `core.ar.spatial` |
| `feature-arview/src/test/.../marker/RealMarkerDetectorTest.kt` | Updated imports to `core.ar.spatial` |
| `docs/FILE_OVERVIEW.md` | Added core-ar/spatial to project structure and quick reference |
| `docs/architecture/core-ar_boundary.md` | Added "Moved Spatial Helpers" section |

### Deleted Files/Directories

| Path | Reason |
|------|--------|
| `feature-arview/.../geometry/` | Empty after files moved to core-ar |

## Package Changes

- **Old package:** `com.example.arweld.feature.arview.geometry`
- **New package:** `com.example.arweld.core.ar.spatial`

## Classes Moved

1. **Point2f** — Pure Kotlin 2D point type for JVM-compatible geometry operations. Includes:
   - `data class Point2f(val x: Float, val y: Float)` with `ZERO` constant
   - Extension functions: `PointF.toPoint2f()`, `Point2f.toPointF()`
   - List conversion helpers: `List<PointF>.toPoint2fList()`, `List<Point2f>.toPointFList()`

2. **orderCornersClockwiseFromTopLeft()** — Orders marker corners clockwise from top-left:
   - Computes centroid, sorts by angle
   - Rotates so top-left is first
   - Verifies clockwise order via cross product

## What Was NOT Moved

- **ArCoreMappers.kt** — Stays in `feature-arview` as it bridges ARCore-specific types to domain types
- **Session manager / detector / estimator** — Will be moved in subsequent tasks
- **Smoothing/refine/drift logic** — Out of scope per task constraints

## Verification Commands

```bash
# Verify core-ar compiles with new spatial package
./gradlew :core-ar:assembleDebug

# Run core-ar unit tests
./gradlew :core-ar:testDebugUnitTest

# Verify feature-arview compiles with updated imports
./gradlew :feature-arview:assembleDebug

# Verify app still compiles
./gradlew :app:assembleDebug
```

## Acceptance Criteria Met

- [x] `Point2f` and `CornerOrdering` moved to `core-ar/src/main/kotlin/.../spatial/`
- [x] Imports updated in `RealMarkerDetector.kt`, `MarkerPoseEstimator.kt`, `RealMarkerDetectorTest.kt`
- [x] `feature-arview` depends on `core-ar`
- [x] Unit test added: `CornerOrderingTest.kt`
- [x] No functional changes — only package relocations
- [x] Documentation updated: `FILE_OVERVIEW.md`, `core-ar_boundary.md`

## Known Risks / Technical Debt

1. **Domain Types Still in core-domain**: The `Pose3D`, `Vector3`, `Quaternion`, and `CameraIntrinsics` classes remain in `core-domain/spatial/`. These are domain-level spatial types used for alignment events and evidence, which is appropriate. `core-ar` spatial helpers are distinct engine-level utilities.

2. **Existing Test Coverage**: The moved `orderCornersClockwiseFromTopLeft` function was already tested in `RealMarkerDetectorTest.kt`. New explicit tests added in `core-ar` for better isolation.

## Next Steps

1. **AR_S1_03+**: Continue migrating AR engine components to `core-ar`
2. Consider moving `MarkerDetector` interface and `DetectedMarker` to `core-ar` when appropriate
3. Pose estimation may migrate to `core-ar` in future tasks

## Constraints Observed

- Did NOT move session manager / detector / estimator (per task constraints)
- Did NOT touch smoothing/refine/drift logic (per task constraints)
- Did NOT change public API signatures (only package paths)
