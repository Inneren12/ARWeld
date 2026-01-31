# 2D3D Manual Editor — Nodes Tool (MVP)

## Add node by tap
- When the **NODE** tool is active, tapping empty canvas space creates a new `Node2D` at the tap
  location in **world/drawing coordinates** (screen → world conversion via the current `viewTransform`).
- The created node is immediately selected.
- Node creation is undoable (a Drawing2D snapshot is pushed to the undo stack).

## Selection rules
- If the tap is within the node hit-test tolerance of an existing node, the editor selects that
  node and **does not create a new one**.
- Node hit testing uses the same screen-space tolerance defined in the selection rules
  (`EditorUiConstants.NODE_HIT_RADIUS_DP`), converted to world units using `viewTransform.scale`.

## Drag to move (NODE tool)
- Dragging starts when the pointer goes down on an existing node while the **NODE** tool is active.
  The node is selected immediately and begins moving once the pointer exceeds touch slop.
- While dragging, the node updates live for immediate visual feedback.
- Dragging uses world-space math derived from the drag start to avoid drift:

```
deltaWorld = pointerWorld - startPointerWorld
newWorld = startWorldPos + deltaWorld
```

- The node position is computed from the **original start position**, not accumulated per tick, to
  keep movement deterministic.

## Drag commit policy
- A single undo snapshot is pushed on **drag end** (not per move tick).
- Persistence happens only on drag end:
  - **Auto-save enabled:** persist `drawing2d.json` once per completed drag.
  - **Manual save:** mark dirty once per completed drag (no per-frame writes).

## Deterministic IDs
- New nodes use deterministic ID allocation (`N000001`, `N000002`, …) backed by
  `Drawing2DEditorMetaV1.nextNodeId`.
- Canonical ordering is preserved for save stability (nodes sorted by id on save).

## Persistence policy
- **Auto-save on discrete mutation.** Node additions are persisted immediately after the reducer
  applies the mutation (via the ViewModel effect).
- This keeps `drawing2d.json` consistent with the UI state and aligns with existing scale/undo
  persistence behavior.

## Delete node (MVP)
- When a node is selected, the bottom sheet shows a **Delete node** action.
- Deleting a node **cascades**: all members connected to that node (`aNodeId` or `bNodeId`) are
  removed in the same mutation to prevent dangling references.
- Selection is cleared after deletion.
- The delete action is undoable as a single step (one Drawing2D snapshot).
- Persistence follows the same policy as other discrete mutations: save once per delete.

## Edit coordinates (S3-21)
- When a node is selected, the bottom sheet shows its **world/drawing-space coordinates** and
  editable **X/Y** fields.
- The inputs are draft-only; the node is not mutated until **Apply** is pressed.
- Apply updates the node coordinates as a single undoable snapshot and persists once.
- Validation rules (locale-stable):
  - **Dot decimal only** (e.g., `12`, `-3.5`, `0.125`).
  - Optional leading **minus** for negative coordinates.
  - No commas, no exponent notation, no blank values.
  - Parsed values must be finite (no NaN/Infinity).
- Invalid inputs surface explicit field errors and block Apply without mutating the drawing.
