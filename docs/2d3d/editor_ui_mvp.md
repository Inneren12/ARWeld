# 2D3D Editor UI — MVP

## Canvas
- The editor canvas shows the drawing in world space with pan/zoom via `viewTransform`.
- Draft overlays render above the drawing contents.

## Tools (MVP)
- **Select:** Pick or move entities.
- **Pan:** Navigate the view.
- **Scale (Stage 1):** Tap to pick calibration points.
- **Node (Stage 1):** Tap empty space to add a node at the world-space tap location; tapping near
  an existing node selects it.
- **Member (Stage 1):** Tap node **A** then node **B** to create a member (node-to-node only).
  The bottom sheet shows a hint (“select the first/second node”) while the draft is active.

## Scale Draft Overlay (S3-13)
- When the **Scale** tool is active, taps create Point **A** and Point **B** in world space.
- The canvas displays labeled markers **A/B** and a line between them while the draft is active.
- If A and B are already set, the next tap resets the draft to a new **A** and clears **B**.

---

## Scale Input (S3-14)
- When points **A** and **B** are set, the bottom sheet shows **“Enter real length (mm)”** and an **Apply** button.
- The UI shows the measured distance in drawing units and the derived `mmPerPx` preview when input is valid.
- Applying the scale persists immediately to `drawing2d.json` and is undoable.

## Undo/Redo (S3-14)
- Undo/Redo controls are available in the bottom sheet for scale edits.
- Undo restores the previous `Drawing2D` snapshot; Redo reapplies it.
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
- Scale status chip:
  - **Missing:** shows “Scale: not set” with a **Set** action that switches to the SCALE tool.
  - **Set:** shows “Scale: {mm/px} mm/px • Ref {length} mm” using deterministic formatting.
  - **Invalid:** shows “Scale: invalid” with a **Reset** action to clear the saved scale.
  - Formatting rules: `mm/px` uses 3 decimals, reference length uses 1 decimal, dot decimal (Locale.US).
  - Screenshot placeholder: `scale_status_indicator.png` (top bar chip/badge)

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
- The Scale tool panel continues to show the measured distance + derived `mm/px` preview using the same deterministic formatting rules.
- When a node is selected:
  - Show node ID and current **world** coordinates (X/Y).
  - Provide **X** and **Y** text inputs with explicit validation errors.
  - Include an **Apply** button to commit the coordinate change as a single undo step.
  - Keep the **Delete node** action visible below the edit controls.
- When a member is selected:
  - Show member ID and endpoint node IDs (A/B).
  - Show resolved endpoint coordinates (A/B) in world space.
  - Always show the member length in drawing units (`px`).
  - If scale is set and valid, also show the length in mm using
    `lengthMm = lengthPx * (realLengthMm / distance(scale.pointA, scale.pointB))`.
  - If scale is missing, show a “Set scale to get mm length” CTA that switches to the Scale tool.
  - Show a profile reference placeholder (“Profile: —” when not set).
  - Keep the **Delete member** action visible below the details.

## Data Wiring
- `ManualEditorViewModel` loads the current `Drawing2D` via `Drawing2DRepository`.
- The repository currently reads from `filesDir/drawing2d/current_drawing.json` and
  falls back to an empty drawing if the file is missing or invalid.

## Code Locations
- Route wrapper: `app/src/main/kotlin/com/example/arweld/ui/drawingeditor/ManualEditorRoute.kt`
- Screen UI: `feature-drawing-editor/src/main/kotlin/com/example/arweld/feature/drawingeditor/ui/ManualEditorScreen.kt`
- ViewModel + state: `feature-drawing-editor/src/main/kotlin/com/example/arweld/feature/drawingeditor/viewmodel/`
- Repository interface: `core-domain/src/main/kotlin/com/example/arweld/core/domain/drawing2d/Drawing2DRepository.kt`
- Repository implementation: `core-data/src/main/kotlin/com/example/arweld/core/data/drawing2d/CurrentDrawing2DRepositoryImpl.kt`

---

## Render Pipeline (S3-10)

The canvas renderer implements a layered drawing pipeline that respects the `viewTransform` for world-to-screen mapping.

### Render Order (back to front)

1. **Underlay Image** (optional)
   - Loaded via Coil `SubcomposeAsyncImage` for efficient async loading
   - Rendered at world origin (0,0) with `viewTransform` applied
   - Policy: fit at origin; image pixel coordinates map 1:1 to world units

2. **Coordinate Axes**
   - X axis: green horizontal line (world space)
   - Y axis: blue vertical line (world space)
   - Drawn with fixed stroke width within the transform

3. **Members** (lines between nodes)
   - Resolved via `resolveAllMemberEndpoints()` helper
   - Gracefully skips members with missing node references (debug logged)
   - Selected members render with thicker stroke and highlight color

4. **Nodes** (circles/markers)
   - Drawn as filled circles with stroke outline
   - Selected nodes render larger with highlight colors
   - Radius and colors defined in `RenderConfig`

5. **Origin Marker**
   - Small pink circle in screen space (always visible reference point)

### Underlay State

The editor tracks underlay image state via `UnderlayState` sealed interface:

```kotlin
sealed interface UnderlayState {
    data object None : UnderlayState           // No underlay configured
    data object Loading : UnderlayState        // Underlay is loading
    data class Loaded(val file: File)          // Underlay ready to render
    data class Missing(val path: String)       // File not found
}
```

### Member Endpoint Resolution

Members reference nodes by ID (`aNodeId`, `bNodeId`). The render pipeline resolves these before drawing:

```kotlin
// Build lookup once, resolve all members
val nodeLookup = buildNodeLookup(drawing)
val resolvedMembers = resolveAllMemberEndpoints(drawing)
```

Resolution results:
- `MemberEndpointResult.Resolved`: Both nodes found, ready to draw
- `MemberEndpointResult.MissingNodes`: One or both nodes missing (skipped with debug log)

### Performance Considerations

- **Pre-computed resolution**: `resolveAllMemberEndpoints()` is memoized via `remember(drawing)` to avoid per-frame allocations
- **Constant render config**: Colors and dimensions are pre-defined constants
- **Coil caching**: Underlay image loading benefits from Coil's built-in memory and disk caching
- **Transform reuse**: `withTransform` block batches all world-space primitives

### Render Configuration

Defined in `RenderConfig` object:

| Primitive | Property | Value |
|-----------|----------|-------|
| Node | radius | 8f |
| Node | stroke width | 2f |
| Node | fill color | Amber (#FFC107) |
| Node (selected) | fill color | Blue (#2196F3) |
| Member | stroke width | 3f |
| Member | color | Blue Grey (#607D8B) |
| Member (selected) | color | Blue (#2196F3) |
| Axis | length | 500f |
| Axis | stroke width | 1.5f |

### Code Locations (Render Pipeline)

- Render helpers: `feature-drawing-editor/src/main/kotlin/com/example/arweld/feature/drawingeditor/render/DrawingRenderHelpers.kt`
- Canvas composable: `ManualEditorScreen.kt` → `DrawingCanvasWithUnderlay()`
- Unit tests: `feature-drawing-editor/src/test/kotlin/com/example/arweld/feature/drawingeditor/render/DrawingRenderHelpersTest.kt`

---

## Next Steps (Out of Scope)

- Gesture handling, node/member editing tools (Pack D/E)
- Scale calibration workflow
- Drawing persistence + save/export logic
