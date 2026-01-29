# AR Sprint 1 / Task 03 — Move ARCore Session Manager to :core-ar Closeout

**Task:** Move ARCore session lifecycle handling into `:core-ar` while keeping feature behavior identical.
**Status:** Complete
**Date:** 2026-02-28

## Summary

Moved `ARCoreSessionManager` out of `feature-arview` into `core-ar` so session lifecycle lives in the core AR module while feature UI remains unchanged. Updated references and docs, and added a basic instrumentation smoke test that renders the AR screen without crashing.

## What Changed

### Moved Files

| Original Location | New Location |
|-------------------|--------------|
| `feature-arview/src/main/kotlin/com/example/arweld/feature/arview/arcore/ARCoreSessionManager.kt` | `core-ar/src/main/kotlin/com/example/arweld/core/ar/arcore/ARCoreSessionManager.kt` |

### Modified Files

| File | Change |
|------|--------|
| `feature-arview/.../arcore/ARViewController.kt` | Updated import to `core-ar` session manager |
| `feature-arview/.../arcore/ARSceneRenderer.kt` | Updated import to `core-ar` session manager |
| `docs/MODULES.md` | Noted session lifecycle now resides in `core-ar` |
| `docs/FILE_OVERVIEW.md` | Updated AR session manager paths and quick reference |
| `docs/architecture/core-ar_boundary.md` | Added “Session manager moved” section |
| `docs/sprints/AR_S1_CORE_AR_03_CLOSEOUT.md` | This closeout document |

### Added Tests

| Test | Purpose |
|------|---------|
| `app/src/androidTest/java/com/example/arweld/ui/ar/ARViewSmokeTest.kt` | Launches ARViewScreen and verifies it renders without crash |

## Behavior Notes

- No changes to marker detection, pose estimation, smoothing, or drift logic.
- No runtime configuration changes; only package relocation and import updates.

## Acceptance Criteria Checklist

- [x] Session manager moved to `core-ar` under `.../arcore/`
- [x] `feature-arview` still builds against `core-ar`
- [x] Documentation updated (MODULES, FILE_OVERVIEW, core-ar boundary)
- [x] Instrumentation smoke test added for AR screen launch

## Next Steps

- Continue modularizing additional AR engine components into `core-ar` as scoped by future tasks.
