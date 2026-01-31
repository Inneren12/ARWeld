package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D

enum class ManualEditorTool {
    SELECT,
    SCALE,
    NODE,
    MEMBER,
}

data class Drawing2DSummary(
    val nodeCount: Int = 0,
    val memberCount: Int = 0,
    val missingNodeRefs: Int = 0,
    val hasScale: Boolean = false,
)

data class ManualEditorUiState(
    val selectedTool: ManualEditorTool = ManualEditorTool.SELECT,
    val isLoading: Boolean = true,
    val drawing: Drawing2D? = null,
    val summary: Drawing2DSummary = Drawing2DSummary(),
    val errorMessage: String? = null,
)
