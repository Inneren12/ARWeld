# Scale Tool — Stage 1 (Pick A/B)

## Goal
Stage 1 of the scale tool lets the user pick two calibration points on the drawing. No length entry happens yet (that arrives in S3-14). The UI shows a draft overlay with the points and the line between them.

## Interaction Flow
1. Set tool = **SCALE**.
2. Tap once to place **Point A** (world/drawing coordinates).
3. Tap again to place **Point B** (world/drawing coordinates).
4. The canvas renders markers labeled **A** and **B**, plus the line between them.

## Draft Reset Behavior
- If both A and B are already set and the user taps again, the draft resets:
  - **A** becomes the new tap point.
  - **B** is cleared.
- Switching **away from SCALE** clears the draft.
- Switching **to SCALE** keeps the current draft (if any).

## Coordinate Space
- Scale points are stored in **world/drawing coordinates**, matching the Drawing2D model space.
- Input taps convert from screen space to world space using the current `viewTransform`.
- Rendering converts world points back to screen space using the same transform so the draft remains stable under pan/zoom.
