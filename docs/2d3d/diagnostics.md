# 2D3D Editor Diagnostics

This document describes the diagnostic event logging API for the 2D3D manual editor.

## Overview

The editor diagnostics system provides structured event logging for editor actions. Events are emitted through the existing `DiagnosticsRecorder` infrastructure and included in diagnostics exports (see `docs/ops/DIAGNOSTICS_EXPORT.md`).

## Event Types

| Event Name | Phase | Description |
|------------|-------|-------------|
| `editor_opened` | lifecycle | Emitted when the editor screen is opened |
| `editor_drawing_saved` | lifecycle | Emitted when the drawing is saved |
| `editor_tool_changed` | tool | Emitted when the user switches tools |
| `editor_node_added` | node | Emitted when a new node is added |
| `editor_node_moved` | node | Emitted when a node is moved |
| `editor_member_added` | member | Emitted when a new member is added |
| `editor_scale_set` | scale | Emitted when scale calibration is set |

## Event Schema

All events include these common attributes:

| Attribute | Type | Description |
|-----------|------|-------------|
| `feature` | string | Always `"drawing_editor"` |
| `phase` | string | Event phase (lifecycle, tool, node, member, scale) |
| `projectId` | string? | Optional project identifier (no PII) |

### editor_opened

Emitted when the editor screen is opened.

```json
{
  "feature": "drawing_editor",
  "phase": "lifecycle",
  "projectId": "proj-123"
}
```

### editor_drawing_saved

Emitted when the drawing is saved.

| Attribute | Type | Description |
|-----------|------|-------------|
| `nodeCount` | string | Number of nodes in the drawing |
| `memberCount` | string | Number of members in the drawing |
| `hasScale` | string | Whether scale calibration is set ("true"/"false") |

```json
{
  "feature": "drawing_editor",
  "phase": "lifecycle",
  "projectId": "proj-123",
  "nodeCount": "10",
  "memberCount": "5",
  "hasScale": "true"
}
```

### editor_tool_changed

Emitted when the user switches tools.

| Attribute | Type | Description |
|-----------|------|-------------|
| `tool` | string | The newly selected tool (SELECT, SCALE, NODE, MEMBER) |
| `previousTool` | string? | The previously selected tool |

```json
{
  "feature": "drawing_editor",
  "phase": "tool",
  "projectId": "proj-123",
  "tool": "NODE",
  "previousTool": "SELECT"
}
```

### editor_node_added

Emitted when a new node is added.

| Attribute | Type | Description |
|-----------|------|-------------|
| `nodeId` | string | Identifier of the added node |
| `x` | string | X-coordinate of the node |
| `y` | string | Y-coordinate of the node |

```json
{
  "feature": "drawing_editor",
  "phase": "node",
  "projectId": "proj-123",
  "nodeId": "N42",
  "x": "123.45",
  "y": "678.9"
}
```

### editor_node_moved

Emitted when an existing node is moved.

| Attribute | Type | Description |
|-----------|------|-------------|
| `nodeId` | string | Identifier of the moved node |
| `fromX` | string | Original X-coordinate |
| `fromY` | string | Original Y-coordinate |
| `toX` | string | New X-coordinate |
| `toY` | string | New Y-coordinate |

```json
{
  "feature": "drawing_editor",
  "phase": "node",
  "projectId": "proj-123",
  "nodeId": "N99",
  "fromX": "10.0",
  "fromY": "20.0",
  "toX": "30.0",
  "toY": "40.0"
}
```

### editor_member_added

Emitted when a new member (connection between nodes) is added.

| Attribute | Type | Description |
|-----------|------|-------------|
| `memberId` | string | Identifier of the added member |
| `aNodeId` | string | Identifier of the first connected node |
| `bNodeId` | string | Identifier of the second connected node |

```json
{
  "feature": "drawing_editor",
  "phase": "member",
  "projectId": "proj-123",
  "memberId": "M1",
  "aNodeId": "N1",
  "bNodeId": "N2"
}
```

### editor_scale_set

Emitted when scale calibration is set.

| Attribute | Type | Description |
|-----------|------|-------------|
| `realWorldLength` | string | Real-world length value for calibration |
| `unit` | string | Unit of measurement (e.g., "mm", "in") |

```json
{
  "feature": "drawing_editor",
  "phase": "scale",
  "projectId": "proj-123",
  "realWorldLength": "1500.0",
  "unit": "mm"
}
```

## Usage

### Injection

The `EditorDiagnosticsLogger` is provided via Hilt and injected into the `ManualEditorViewModel`:

```kotlin
@HiltViewModel
class ManualEditorViewModel @Inject constructor(
    private val drawing2DRepository: Drawing2DRepository,
    private val editorDiagnosticsLogger: EditorDiagnosticsLogger,
) : ViewModel() {
    // ...
}
```

### Logging Events

Use the convenience methods on `EditorDiagnosticsLogger`:

```kotlin
// Log editor opened
editorDiagnosticsLogger.logEditorOpened(projectId = "proj-123")

// Log tool change
editorDiagnosticsLogger.logToolChanged(
    projectId = "proj-123",
    tool = "NODE",
    previousTool = "SELECT",
)

// Log node added
editorDiagnosticsLogger.logNodeAdded(
    projectId = "proj-123",
    nodeId = "N42",
    x = 123.45,
    y = 678.9,
)

// Log scale set
editorDiagnosticsLogger.logScaleSet(
    projectId = "proj-123",
    realWorldLength = 1500.0,
    unit = "mm",
)
```

### Generic Logging

For custom attributes, use the generic `log` method:

```kotlin
editorDiagnosticsLogger.log(
    event = EditorEvent.EDITOR_OPENED,
    projectId = "proj-123",
    extras = mapOf("customKey" to "customValue"),
)
```

## Implementation Files

| File | Description |
|------|-------------|
| `feature-drawing-editor/.../diagnostics/EditorDiagnostics.kt` | Event types and logger |
| `app/.../di/DiagnosticsModule.kt` | Hilt provider for the logger |
| `feature-drawing-editor/.../viewmodel/ManualEditorViewModel.kt` | Uses the logger |

## Testing

Unit tests are located at:
- `feature-drawing-editor/.../diagnostics/EditorDiagnosticsLoggerTest.kt`
- `feature-drawing-editor/.../viewmodel/ManualEditorViewModelTest.kt`

Run tests with:
```bash
./gradlew :feature-drawing-editor:test
```

## Privacy

Events do not contain PII. The `projectId` is a technical identifier, not user-identifying information. Avoid adding user names, email addresses, or other personal data to event attributes.

## Export

Editor diagnostic events are included in the diagnostics export zip alongside other diagnostic events. See `docs/ops/DIAGNOSTICS_EXPORT.md` for export instructions.
