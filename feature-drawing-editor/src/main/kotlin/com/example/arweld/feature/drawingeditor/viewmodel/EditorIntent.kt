package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D

sealed interface EditorIntent {
    data class ToolChanged(val tool: EditorTool) : EditorIntent

    data class SelectEntity(val selection: EditorSelection) : EditorIntent

    data object ClearSelection : EditorIntent

    data object LoadRequested : EditorIntent

    data class Loaded(val drawing: Drawing2D) : EditorIntent

    data object SaveRequested : EditorIntent

    data object Saved : EditorIntent

    data class Error(val message: String) : EditorIntent
}
