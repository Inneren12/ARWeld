package com.example.arweld.feature.drawingeditor.viewmodel

fun reduceEditorState(state: EditorState, intent: EditorIntent): EditorState = when (intent) {
    is EditorIntent.ToolChanged -> state.copy(
        tool = intent.tool,
        selection = EditorSelection.None,
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
}
