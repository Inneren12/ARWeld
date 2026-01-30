# core-ar Module Boundary Rules

This document defines the strict boundary rules for the `:core-ar` module. These rules ensure clean separation of concerns and prevent architectural drift.

## Purpose

The `core-ar` module provides the core AR engine interface and rendering primitives for ARWeld. It bridges ARCore/Filament to structural model data from `core-structural`, enabling AR visualization without coupling to domain/data layers or UI frameworks.

## Allowed Dependencies

| Dependency | Allowed | Notes |
|------------|---------|-------|
| `core-structural` | **YES** | For StructuralModel, MemberMeshes, Node reference frames |
| ARCore (`com.google.ar:core`) | **YES** | AR session and frame data |
| Filament (`filament-android`, `gltfio-android`, `filament-utils-android`) | **YES** | 3D rendering |
| AndroidX Core (`androidx.core:core-ktx`) | **YES** | Minimal Android utilities |
| Kotlinx Coroutines | **YES** | Async operations |
| Kotlinx Serialization | **YES** | JSON serialization for model data |
| ML Kit Barcode (`com.google.mlkit:barcode-scanning`) | **YES** | Marker detection backend |

**PoseTypes location:** `Pose3D`, `Vector3`, `Quaternion`, and `CameraIntrinsics` live in
`core-structural` (package `com.example.arweld.core.domain.spatial`), so `core-ar` imports them
from `core-structural` without depending on `core-domain`.

## Forbidden Dependencies

| Dependency | Forbidden | Reason |
|------------|-----------|--------|
| `core-data` | **NO** | Data/persistence belongs in core-data; core-ar is stateless |
| `core-domain` | **NO** | Domain models and reducers belong in core-domain; core-ar consumes only core-structural spatial types |
| `core-auth` | **NO** | Auth concerns belong in feature layer |
| Any `feature-*` module | **NO** | Features depend on core, not vice versa |
| Jetpack Compose | **NO** | UI composition belongs in feature-arview |
| Navigation Compose | **NO** | Navigation belongs in app/feature modules |
| Hilt/Dagger | **NO** | DI wiring belongs in feature/app modules |
| Room/Database | **NO** | Persistence belongs in core-data |

## Design Rationale

1. **Separation of Concerns**: AR rendering logic should be independent of business domain concepts (WorkItem, Event, Evidence). The structural model (nodes, members, geometry) is sufficient for rendering.

2. **Testability**: By excluding DI frameworks and domain dependencies, `core-ar` can be unit tested with mock StructuralModel data without complex setup.

3. **Reusability**: The AR engine can potentially be reused in other contexts (e.g., standalone model viewer) without pulling in the full ARWeld architecture.

4. **Build Performance**: Minimal dependencies mean faster incremental builds when only AR rendering changes.

## Boundary Verification

To verify the module follows these rules, check `core-ar/build.gradle.kts`:

```kotlin
dependencies {
    // ALLOWED
    implementation(project(":core-structural"))
    implementation(libs.google.ar.core)
    implementation(libs.filament.android)
    implementation(libs.filament.gltfio.android)
    implementation(libs.filament.utils.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // FORBIDDEN - these must NOT appear:
    // implementation(project(":core-data"))    // NO!
    // implementation(project(":core-auth"))    // NO!
    // implementation(libs.hilt.android)        // NO!
    // implementation(libs.androidx.compose.*)  // NO!
}
```

## Boundary Verification: automated guard

An automated Gradle guard enforces the forbidden dependency rules for `:core-ar` by checking the
`implementation`, `api`, `compileOnly`, and `runtimeOnly` configurations for direct project
dependencies on `:core-domain` or `:core-data`. The guard runs via:

```
./gradlew verifyCoreArBoundaries
```

This task is wired into the sprint quality gate tasks so CI will fail if someone adds a forbidden
dependency to `:core-ar`.

## Integration Pattern

The `feature-arview` module should depend on `core-ar` and provide:
- Compose UI wrapper for AR surface
- Lifecycle management (pause/resume/destroy)
- DI wiring for ArEngine implementations
- Integration with domain events (alignment events via core-domain)

```
feature-arview
    ├─> core-ar (AR engine interface + rendering)
    ├─> core-domain (for AR alignment events, Evidence models)
    ├─> core-structural (for shared spatial math types)
    └─> core-data (for EventRepository to log alignment)
```

## DI wiring

Core AR types expose constructor-friendly interfaces or factories in `core-ar/api`, and the app
module owns the concrete bindings. `feature-arview` requests these abstractions via Hilt
entry points so it remains UI-layer only while the app provides implementation wiring.

**Flow:**

1. `core-ar` defines factories such as `ArSessionManagerFactory`, `MarkerDetectorFactory`,
   `MarkerPoseEstimatorFactory`, `MultiMarkerPoseRefinerFactory`, `DriftMonitorFactory`, and
   `ArCaptureServiceFactory` in `core-ar/api`.
2. `core-ar` provides default implementations (e.g., `ARCoreSessionManager`, `RealMarkerDetector`,
   `SurfaceViewArCaptureService`) in non-API packages.
3. `app` binds the factories to implementations in `app/di/ArCoreModule.kt` and exposes an
   `ArViewControllerFactory` for `feature-arview` to consume via an `EntryPoint`.

This keeps `core-ar` free of Hilt while centralizing wiring in the app module, with no behavior
changes to the AR pipeline.

## Sprint 1 task log (core-ar boundary changes)

Each Sprint 1 task is recorded once below with PR id/name, moved-file table, and verification
commands. For the consolidated closeout rollup, see
[`docs/sprints/AR_S1_CLOSEOUT.md`](../sprints/AR_S1_CLOSEOUT.md).

### P1-AR-S1-01 — Create :core-ar module (initial boundary + wiring)

**PR:** P1-AR-S1-01 — Create :core-ar module

**Moved/Owned Files**

| Original Location | New Location |
|-------------------|--------------|
| _New module_ | `core-ar/build.gradle.kts` |
| _New module_ | `core-ar/src/main/AndroidManifest.xml` |
| _New module_ | `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/ArEngine.kt` |
| _New module_ | `docs/architecture/core-ar_boundary.md` |

**Verification commands (documented):**
- `./gradlew :core-ar:assembleDebug`

### P1-AR-S1-02 — Move math/spatial helpers into :core-ar

**PR:** P1-AR-S1-02 — Move Math/Spatial Helpers to :core-ar

**Moved Files**

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `Point2f` | `feature-arview/.../geometry/Point2f.kt` | `core-ar/.../spatial/Point2f.kt` |
| `orderCornersClockwiseFromTopLeft()` | `feature-arview/.../geometry/CornerOrdering.kt` | `core-ar/.../spatial/CornerOrdering.kt` |

**Verification commands (documented):**
- `./gradlew :core-ar:assembleDebug`
- `./gradlew :core-ar:testDebugUnitTest`

### P1-AR-S1-03 — Move ARCore session manager into :core-ar

**PR:** P1-AR-S1-03 — Move ARCore Session Manager to :core-ar

**Moved Files**

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `ARCoreSessionManager` | `feature-arview/.../arcore/ARCoreSessionManager.kt` | `core-ar/.../arcore/ARCoreSessionManager.kt` |

**Verification commands (documented):**
- `./gradlew :core-ar:assembleDebug`
- `./gradlew :feature-arview:compileDebugKotlin`

### P1-AR-S1-04 — Move marker detection pipeline into :core-ar

**PR:** P1-AR-S1-04 — Move Marker Detection Pipeline to :core-ar

**Moved Files**

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `MarkerDetector` | `feature-arview/.../marker/MarkerDetector.kt` | `core-ar/.../marker/MarkerDetector.kt` |
| `DetectedMarker` | `feature-arview/.../marker/MarkerDetector.kt` | `core-ar/.../marker/MarkerDetector.kt` |
| `RealMarkerDetector` | `feature-arview/.../marker/RealMarkerDetector.kt` | `core-ar/.../marker/RealMarkerDetector.kt` |

**Verification commands (documented):**
- `./gradlew :core-ar:assembleDebug`
- `./gradlew :core-ar:testDebugUnitTest`
- `./gradlew :feature-arview:compileDebugKotlin`

### P1-AR-S1-05 — Move pose estimator (PnP) into :core-ar

**PR:** P1-AR-S1-05 — Move MarkerPoseEstimator into :core-ar

**Moved Files**

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `MarkerPoseEstimator` | `feature-arview/.../pose/MarkerPoseEstimator.kt` | `core-ar/.../pose/MarkerPoseEstimator.kt` |
| `MarkerPoseEstimatorTest` | `feature-arview/src/test/.../pose/MarkerPoseEstimatorTest.kt` | `core-ar/src/test/.../pose/MarkerPoseEstimatorTest.kt` |

**Verification commands (documented):**
- `./gradlew :core-ar:compileDebugKotlin`

### P1-AR-S1-06 — Align pose types with core-structural dependency cleanup

**PR:** P1-AR-S1-06 — Move pose types to core-structural

**Moved/Owned Files**

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `PoseTypes` (Pose3D/Vector3/Quaternion/CameraIntrinsics) | `core-domain/.../spatial/PoseTypes.kt` | `core-structural/.../spatial/PoseTypes.kt` |

**Rationale**
- Keep shared spatial math in `core-structural` so `core-ar` can consume `PoseTypes` without taking a `core-domain` dependency.

**Verification commands (documented):**
- `./gradlew :core-ar:compileDebugKotlin`
- `./gradlew :feature-arview:compileDebugKotlin`

### P1-AR-S1-07 — Move MultiMarkerPoseRefiner into :core-ar

**PR:** P1-AR-S1-07 — Move MultiMarkerPoseRefiner into :core-ar

**Moved Files**

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `MultiMarkerPoseRefiner` | `feature-arview/.../pose/MultiMarkerPoseRefiner.kt` | `core-ar/.../pose/MultiMarkerPoseRefiner.kt` |

**Verification commands (documented):**
- `./gradlew :core-ar:compileDebugKotlin`
- `./gradlew :core-ar:testDebugUnitTest`
- `./gradlew :feature-arview:compileDebugKotlin`

### P1-AR-S1-08 — Move drift + tracking state into :core-ar

**PR:** P1-AR-S1-08 — Move DriftMonitor + TrackingQuality state into :core-ar

**Moved Files**

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `DriftMonitor` | `feature-arview/.../alignment/DriftMonitor.kt` | `core-ar/.../alignment/DriftMonitor.kt` |
| `TrackingQuality` | `feature-arview/.../tracking/TrackingQuality.kt` | `core-ar/.../tracking/TrackingQuality.kt` |
| `TrackingStatus` | `feature-arview/.../tracking/TrackingQuality.kt` | `core-ar/.../tracking/TrackingQuality.kt` |
| `PerformanceMode` | `feature-arview/.../tracking/PerformanceMode.kt` | `core-ar/.../tracking/PerformanceMode.kt` |

**Verification commands (documented):**
- `./gradlew :core-ar:compileDebugKotlin`
- `./gradlew :feature-arview:compileDebugKotlin`

### P1-AR-S1-09 — Move AR screenshot capture into :core-ar

**PR:** P1-AR-S1-09 — Move AR screenshot capture utility into :core-ar

**Moved/Owned Files**

| Original Location | New Location |
|-------------------|--------------|
| `feature-arview/.../capture/SurfaceViewArCaptureService.kt` | `core-ar/.../capture/SurfaceViewArCaptureService.kt` |
| `feature-arview/.../api/ArCaptureService.kt` | `core-ar/.../api/ArCaptureService.kt` |
| `feature-arview/.../api/ArCaptureServiceRegistry.kt` | `core-ar/.../api/ArCaptureServiceRegistry.kt` |

**Capture storage:**
- `filesDir/evidence/ar_screenshots/` (PNG output)

**Verification commands (documented):**
- `./gradlew :core-ar:compileDebugKotlin`

### P1-AR-S1-10 — Rewire DI bindings for :core-ar APIs

**PR:** P1-AR-S1-10 — Rewire DI: bind :core-ar interfaces via app/di

**Moved/Owned Files**

| Original Location | New Location |
|-------------------|--------------|
| _New bindings_ | `app/src/main/kotlin/com/example/arweld/di/ArCoreModule.kt` |
| _New factory_ | `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ArViewControllerFactory.kt` |

**Verification commands (documented):**
- `./gradlew :app:assembleDebug`

### P1-AR-S1-11 — Add AR screen launch smoke instrumentation test

**PR:** P1-AR-S1-11 — Add AR screen launch smoke instrumentation test

**Moved/Owned Files**

| Original Location | New Location |
|-------------------|--------------|
| _New test module_ | `app/src/androidTest/java/com/example/arweld/ui/ar/ArCoreTestModule.kt` |
| _New smoke test_ | `app/src/androidTest/java/com/example/arweld/ui/ar/ARViewSmokeTest.kt` |
| _New smoke test_ | `app/src/androidTest/java/com/example/arweld/ui/ar/ARViewNavigationSmokeTest.kt` |

**Verification commands (documented):**
- `./gradlew :app:connectedDebugAndroidTest`

### P1-AR-S1-12 — Add Gradle guard for :core-ar dependency boundaries

**PR:** P1-AR-S1-12 — Add Gradle guard for :core-ar dependency boundaries

**Moved/Owned Files**

| Original Location | New Location |
|-------------------|--------------|
| _New Gradle task_ | `build.gradle.kts` (`verifyCoreArBoundaries`) |

**Verification commands (documented):**
- `./gradlew verifyCoreArBoundaries`

## Future Considerations

As the AR system evolves, additional APIs may be added to `core-ar`:

- `MeshLoader` — Load member meshes from StructuralModel
- `PoseEstimator` — Compute world-to-model transforms
- `FrameCapture` — Capture AR frames for evidence
- `TrackingState` — Expose tracking quality metrics

All additions must follow the same boundary rules: no domain/data/UI dependencies.
