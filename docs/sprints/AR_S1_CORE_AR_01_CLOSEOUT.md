# AR Sprint 1 / Task 01 — Create :core-ar Module Closeout

**Task:** Create :core-ar module with minimal compile + docs updates
**Status:** Complete
**Date:** 2026-01-29

## Summary

Created the `:core-ar` Gradle module as an Android library with strict boundary rules. The module provides the foundation for AR engine interfaces that bridge ARCore/Filament to structural model data.

## What Changed

### New Files

| File | Description |
|------|-------------|
| `core-ar/build.gradle.kts` | Android library module configuration with ARCore/Filament deps |
| `core-ar/src/main/AndroidManifest.xml` | Minimal manifest for library module |
| `core-ar/src/main/kotlin/com/example/arweld/core/ar/api/ArEngine.kt` | Core AR engine interface (placeholder) |
| `docs/architecture/core-ar_boundary.md` | Boundary rules documentation |
| `docs/sprints/AR_S1_CORE_AR_01_CLOSEOUT.md` | This closeout document |

### Modified Files

| File | Change |
|------|--------|
| `settings.gradle.kts` | Added `include(":core-ar")` |
| `docs/MODULES.md` | Added core:ar to dependency graph, rules, and module description |
| `docs/FILE_OVERVIEW.md` | Added core-ar to project structure and quick reference |

## Boundary Rules Enforced

The `:core-ar` module follows strict dependency boundaries:

**Allowed:**
- `core-structural` (for StructuralModel geometry)
- ARCore, Filament, AndroidX Core, Kotlinx Coroutines/Serialization

**Forbidden:**
- `core-domain`, `core-data`, `core-auth`
- Any `feature-*` module
- Compose UI, Navigation, Hilt/Dagger, Room

See `docs/architecture/core-ar_boundary.md` for full details.

## Verification Commands

```bash
# Verify core-ar compiles
./gradlew :core-ar:assembleDebug

# Verify app still compiles
./gradlew :app:assembleDebug

# Run all quality gates (optional)
./gradlew s1QualityGate
```

## Known Risks / Technical Debt

1. **Placeholder Interface**: `ArEngine.kt` contains TODO comments for future methods. These will be implemented in subsequent AR Sprint tasks.

2. **No Implementation Yet**: The module only contains the interface; actual ARCore/Filament bridge implementations will be added in future tasks.

3. **Integration Pending**: `feature-arview` does not yet depend on `core-ar`. This integration will be done when migrating AR rendering code.

## Next Steps

1. **AR_S1_02**: Add mesh loading interfaces to `core-ar`
2. **AR_S1_03**: Implement `ArEngineImpl` with Filament rendering
3. **AR_S1_04**: Migrate pose estimation from `feature-arview` to `core-ar`
4. **AR_S1_05**: Wire `feature-arview` to depend on `core-ar`

## Dependencies for Future Tasks

- `feature-arview` will need to add `implementation(project(":core-ar"))` when integration begins
- No changes needed to `core-domain` or `core-data` for this task
