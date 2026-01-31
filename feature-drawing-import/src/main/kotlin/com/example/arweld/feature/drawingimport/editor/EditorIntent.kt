package com.example.arweld.feature.drawingimport.editor

sealed interface EditorIntent {
    data class ToolChanged(val tool: EditorTool) : EditorIntent

    data class ScaleTap(val worldPoint: Vec2) : EditorIntent

    data object ScaleClearDraft : EditorIntent
}
