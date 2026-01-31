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
