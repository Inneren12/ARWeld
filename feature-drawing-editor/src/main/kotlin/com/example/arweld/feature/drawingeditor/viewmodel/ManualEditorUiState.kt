package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.feature.drawingeditor.editor.ViewTransform

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
    val drawing: Drawing2D = Drawing2D(nodes = emptyList(), members = emptyList()),
    val summary: Drawing2DSummary = Drawing2DSummary(),
    val selection: ManualEditorSelection = ManualEditorSelection.None,
    val viewTransform: ViewTransform = ViewTransform.identity(),
    val undoStack: List<Drawing2D> = emptyList(),
    val errorMessage: String? = null,
)

sealed class ManualEditorSelection {
    data object None : ManualEditorSelection()

    data class Node(val nodeId: String) : ManualEditorSelection()
}
