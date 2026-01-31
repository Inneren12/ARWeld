# 2D3D Editor State Machine (S3-08)

This document describes the Manual Editor state machine introduced in S3-08. It keeps
editor interactions deterministic and testable by separating **pure state updates**
from **side-effectful IO** (load/save).

## Core Types

### EditorState
`EditorState` is the single source of truth for editor UI state:
- **tool**: Current editing tool (`SELECT`, `SCALE`, `NODE`, `MEMBER`).
- **selection**: Current selection (`None`, `Node(id)`, `Member(id)`).
- **drawing**: Current `Drawing2D` snapshot.
- **isLoading**: Loading/saving flag (used by the UI to show progress).
- **lastError**: Last error string (nullable).
- **dirtyFlag**: Whether the drawing has unsaved edits (placeholder; no auto-save yet).
- **viewTransform**: Placeholder for pan/zoom (scale + offset).

### EditorIntent
`EditorIntent` captures user and system intents:
- Tool/selection: `ToolChanged`, `SelectEntity`, `ClearSelection`.
- Load/save: `LoadRequested`, `Loaded`, `SaveRequested`, `Saved`.
- Error: `Error(message)`.

### Reducer (Pure)
`reduceEditorState(state, intent)` is a pure function that:
- Updates the tool and **clears selection** on `ToolChanged` (policy choice to avoid
  stale selections when switching tools).
- Applies the `Drawing2D` snapshot on `Loaded` and clears `isLoading`/`lastError`.
- Sets `lastError` on `Error` and clears `isLoading`.
- Clears selection on `ClearSelection`.
- Marks `isLoading` for `LoadRequested` / `SaveRequested`.

This reducer performs **no IO** and is unit-tested.

## Effects (ViewModel)

The `ManualEditorViewModel` orchestrates effects:
- **Load on init**: Dispatches `LoadRequested`, then calls `Drawing2DRepository.getCurrentDrawing()`,
  dispatching `Loaded` or `Error`.
- **Save on request**: Dispatches `SaveRequested`, then calls
  `Drawing2DRepository.saveCurrentDrawing()`, dispatching `Saved` or `Error`.

The UI only emits intents (e.g., tool toggles -> `ToolChanged`).

## Extension Points

Future tool workflows plug in via:
- `EditorTool` enum (add new tool values).
- `EditorIntent` for tool-specific actions (e.g., `ScalePointSelected`).
- Reducer branches for the new intents.
- ViewModel effect handlers for tool-specific IO.

Undo/redo is intentionally deferred to S3-12.
