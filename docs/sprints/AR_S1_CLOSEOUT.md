# AR Sprint 1 Closeout (Authoritative)

This document is the single source of truth for AR Sprint 1 closeout details and consolidates
all prior per-task closeout notes. It supersedes legacy closeout fragments in
`docs/sprints/AR_S1_CORE_AR_0X_CLOSEOUT.md`.

**Scope:** P1-AR-S1-01 through P1-AR-S1-14

**Sources consolidated:**
- `docs/architecture/core-ar_boundary.md`
- Legacy closeout files in `docs/sprints/`

**CI coverage note:** The CI workflow runs `koverHtmlReport` and `koverXmlReport` because merged Kover tasks are not registered in this build. If merged coverage is added later, update the workflow to use the merged tasks instead.

> Dates are listed only when recorded in the original closeout notes.

---

## P1-AR-S1-01 — Create :core-ar module (initial boundary + wiring)

**Status:** Complete
**Date:** TBD

### Summary
- Created the `:core-ar` Gradle module as an Android library with boundary rules and initial API placeholder.
- Documented boundary rules and updated module/structure references.

### Moved/Owned Paths
- `core-ar/build.gradle.kts`
- `core-ar/src/main/AndroidManifest.xml`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/ArEngine.kt`
- `docs/architecture/core-ar_boundary.md`
- `settings.gradle.kts`
- `docs/MODULES.md`
- `docs/FILE_OVERVIEW.md`

### Commands Run
- `./gradlew :core-ar:assembleDebug`
- `./gradlew :app:assembleDebug`
- `./gradlew s1QualityGate` (optional)

---

## P1-AR-S1-02 — Move math/spatial helpers into :core-ar

**Status:** Complete
**Date:** TBD

### Summary
- Relocated AR math/spatial helpers from `feature-arview` into `core-ar` without behavior changes.
- Added unit coverage for corner ordering and updated feature imports.

### Moved/Owned Paths
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/spatial/Point2f.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/spatial/CornerOrdering.kt`
- `core-ar/src/test/kotlin/com/example/arweld/core/ar/spatial/CornerOrderingTest.kt`
- `feature-arview/build.gradle.kts`
- `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/marker/RealMarkerDetector.kt`
- `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/pose/MarkerPoseEstimator.kt`
- `feature-arview/src/test/kotlin/com/example/arweld/feature/arview/marker/RealMarkerDetectorTest.kt`
- `docs/FILE_OVERVIEW.md`
- `docs/architecture/core-ar_boundary.md`

### Commands Run
- `./gradlew :core-ar:assembleDebug`
- `./gradlew :core-ar:testDebugUnitTest`
- `./gradlew :feature-arview:assembleDebug`
- `./gradlew :app:assembleDebug`

---

## P1-AR-S1-03 — Move ARCore session manager into :core-ar

**Status:** Complete
**Date:** TBD

### Summary
- Moved `ARCoreSessionManager` into `core-ar` and updated feature references.
- Added an instrumentation smoke test to ensure AR screen launch stability.

### Moved/Owned Paths
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/arcore/ARCoreSessionManager.kt`
- `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARViewController.kt`
- `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARSceneRenderer.kt`
- `app/src/androidTest/java/com/example/arweld/ui/ar/ARViewSmokeTest.kt`
- `docs/MODULES.md`
- `docs/FILE_OVERVIEW.md`
- `docs/architecture/core-ar_boundary.md`

### Commands Run
- `./gradlew :core-ar:assembleDebug`
- `./gradlew :feature-arview:compileDebugKotlin`

---

## P1-AR-S1-04 — Move marker detection pipeline into :core-ar

**Status:** Complete
**Date:** TBD

### Summary
- Moved marker detector interface, model, and ML Kit implementation into `core-ar`.
- Updated feature and tests to consume the new core interfaces with no behavior change.

### Moved/Owned Paths
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/marker/MarkerDetector.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/marker/RealMarkerDetector.kt`
- `core-ar/src/test/kotlin/com/example/arweld/core/ar/marker/RealMarkerDetectorTest.kt`
- `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARViewController.kt`
- `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/marker/SimulatedMarkerDetector.kt`
- `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/marker/StubMarkerDetector.kt`
- `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/pose/MarkerPoseEstimator.kt`
- `feature-arview/src/test/kotlin/com/example/arweld/feature/arview/pose/MarkerPoseEstimatorTest.kt`
- `core-ar/build.gradle.kts`
- `feature-arview/build.gradle.kts`
- `app/src/androidTest/java/com/example/arweld/ui/ar/ARViewSmokeTest.kt`
- `docs/MODULES.md`
- `docs/FILE_OVERVIEW.md`
- `docs/architecture/core-ar_boundary.md`

### Commands Run
- `./gradlew :core-ar:assembleDebug`
- `./gradlew :core-ar:testDebugUnitTest`
- `./gradlew :feature-arview:assembleDebug`
- `./gradlew :app:connectedAndroidTest`

---

## P1-AR-S1-05 — Move pose estimator (PnP) into :core-ar

**Status:** Complete
**Date:** TBD

### Summary
- Moved `MarkerPoseEstimator` and its unit test into `core-ar` with no math changes.
- Updated feature wiring and documented the dependency change.

### Moved/Owned Paths
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/pose/MarkerPoseEstimator.kt`
- `core-ar/src/test/kotlin/com/example/arweld/core/ar/pose/MarkerPoseEstimatorTest.kt`
- `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARViewController.kt`
- `core-ar/build.gradle.kts`
- `docs/MODULES.md`
- `docs/FILE_OVERVIEW.md`
- `docs/architecture/core-ar_boundary.md`

### Commands Run
- `./gradlew :core-ar:compileDebugKotlin`

---

## P1-AR-S1-06 — Align pose types with core-structural dependency cleanup

**Status:** Complete

### Summary
- Relocated shared pose types to `core-structural` so `core-ar` can consume them without a `core-domain` dependency.
- Kept feature wiring intact while removing the redundant dependency edge.

### Moved/Owned Paths
- `core-structural/src/main/kotlin/com/example/arweld/core/domain/spatial/PoseTypes.kt`

### Commands Run
- `./gradlew :core-ar:compileDebugKotlin`
- `./gradlew :feature-arview:compileDebugKotlin`

---

## P1-AR-S1-07 — Move MultiMarkerPoseRefiner into :core-ar (no logic changes)

**Status:** Complete
**Date:** TBD

### Summary
- Confirmed `MultiMarkerPoseRefiner` is owned by `:core-ar` and referenced from `feature-arview` without logic changes.
- Ensured unit coverage for deterministic output lives in `core-ar`.

### Moved/Owned Paths
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/pose/MultiMarkerPoseRefiner.kt`
- `core-ar/src/test/kotlin/com/example/arweld/core/ar/pose/MultiMarkerPoseRefinerTest.kt`

### Commands Run
- `./gradlew :core-ar:compileDebugKotlin`
- `./gradlew :core-ar:testDebugUnitTest`
- `./gradlew :feature-arview:compileDebugKotlin`
- `./gradlew :app:assembleDebug`
- `./gradlew testDebugUnitTest`

---

## P1-AR-S1-08 — Move DriftMonitor + TrackingQuality state into :core-ar (no logic changes)

**Status:** Complete
**Date:** TBD

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
**Date:** TBD

### Summary
- Introduced `ArCaptureService` (API v0) in `core-ar` and moved SurfaceView PixelCopy capture there.
- Updated `feature-arview` to call the capture service and map capture metadata back to `ArScreenshotMeta` for QC evidence storage.
- Added a smoke instrumentation test for the capture service wiring using a test SurfaceView host.

### Moved/Owned Paths
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/ArCaptureService.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/ArCaptureServiceRegistry.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/capture/SurfaceViewArCaptureService.kt`
- `core-ar/src/androidTest/kotlin/com/example/arweld/core/ar/capture/SurfaceViewArCaptureServiceTest.kt`

### Commands Run
- `./gradlew :core-ar:compileDebugKotlin`
- `./gradlew :core-ar:testDebugUnitTest`
- `./gradlew :core-ar:connectedDebugAndroidTest`

---

## P1-AR-S1-10 — Rewire DI: bind :core-ar interfaces via app/di (no logic changes)

**Status:** Complete
**Date:** TBD

### Summary
- Introduced core-ar API factories and bound them in the app DI module without adding Hilt to core-ar.
- Updated feature-arview to obtain an `ArViewControllerFactory` via Hilt entry points.
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
- `./gradlew :feature-arview:compileDebugKotlin`
- `./gradlew :app:assembleDebug`
- `./gradlew :app:connectedDebugAndroidTest --tests "com.example.arweld.ui.ar.ARViewNavigationSmokeTest"`

---

## P1-AR-S1-10a — Fix Hilt wiring + CI managed device gating (no logic changes)

**Status:** Complete
**Date:** TBD

### Summary
- Ensured the app module depends on `:core-ar` so Hilt/KSP can resolve AR factory bindings.
- Gated managed-device instrumentation tests behind a manual CI dispatch to avoid x86_64 accel requirements.

### Moved/Owned Paths
- `app/build.gradle.kts`
- `.github/workflows/instrumentation-smoke.yml`
- `docs/architecture/core-ar_boundary.md`

### Commands Run
- `./gradlew :app:compileDebugKotlin`
- `./gradlew :app:kspDebugKotlin`
- `./gradlew :app:assembleDebug`
- `./gradlew testDebugUnitTest`

---

## P1-AR-S1-10b — Fix androidTest Hilt wiring + disable managed devices in CI (no logic changes)

**Status:** Complete
**Date:** TBD

### Summary
- Ensured androidTest Hilt modules resolve by keeping TestInstallIn wired and adding ARCore to the androidTest classpath.
- Disabled managed-device instrumentation smoke in CI by default to avoid x86_64 acceleration requirements.

### Moved/Owned Paths
- `app/src/androidTest/java/com/example/arweld/di/FakeScannerModule.kt`
- `app/build.gradle.kts`
- `.github/workflows/android-ci.yml`

### Commands Run
- `./gradlew :app:kspDebugAndroidTestKotlin`
- `./gradlew :app:compileDebugAndroidTestKotlin`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:assembleDebug`

---

## P1-AR-S1-11 — Add AR screen launch smoke instrumentation test (no AR detection required)

**Status:** Complete
**Date:** TBD

### Summary
- Added a test-only Hilt module that swaps in a fake AR session manager and simulated marker detector.
- Expanded AR view instrumentation smoke tests to navigate through AppNavigation and assert the AR screen renders without relying on ARCore.

### Moved/Owned Paths
- `app/src/androidTest/java/com/example/arweld/ui/ar/ArCoreTestModule.kt`
- `app/src/androidTest/java/com/example/arweld/ui/ar/ARViewSmokeTest.kt`
- `app/src/androidTest/java/com/example/arweld/ui/ar/ARViewNavigationSmokeTest.kt`

### Commands Run
- `./gradlew :core-ar:compileDebugKotlin`
- `./gradlew :feature-arview:compileDebugKotlin`
- `./gradlew :app:assembleDebug`
- `./gradlew :app:connectedDebugAndroidTest --tests "com.example.arweld.ui.ar.ARViewSmokeTest"`
- `./gradlew :app:connectedDebugAndroidTest --tests "com.example.arweld.ui.ar.ARViewNavigationSmokeTest"`

---

## P1-AR-S1-12 — Add Gradle guard for :core-ar dependency boundaries

**Status:** Complete
**Date:** TBD

### Summary
- Added a Gradle verification task to fail if `:core-ar` declares direct dependencies on `:core-domain` or `:core-data`.
- Wired the guard into sprint quality gate tasks so CI enforces the boundary.

### Moved/Owned Paths
- `build.gradle.kts` (task `verifyCoreArBoundaries`)

### Commands Run
- `./gradlew verifyCoreArBoundaries`

---

## P1-AR-S1-13 — Closeout record not found

**Status:** Not documented

### Summary
- No closeout record found in-repo for this task. Confirm scope, implementation, and verification
  commands before considering the task complete.

### Moved/Owned Paths
- Not recorded.

### Commands Run
- Not recorded.

---

## P1-AR-S1-14 — Closeout record not found

**Status:** Not documented

### Summary
- No closeout record found in-repo for this task. Confirm scope, implementation, and verification
  commands before considering the task complete.

### Moved/Owned Paths
- Not recorded.

### Commands Run
- Not recorded.
