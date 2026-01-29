# AR Sprint 1 / Task 05 — Move Pose Estimator (PnP) into :core-ar Closeout

**Task:** Move pose estimation (PnP) into `:core-ar` (no math changes)
**Status:** Complete
**Date:** 2026-03-01

## Summary

Moved `MarkerPoseEstimator` and its unit test into `:core-ar` so the AR engine owns pose estimation logic while `feature-arview` keeps the wiring intact. Behavior remains unchanged; no numeric thresholds or alignment logic were modified.

## What Changed

### Moved Files

| Original Location | New Location |
|-------------------|--------------|
| `feature-arview/.../pose/MarkerPoseEstimator.kt` | `core-ar/.../pose/MarkerPoseEstimator.kt` |
| `feature-arview/src/test/.../pose/MarkerPoseEstimatorTest.kt` | `core-ar/src/test/.../pose/MarkerPoseEstimatorTest.kt` |

### Modified Files

| File | Change |
|------|--------|
| `feature-arview/.../arcore/ARViewController.kt` | Updated estimator import to `core-ar` |
| `core-ar/build.gradle.kts` | Added `core-domain` dependency for spatial math types |
| `docs/MODULES.md` | Documented core-ar dependency update + pose estimator move |
| `docs/FILE_OVERVIEW.md` | Updated estimator location notes |
| `docs/architecture/core-ar_boundary.md` | Noted pose estimation move and spatial-type dependency |
| `docs/sprints/AR_S1_CORE_AR_05_CLOSEOUT.md` | This closeout document |

## Package Changes

- **Old package:** `com.example.arweld.feature.arview.pose`
- **New package:** `com.example.arweld.core.ar.pose`

## Tests

- `MarkerPoseEstimatorTest` now runs from `core-ar` and continues to validate deterministic pose properties for fixed intrinsics and corners.

## Acceptance Criteria Met

- [x] Pose estimator moved into `core-ar`
- [x] Feature module imports updated (no estimator implementation in `feature-arview`)
- [x] Unit test retained and relocated
- [x] Documentation updated (MODULES, FILE_OVERVIEW, core-ar boundary, closeout)

## Constraints Observed

- PnP math and thresholds unchanged
- Multi-marker refiner left in `feature-arview`
- Alignment behavior unchanged
