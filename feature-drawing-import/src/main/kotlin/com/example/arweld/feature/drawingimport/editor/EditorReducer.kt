package com.example.arweld.feature.drawingimport.editor

fun reduceEditorState(
    state: EditorState,
    intent: EditorIntent,
): EditorState {
    return when (intent) {
        is EditorIntent.ToolChanged -> {
            val nextDraft = if (intent.tool == EditorTool.SCALE) {
                state.scaleDraft
            } else {
                null
            }
            state.copy(tool = intent.tool, scaleDraft = nextDraft)
        }
        is EditorIntent.ScaleTap -> {
            if (state.tool != EditorTool.SCALE) {
                return state
            }
            val currentDraft = state.scaleDraft ?: ScaleDraft(pointA = null, pointB = null)
            val updatedDraft = when {
                currentDraft.pointA == null -> currentDraft.copy(pointA = intent.worldPoint)
                currentDraft.pointB == null -> currentDraft.copy(pointB = intent.worldPoint)
                else -> ScaleDraft(pointA = intent.worldPoint, pointB = null)
            }
            state.copy(scaleDraft = updatedDraft)
        }
        EditorIntent.ScaleClearDraft -> state.copy(scaleDraft = null)
    }
}
