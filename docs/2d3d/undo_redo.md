# 2D3D Manual Editor — Undo/Redo

## Overview
The manual editor uses **in-memory history stacks** to support undo/redo for drawing mutations. Each mutating action stores a full `Drawing2D` snapshot, keeping behavior deterministic and easy to reason about. The history stack is bounded to keep memory usage predictable.

## Data Structures
- `undoStack`: List of prior `Drawing2D` snapshots (bounded).
- `redoStack`: List of snapshots undone and available to reapply (bounded).
- Max depth: 50 snapshots per stack (newest retained).

## Rules
1. **Mutations** push the previous drawing onto `undoStack` and clear `redoStack`.
2. **Undo** pops the last entry from `undoStack`, applies it, and pushes the current snapshot onto `redoStack`.
3. **Redo** pops the last entry from `redoStack`, applies it, and pushes the current snapshot onto `undoStack`.
4. When stacks exceed the max depth, the oldest snapshot is dropped.
5. Undo/redo is disabled when the relevant stack is empty.

## Persistence
Undo/redo actions persist the restored `Drawing2D` immediately via `Drawing2DRepository.saveCurrentDrawing()` to keep `drawing2d.json` consistent with the UI state.

## Scope
Undo/redo currently tracks **Drawing2D mutations** (scale apply/reset and future node/member edits). The following are not tracked:
- Tool changes
- Selection changes
- View/pan/zoom transforms

Future tools should use the same snapshot-based approach for deterministic history.
