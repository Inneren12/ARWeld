# AR Sprint 1 Closeout

This document accumulates sprint-closeout notes for AR Sprint 1 tasks.

---

## P1-AR-S1-07 — Move MultiMarkerPoseRefiner into :core-ar (no logic changes)

**Status:** Complete
**Date:** 2026-03-02

### Summary

- Confirmed `MultiMarkerPoseRefiner` is owned by `:core-ar` and referenced from `feature-arview` without logic changes.
- Added a smoke-level unit test in `core-ar` to assert deterministic output for a fixed input set.

### Moved/Owned Paths

- `core-ar/src/main/kotlin/com/example/arweld/core/ar/pose/MultiMarkerPoseRefiner.kt`
- `core-ar/src/test/kotlin/com/example/arweld/core/ar/pose/MultiMarkerPoseRefinerTest.kt`

### Commands Run

- `./gradlew :core-ar:compileDebugKotlin`
- `./gradlew :feature-arview:compileDebugKotlin`
- `./gradlew :app:assembleDebug`
- `./gradlew testDebugUnitTest`

---

## P1-AR-S1-08 — Move DriftMonitor + TrackingQuality state into :core-ar (no logic changes)

**Status:** Complete
**Date:** 2026-03-02

### Summary

- Moved drift monitoring and tracking-quality state models into `:core-ar` to keep engine state in the core module.
- Updated feature-arview to consume the moved models while keeping UI mappings local.
- Added a unit test for drift monitor determinism.

### Moved/Owned Paths

- `core-ar/src/main/kotlin/com/example/arweld/core/ar/alignment/DriftMonitor.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/tracking/TrackingQuality.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/tracking/PerformanceMode.kt`
- `core-ar/src/test/kotlin/com/example/arweld/core/ar/alignment/DriftMonitorTest.kt`

### Commands Run

- `./gradlew :core-ar:compileDebugKotlin`
- `./gradlew :feature-arview:compileDebugKotlin`
- `./gradlew :app:assembleDebug`
- `./gradlew testDebugUnitTest`

---

## P1-AR-S1-09 — Move AR screenshot capture utility into :core-ar (no logic changes)

**Status:** Complete
**Date:** 2026-03-02

### Summary

- Introduced `ArCaptureService` (API v0) in `core-ar` and moved SurfaceView PixelCopy capture there.
- Updated `feature-arview` to call the capture service and map capture metadata back to
  `ArScreenshotMeta` for QC evidence storage.
- Added a smoke instrumentation test for the capture service wiring using a test SurfaceView host.

### Moved/Owned Paths

- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/ArCaptureService.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/ArCaptureServiceRegistry.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/capture/SurfaceViewArCaptureService.kt`
- `core-ar/src/androidTest/kotlin/com/example/arweld/core/ar/capture/SurfaceViewArCaptureServiceTest.kt`

### Commands Run

- Not run (not executed in this change)
