package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D

enum class EditorTool {
    SELECT,
    SCALE,
    NODE,
    MEMBER,
}

sealed interface EditorSelection {
    data object None : EditorSelection

    data class Node(val id: String) : EditorSelection

    data class Member(val id: String) : EditorSelection
}

data class ViewTransform(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
)

data class EditorState(
    val tool: EditorTool = EditorTool.SELECT,
    val selection: EditorSelection = EditorSelection.None,
    val drawing: Drawing2D = Drawing2D(nodes = emptyList(), members = emptyList()),
    val isLoading: Boolean = true,
    val lastError: String? = null,
    val dirtyFlag: Boolean = false,
    val viewTransform: ViewTransform = ViewTransform(),
)
