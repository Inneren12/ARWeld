# 2D3D Editor — Nodes Tool

## Add node by tap (S3-17)
- When the **Node** tool is active, tapping empty canvas space creates a new `Node2D`.
- The new node uses **deterministic IDs** via the editor v1 ID allocator (`N000001`, `N000002`, ...).
- The newly created node is auto-selected immediately after creation.
- Node coordinates are stored in **world/drawing space**, using screen → world conversion from the current `viewTransform`.

## Selection rules
- If a tap lands within the node hit radius, the existing node is selected.
- No new node is created when selecting an existing node.

## Persistence policy
**Auto-save on discrete mutation.**
- Adding a node triggers an immediate save of `drawing2d/current_drawing.json`.
- Serialization uses canonical ordering to ensure deterministic output.
- Selection-only taps do not write to disk.

## Undo/redo
- Each node creation pushes the prior `Drawing2D` state onto the undo stack.
- Undo UI is not yet exposed; the stack is maintained for upcoming tools.
