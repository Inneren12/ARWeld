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

## Moved Spatial Helpers (AR Sprint 1 / Task 02)

The following spatial/math helper classes were moved from `feature-arview` into `core-ar` to enable AR engine code to live in the core module:

### Relocated Classes

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `Point2f` | `feature-arview/.../geometry/Point2f.kt` | `core-ar/.../spatial/Point2f.kt` |
| `orderCornersClockwiseFromTopLeft()` | `feature-arview/.../geometry/CornerOrdering.kt` | `core-ar/.../spatial/CornerOrdering.kt` |

### Package Change

- **Old package:** `com.example.arweld.feature.arview.geometry`
- **New package:** `com.example.arweld.core.ar.spatial`

### What Was Moved

1. **Point2f.kt** — Pure Kotlin 2D point class for JVM-compatible geometry operations, plus conversion functions to/from Android `PointF`.

2. **CornerOrdering.kt** — Algorithm to order marker corners clockwise from top-left. Used by marker detection and pose estimation.

### What Was NOT Moved

- **ArCoreMappers.kt** — Stays in `feature-arview` as it bridges ARCore-specific types to domain types
- **Session/detector/estimator** — Will be moved in subsequent tasks
- **Smoothing/refine/drift logic** — Out of scope for this task

## Session Manager Moved (AR Sprint 1 / Task 03)

The ARCore session lifecycle manager now lives in `core-ar` so AR session setup can be shared without coupling to feature UI code.

### Relocated Class

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `ARCoreSessionManager` | `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARCoreSessionManager.kt` | `core-ar/src/main/kotlin/com/example/arweld/core/ar/arcore/ARCoreSessionManager.kt` |

### Notes

- Behavior is unchanged: lazy session creation on resume, safe pause/destroy, and display-geometry updates remain identical.
- `feature-arview` now depends on `core-ar` for session lifecycle while keeping UI/lifecycle wiring in the feature module.

### Migration Notes

- Consuming code in `feature-arview` updated imports from `feature.arview.geometry` → `core.ar.spatial`
- `feature-arview/build.gradle.kts` now includes `implementation(project(":core-ar"))`
- Unit tests added in `core-ar/src/test/.../spatial/CornerOrderingTest.kt`
- No behavior changes; move-only refactoring

## Marker Pipeline Moved (AR Sprint 1 / Task 04)

The marker detection interface and default ML Kit-backed implementation now live in `core-ar` so AR engine code owns the detection pipeline.

### Relocated Classes

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `MarkerDetector` | `feature-arview/.../marker/MarkerDetector.kt` | `core-ar/.../marker/MarkerDetector.kt` |
| `DetectedMarker` | `feature-arview/.../marker/MarkerDetector.kt` | `core-ar/.../marker/MarkerDetector.kt` |
| `RealMarkerDetector` | `feature-arview/.../marker/RealMarkerDetector.kt` | `core-ar/.../marker/RealMarkerDetector.kt` |

### Notes

- `feature-arview` keeps `SimulatedMarkerDetector` for debug-only triggers and simply depends on the core interface.
- No behavior changes: detection cadence, thresholds, and rotation handling remain identical.

## Capture Service Added (AR Sprint 1 / Task 09)

The AR screenshot capture flow now lives in `core-ar` as a thin service API. The service performs
PixelCopy from the AR `SurfaceView`, writes a PNG file under the app-scoped evidence directory, and
returns the file `Uri` plus size metadata. Tracking/alignment metadata is supplied by the feature
layer and attached to the result to keep `core-ar` free of domain dependencies.

### API Summary (v0)

- `ArCaptureService.captureScreenshot(request)` returns `ArCaptureResult` with file `Uri`, width,
  height, timestamp, and optional `ArCaptureMeta`.
- `createSurfaceViewCaptureService(surfaceView)` constructs the default implementation.
- `ArCaptureServiceRegistry` exposes the active capture service instance when needed.

### Owned Paths

- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/ArCaptureService.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/ArCaptureServiceRegistry.kt`
- `core-ar/src/main/kotlin/com/example/arweld/core/ar/capture/SurfaceViewArCaptureService.kt`

### Integration Notes

- `feature-arview` calls the capture service and maps `ArCaptureMeta` to domain `ArScreenshotMeta`
  when saving evidence via `core-data`.

## Future Considerations

As the AR system evolves, additional APIs may be added to `core-ar`:

- `MeshLoader` — Load member meshes from StructuralModel
- `PoseEstimator` — Compute world-to-model transforms
- `FrameCapture` — Capture AR frames for evidence
- `TrackingState` — Expose tracking quality metrics

All additions must follow the same boundary rules: no domain/data/UI dependencies.

## Pose Estimation + PnP moved in PR P1-AR-S1-06

The pose estimation and PnP pipeline now live fully in `core-ar`, and spatial math types were relocated to `core-structural` so `core-ar` no longer depends on `core-domain`.

### Relocated Classes

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `MultiMarkerPoseRefiner` | `feature-arview/.../pose/MultiMarkerPoseRefiner.kt` | `core-ar/.../pose/MultiMarkerPoseRefiner.kt` |
| `PoseTypes` (Pose3D/Vector3/Quaternion/CameraIntrinsics) | `core-domain/.../spatial/PoseTypes.kt` | `core-structural/.../spatial/PoseTypes.kt` |

### Rationale

- Keeps pose estimation logic in the core AR engine for reuse and testing.
- Removes `core-ar`'s dependency on `core-domain` while preserving shared spatial types via `core-structural`.
- Maintains runtime behavior: only module/package wiring moved.

## Multi-marker refine moved in P1-AR-S1-07

The multi-marker refinement logic is now owned by `core-ar`, with behavior unchanged.

### Moved Files

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `MultiMarkerPoseRefiner` | `feature-arview/.../pose/MultiMarkerPoseRefiner.kt` | `core-ar/.../pose/MultiMarkerPoseRefiner.kt` |

### Notes

- Behavior unchanged; move-only refactor.
- `feature-arview` consumes the refiner via `core-ar` imports.

## Drift + tracking state moved in P1-AR-S1-08

The drift monitoring and tracking-quality state models now live in `core-ar` so the AR engine owns tracking health without UI coupling.

### Moved Files

| Class | Original Location | New Location |
|-------|-------------------|--------------|
| `DriftMonitor` | `feature-arview/.../alignment/DriftMonitor.kt` | `core-ar/.../alignment/DriftMonitor.kt` |
| `TrackingQuality` | `feature-arview/.../tracking/TrackingQuality.kt` | `core-ar/.../tracking/TrackingQuality.kt` |
| `TrackingStatus` | `feature-arview/.../tracking/TrackingQuality.kt` | `core-ar/.../tracking/TrackingQuality.kt` |
| `PerformanceMode` | `feature-arview/.../tracking/PerformanceMode.kt` | `core-ar/.../tracking/PerformanceMode.kt` |

### Notes

- No logic changes; thresholds, smoothing, and state transitions remain identical.
- `feature-arview` continues to own UI mapping (colors, strings, badges) using the core state models.
