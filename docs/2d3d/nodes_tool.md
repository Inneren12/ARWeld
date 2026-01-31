# 2D3D Manual Editor — Nodes Tool (MVP)

## Add node by tap
- When the **NODE** tool is active, tapping empty canvas space creates a new `Node2D` at the tap
  location in **world/drawing coordinates** (screen → world conversion via the current `viewTransform`).
- The created node is immediately selected.
- Node creation is undoable (a Drawing2D snapshot is pushed to the undo stack).

## Selection rules
- If the tap is within the node hit-test tolerance of an existing node, the editor selects that
  node and **does not create a new one**.
- Node hit testing uses the same screen-space tolerance defined in the selection rules, converted
  to world units using `viewTransform.scale`.

## Deterministic IDs
- New nodes use deterministic ID allocation (`N000001`, `N000002`, …) backed by
  `Drawing2DEditorMetaV1.nextNodeId`.
- Canonical ordering is preserved for save stability (nodes sorted by id on save).

## Persistence policy
- **Auto-save on discrete mutation.** Node additions are persisted immediately after the reducer
  applies the mutation (via the ViewModel effect).
- This keeps `drawing2d.json` consistent with the UI state and aligns with existing scale/undo
  persistence behavior.
