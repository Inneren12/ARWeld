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

---

## P1-AR-S1-10 — Rewire DI: bind :core-ar interfaces via app/di (no logic changes)

**Status:** Complete
**Date:** 2026-03-02

### Summary

- Introduced core-ar API factories (session manager, marker detector, pose/refiner, drift, capture)
  and bound them in the app DI module without adding Hilt to core-ar.
- Updated feature-arview to obtain an `ArViewControllerFactory` via Hilt entry points so it no
  longer constructs core-ar implementations directly.
- Added a navigation smoke instrumentation test to ensure the AR screen initializes through DI.

### Moved/Owned Paths

- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/ArSessionManager.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/MarkerDetectorFactory.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/ArCaptureServiceFactory.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/MarkerPoseEstimatorFactory.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/MultiMarkerPoseRefinerFactory.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/DriftMonitorFactory.kt`
- `app/src/main/kotlin/com/example/arweld/di/ArCoreModule.kt`
- `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ArViewControllerFactory.kt`
- `app/src/androidTest/java/com/example/arweld/ui/ar/ARViewNavigationSmokeTest.kt`

### Commands Run

- Not run (not executed in this change)

---

## P1-AR-S1-11 — Add AR screen launch smoke instrumentation test (no AR detection required)

**Status:** Complete
**Date:** 2026-03-02

### Summary

- Added a test-only Hilt module that swaps in a fake AR session manager and simulated marker detector.
- Expanded the AR view instrumentation smoke tests to navigate through AppNavigation and assert the AR screen renders without relying on ARCore.

### Moved/Owned Paths

- `app/src/androidTest/java/com/example/arweld/ui/ar/ArCoreTestModule.kt`
- `app/src/androidTest/java/com/example/arweld/ui/ar/ARViewSmokeTest.kt`
- `app/src/androidTest/java/com/example/arweld/ui/ar/ARViewNavigationSmokeTest.kt`

### Commands Run

- `./gradlew :app:connectedDebugAndroidTest`

---

## P1-AR-S1-12 — Add Gradle guard for :core-ar dependency boundaries

**Status:** Complete
**Date:** 2026-03-02

### Summary

- Added a Gradle verification task that fails if `:core-ar` declares direct project dependencies on
  `:core-domain` or `:core-data`.
- Wired the guard into the existing sprint quality gate tasks so CI enforces the boundary.

### Commands Run

- `./gradlew verifyCoreArBoundaries`

---
