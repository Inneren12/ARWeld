package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D

sealed interface EditorIntent {
    data class ToolChanged(val tool: EditorTool) : EditorIntent

    data class SelectEntity(val selection: EditorSelection) : EditorIntent

    data object ClearSelection : EditorIntent

    data class ViewTransformGesture(
        val panX: Float,
        val panY: Float,
        val zoomFactor: Float,
        val focalX: Float,
        val focalY: Float,
    ) : EditorIntent

    data object LoadRequested : EditorIntent

    data class Loaded(val drawing: Drawing2D) : EditorIntent

    data object SaveRequested : EditorIntent

    data object Saved : EditorIntent

    data class Error(val message: String) : EditorIntent

    data class ScalePointSelected(val point: Point2D) : EditorIntent

    data class ScaleLengthChanged(val text: String) : EditorIntent

    data object ScaleApplyRequested : EditorIntent

    data class ScaleApplied(val drawing: Drawing2D) : EditorIntent

    data class ScaleApplyFailed(val message: String) : EditorIntent

    data object UndoRequested : EditorIntent

    data object RedoRequested : EditorIntent
}
