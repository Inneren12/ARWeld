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

## Forbidden Dependencies

| Dependency | Forbidden | Reason |
|------------|-----------|--------|
| `core-domain` | **NO** | Domain logic belongs in core-domain; core-ar is rendering-only |
| `core-data` | **NO** | Data/persistence belongs in core-data; core-ar is stateless |
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
    // implementation(project(":core-domain"))  // NO!
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
    └─> core-data (for EventRepository to log alignment)
```

## Future Considerations

As the AR system evolves, additional APIs may be added to `core-ar`:

- `MeshLoader` — Load member meshes from StructuralModel
- `PoseEstimator` — Compute world-to-model transforms
- `FrameCapture` — Capture AR frames for evidence
- `TrackingState` — Expose tracking quality metrics

All additions must follow the same boundary rules: no domain/data/UI dependencies.
