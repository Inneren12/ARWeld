package com.example.arweld.feature.drawingeditor.viewmodel

fun reduceEditorState(state: EditorState, intent: EditorIntent): EditorState = when (intent) {
    is EditorIntent.ToolChanged -> state.copy(
        tool = intent.tool,
        selection = EditorSelection.None,
        scaleDraft = if (intent.tool == EditorTool.SCALE) {
            state.scaleDraft
        } else {
            ScaleDraft()
        },
    )
    is EditorIntent.SelectEntity -> state.copy(selection = intent.selection)
    EditorIntent.ClearSelection -> state.copy(selection = EditorSelection.None)
    is EditorIntent.ViewTransformGesture -> state.copy(
        viewTransform = applyViewTransformGesture(
            transform = state.viewTransform,
            panX = intent.panX,
            panY = intent.panY,
            zoomFactor = intent.zoomFactor,
            focalX = intent.focalX,
            focalY = intent.focalY,
        )
    )
    EditorIntent.LoadRequested -> state.copy(
        isLoading = true,
        lastError = null,
    )
    is EditorIntent.Loaded -> state.copy(
        isLoading = false,
        drawing = intent.drawing,
        lastError = null,
        dirtyFlag = false,
        undoStack = emptyList(),
        redoStack = emptyList(),
    )
    EditorIntent.SaveRequested -> state.copy(
        isLoading = true,
        lastError = null,
    )
    EditorIntent.Saved -> state.copy(
        isLoading = false,
        lastError = null,
        dirtyFlag = false,
    )
    is EditorIntent.Error -> state.copy(
        isLoading = false,
        lastError = intent.message,
    )
    is EditorIntent.ScalePointSelected -> {
        val draft = state.scaleDraft
        val updatedDraft = when {
            draft.pointA == null -> draft.copy(pointA = intent.point)
            draft.pointB == null -> draft.copy(pointB = intent.point)
            else -> draft.copy(pointA = intent.point, pointB = null)
        }
        state.copy(scaleDraft = recomputeScaleDraft(updatedDraft))
    }
    is EditorIntent.ScaleLengthChanged -> {
        val updatedDraft = state.scaleDraft.copy(inputText = intent.text)
        state.copy(scaleDraft = recomputeScaleDraft(updatedDraft))
    }
    EditorIntent.ScaleApplyRequested -> state.copy(
        scaleDraft = state.scaleDraft.copy(applyError = null),
    )
    is EditorIntent.ScaleApplied -> state.copy(
        drawing = intent.drawing,
        dirtyFlag = false,
        lastError = null,
        scaleDraft = ScaleDraft(),
        undoStack = state.undoStack + state.drawing,
        redoStack = emptyList(),
    )
    is EditorIntent.ScaleApplyFailed -> state.copy(
        scaleDraft = state.scaleDraft.copy(applyError = intent.message),
    )
    EditorIntent.UndoRequested -> {
        if (state.undoStack.isEmpty()) {
            state
        } else {
            val newDrawing = state.undoStack.last()
            state.copy(
                drawing = newDrawing,
                undoStack = state.undoStack.dropLast(1),
                redoStack = state.redoStack + state.drawing,
                dirtyFlag = false,
            )
        }
    }
    EditorIntent.RedoRequested -> {
        if (state.redoStack.isEmpty()) {
            state
        } else {
            val newDrawing = state.redoStack.last()
            state.copy(
                drawing = newDrawing,
                redoStack = state.redoStack.dropLast(1),
                undoStack = state.undoStack + state.drawing,
                dirtyFlag = false,
            )
        }
    }
}

private const val SCALE_DISTANCE_EPSILON = 1e-6

private fun recomputeScaleDraft(draft: ScaleDraft): ScaleDraft {
    val pointA = draft.pointA
    val pointB = draft.pointB
    if (pointA == null || pointB == null) {
        return draft.copy(
            inputError = null,
            applyError = null,
            pendingDistancePx = null,
            pendingMmPerPx = null,
        )
    }

    val distance = distanceBetween(pointA, pointB)
    if (distance <= SCALE_DISTANCE_EPSILON) {
        return draft.copy(
            inputError = "Points are too close. Select two distinct points.",
            applyError = null,
            pendingDistancePx = null,
            pendingMmPerPx = null,
        )
    }

    val input = draft.inputText.trim()
    if (input.isEmpty()) {
        return draft.copy(
            inputError = null,
            applyError = null,
            pendingDistancePx = distance,
            pendingMmPerPx = null,
        )
    }

    val parsedLength = parseStrictNumber(input)
    if (parsedLength == null) {
        return draft.copy(
            inputError = "Enter a valid length in mm (e.g., 1500.0).",
            applyError = null,
            pendingDistancePx = distance,
            pendingMmPerPx = null,
        )
    }
    if (parsedLength <= 0.0) {
        return draft.copy(
            inputError = "Length must be > 0 mm.",
            applyError = null,
            pendingDistancePx = distance,
            pendingMmPerPx = null,
        )
    }

    return draft.copy(
        inputError = null,
        applyError = null,
        pendingDistancePx = distance,
        pendingMmPerPx = parsedLength / distance,
    )
}
