# 2D3D Editor UI — MVP

## Canvas
- The editor canvas shows the drawing in world space with pan/zoom via `viewTransform`.
- Draft overlays render above the drawing contents.

## Tools (MVP)
- **Select:** Pick or move entities.
- **Pan:** Navigate the view.
- **Scale (Stage 1):** Tap to pick calibration points.

## Scale Draft Overlay (S3-13)
- When the **Scale** tool is active, taps create Point **A** and Point **B** in world space.
- The canvas displays labeled markers **A/B** and a line between them while the draft is active.
- If A and B are already set, the next tap resets the draft to a new **A** and clears **B**.
# Manual Editor UI MVP (2D3D)

## Purpose
This document describes the MVP shell for the 2D3D Manual Editor screen. The screen is a
read-only placeholder that wires a `Drawing2D` instance into the UI and exposes a minimal
layout for toolbar, canvas, and bottom sheet sections.

## Layout

### Toolbar
- Title: **Manual Editor**
- Tool selector buttons (placeholders): **Select**, **Scale**, **Node**, **Member**
- Selection only updates local UI state (no editing actions yet)

### Canvas
- Placeholder surface for the 2D drawing area
- Displays a summary overlay with:
  - Node count
  - Member count
  - Missing node reference count
  - Scale calibration status

### Bottom Sheet
- Placeholder panel for context details
- Echoes current tool and the same `Drawing2D` summary

## Data Wiring
- `ManualEditorViewModel` loads the current `Drawing2D` via `Drawing2DRepository`.
- The repository currently reads from `filesDir/drawing2d/current_drawing.json` and
  falls back to an empty drawing if the file is missing or invalid.

## Code Locations
- Route wrapper: `app/src/main/kotlin/com/example/arweld/ui/drawingeditor/ManualEditorRoute.kt`
- Screen UI: `feature-drawing-editor/src/main/kotlin/com/example/arweld/feature/drawingeditor/ui/ManualEditorScreen.kt`
- ViewModel + state: `feature-drawing-editor/src/main/kotlin/com/example/arweld/feature/drawingeditor/viewmodel/`
- Repository interface: `core-domain/src/main/kotlin/com/example/arweld/core/domain/drawing2d/Drawing2DRepository.kt`
- Repository implementation: `core-data/src/main/kotlin/com/example/arweld/core/data/drawing2d/Drawing2DRepositoryImpl.kt`

## Next Steps (Out of Scope)
- Gesture handling, node/member editing tools
- Scale calibration workflow
- Drawing persistence + save/export logic
