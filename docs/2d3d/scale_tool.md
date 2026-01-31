# Scale Tool — Stage 2 (Input + Apply)

## Goal
Stage 2 completes the scale tool by letting the user enter a **real length (mm)** after selecting points A/B. The editor computes `mmPerPx` (derived) and persists the scale calibration to `drawing2d.json`.

## Interaction Flow
1. Set tool = **SCALE**.
2. Tap once to place **Point A** (world/drawing coordinates).
3. Tap again to place **Point B** (world/drawing coordinates).
4. The canvas renders markers labeled **A** and **B**, plus the line between them.
5. The bottom sheet shows **“Enter real length (mm)”** + **Apply** once A/B exist.
6. On Apply, the editor computes `mmPerPx` and saves scale calibration.

## Computation
- `distancePx = distance(A, B)` measured in **editor drawing units** (currently 1 unit == 1 px prior to calibration).
- `mmPerPx = realLengthMm / distancePx` (derived; not stored in the schema).

## Validation Rules (deterministic)
- `realLengthMm > 0`.
- `distancePx(A, B) > epsilon` (points must be distinct).
- Input parsing is strict: digits with optional decimal point (`.`) only; no locale-dependent commas.

Inline errors are shown for invalid input or degenerate point selections.

## Persistence Policy
- Apply immediately persists to `drawing2d.json` via `Drawing2DRepository.saveCurrentDrawing()` (atomic write).
- If save fails, the UI shows an explicit error and the scale remains unchanged.

## Undo/Redo
- Applying scale pushes the previous `Drawing2D` snapshot onto the undo stack.
- Undo restores the previous scale state (or lack of scale).
- Redo reapplies the scale.

## Draft Reset Behavior
- If both A and B are already set and the user taps again, the draft resets:
  - **A** becomes the new tap point.
  - **B** is cleared.
- Switching **away from SCALE** clears the draft.
- Switching **to SCALE** keeps the current draft (if any).

## Scale Status Indicator
- The editor top bar shows a compact scale status chip:
  - **Scale not set** → warning + **Set** action (switches tool to SCALE).
  - **Scale invalid** → warning + **Reset** action (clears stored scale and returns to “not set”).
  - **Scale set** → “Scale: {mm/px} mm/px • Ref {length} mm”.
- Deterministic formatting:
  - `mm/px` formatted with 3 decimals using `Locale.US`.
  - Reference length formatted with 1 decimal using `Locale.US`.
  - No locale-dependent commas or separators.

## Coordinate Space
- Scale points are stored in **world/drawing coordinates**, matching the Drawing2D model space.
- Input taps convert from screen space to world space using the current `viewTransform`.
- Rendering converts world points back to screen space using the same transform so the draft remains stable under pan/zoom.
