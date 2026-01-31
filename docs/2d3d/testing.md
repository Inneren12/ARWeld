# 2D3D Editor Testing

This document describes the test coverage and testing strategy for the 2D3D manual editor feature.

## Overview

The 2D3D editor testing is organized into multiple layers:

| Layer | Type | Module | What It Tests |
|-------|------|--------|---------------|
| Smoke | JVM Unit | `core-drawing2d` | Editor foundations: serialization, validation, empty state |
| Schema | JVM Unit | `core-drawing2d` | Drawing2D schema serialization, validation, determinism |
| Domain | JVM Unit | `core-domain` | Project2D3DWorkspace path conventions |
| ViewModel | JVM Unit | `feature-drawing-editor` | ManualEditorViewModel state management |
| Diagnostics | JVM Unit | `feature-drawing-editor` | EditorDiagnosticsLogger event emission |

## Scale Tool Tests

The scale tool has dedicated regression coverage to keep calibration deterministic and safe:

- **Math + validation:** mmPerPx calculation, tiny/zero distance rejection, and non-positive length validation.
- **Reducer/ViewModel flow:** A/B taps + length entry apply scale and surface error states.
- **Persistence:** save/load round trips preserve `Drawing2D.scale`, and repeated saves emit identical JSON.
- **Undo/Redo:** scale apply can undo/redo and clears redo on new apply.

### How to Run

```bash
# Scale tool reducer + ViewModel tests
./gradlew :feature-drawing-editor:test --tests "*ScaleDraftReducerTest" --tests "*ManualEditorScaleTest" --tests "*EditorReducerUndoRedoTest" --tests "*ScaleStatusTest"

# Persistence coverage (drawing2d.json save/load determinism)
./gradlew :core-data:test --tests "*Drawing2DRepositoryImplTest"
```

## Node Move Tests

Node drag behavior is covered with reducer and ViewModel tests:

- **Reducer:** deterministic drag math, undo snapshot only on drag end.
- **ViewModel:** persistence occurs once per completed drag.

### How to Run

```bash
# Node drag reducer + ViewModel tests
./gradlew :feature-drawing-editor:test --tests "*NodeDragReducerTest" --tests "*ManualEditorViewModelTest"
```

## Node Delete Tests

Node delete behavior is covered with reducer tests:

- **Reducer:** deletes the node and cascades removal of connected members without leaving dangling references.
- **Undo/Redo:** delete can be undone and redone as a single snapshot.

### How to Run

```bash
# Node delete reducer + undo/redo coverage
./gradlew :feature-drawing-editor:test --tests "*EditorReducerTest" --tests "*EditorReducerUndoRedoTest"
```

## Node Edit Tests

Node coordinate editing is covered with reducer + undo tests:

- **Reducer:** draft input validation, apply updates node coordinates, and apply failures surface errors.
- **Undo/Redo:** apply then undo restores the previous coordinates.

### How to Run

```bash
# Node edit reducer + undo coverage
./gradlew :feature-drawing-editor:test --tests "*NodeEditReducerTest" --tests "*EditorReducerUndoRedoTest"
```

## Member Tool Validation Tests

Member creation validation is covered with reducer tests:

- **Same-node rejection:** selecting A == B shows an error and does not mutate the drawing.
- **Duplicate rejection:** selecting an existing A/B pair (direction-insensitive) shows an error and does not mutate.
- **Undo stack:** invalid attempts do not push history snapshots.

### How to Run

```bash
# Member tool reducer validation
./gradlew :feature-drawing-editor:test --tests "*EditorReducerTest"
```

## Member Panel Length Tests

The member bottom sheet calculations are covered with pure JVM tests:

- `computeLengthPx` uses Euclidean distance between endpoint coordinates.
- `computeMmPerPx` and `computeLengthMm` follow the scale conversion formula.
- Missing scale inputs return `null` without crashing.
- Unresolved endpoints return explicit error results.

### How to Run

```bash
./gradlew :feature-drawing-editor:test --tests "*MemberMetricsTest"
```

## Smoke Tests

The smoke test suite (`Drawing2DEditorSmokeTest`) provides minimal coverage to catch basic wiring regressions in the 2D3D editor foundations without requiring full UI.

### What Smoke Tests Cover

1. **Empty drawing serialization roundtrip** - Verifies an empty drawing (no nodes, members, or scale) can be serialized to JSON and deserialized back correctly.

2. **Missing node reference validation** - Ensures the validation helper correctly reports no missing references for empty drawings.

3. **Schema version persistence** - Confirms schema version is included in serialized JSON for forward compatibility.

4. **Meta state preservation** - Validates editor meta (ID allocators) roundtrips correctly.

5. **Save/load cycle simulation** - Verifies that a serialize-deserialize cycle preserves drawing state.

6. **Minimal JSON structure** - Confirms that empty drawings produce minimal JSON with required fields only.

Note: Workspace path convention tests are in `core-domain/Project2D3DWorkspaceTest`.

### Why Smoke Tests?

- **Fast feedback**: JVM tests run quickly without Android emulator
- **Catch regressions early**: Breaks in serialization or workspace wiring are detected before UI testing
- **CI-friendly**: Included in `./gradlew test` which runs in S1/S2 quality gates

## Running Tests

### All 2D3D Related Tests

```bash
# Run smoke tests in core-drawing2d
./gradlew :core-drawing2d:test --tests "*Drawing2DEditorSmokeTest"

# Run all editor schema tests
./gradlew :core-drawing2d:test --tests "*editor.v1.*"

# Run workspace tests in core-domain
./gradlew :core-domain:test --tests "*Project2D3DWorkspaceTest"

# Run editor ViewModel tests
./gradlew :feature-drawing-editor:test
```

### Full Test Suite

```bash
# Run all unit tests
./gradlew test --no-daemon

# Quality gate (includes tests + lint)
./gradlew s1QualityGate --no-daemon
```

## Test Locations

| Test File | Module | Description |
|-----------|--------|-------------|
| `Drawing2DEditorSmokeTest.kt` | `core-drawing2d` | Smoke tests for editor foundations |
| `Drawing2DEditorSerializationTest.kt` | `core-drawing2d` | Full serialization roundtrip tests |
| `Drawing2DEditorDeterminismTest.kt` | `core-drawing2d` | Deterministic serialization verification |
| `Project2D3DWorkspaceTest.kt` | `core-domain` | Workspace path and segment tests |
| `ManualEditorViewModelTest.kt` | `feature-drawing-editor` | ViewModel state management tests |
| `NodeDragReducerTest.kt` | `feature-drawing-editor` | Node drag reducer tests (deterministic move + history) |
| `NodeEditReducerTest.kt` | `feature-drawing-editor` | Node edit reducer tests (draft + apply + error) |
| `EditorDiagnosticsLoggerTest.kt` | `feature-drawing-editor` | Diagnostics event emission tests |

## Adding New Tests

When extending the 2D3D editor, add tests at the appropriate layer:

1. **New data models** - Add serialization tests in `core-drawing2d`
2. **New validation rules** - Add to `DrawingValidatorCoreTest` or create new validator tests
3. **New ViewModel actions** - Add to `ManualEditorViewModelTest`
4. **New diagnostics events** - Add to `EditorDiagnosticsLoggerTest`

## Coverage Goals

The 2D3D editor tests contribute to the repository's coverage requirements:

- `core-domain` module: 60% instruction coverage baseline
- Overall project: 25% instruction coverage baseline

Run coverage reports with:

```bash
./gradlew koverHtmlReport
```

Reports are generated at `build/reports/kover/html/index.html`.
