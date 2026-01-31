# 2D3D Manual Editor — Undo/Redo

## Overview
The manual editor uses **in-memory history stacks** to support undo/redo for scale calibration (S3-14). Each mutating action stores a full `Drawing2D` snapshot, keeping behavior deterministic and easy to reason about.

## Data Structures
- `undoStack`: List of prior `Drawing2D` snapshots.
- `redoStack`: List of snapshots undone and available to reapply.

## Rules
1. **Apply scale** pushes the previous drawing onto `undoStack` and clears `redoStack`.
2. **Undo** pops the last entry from `undoStack`, applies it, and pushes the current snapshot onto `redoStack`.
3. **Redo** pops the last entry from `redoStack`, applies it, and pushes the current snapshot onto `undoStack`.
4. Undo/redo is disabled when the relevant stack is empty.

## Persistence
Undo/redo actions persist the restored `Drawing2D` immediately via `Drawing2DRepository.saveCurrentDrawing()` to keep `drawing2d.json` consistent with the UI state.

## Scope
Only the scale calibration workflow is wired to undo/redo in S3-14. Future tools should follow the same snapshot-based approach for deterministic history.
