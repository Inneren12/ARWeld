package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import java.io.File

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

/**
 * Underlay image state for the canvas renderer.
 */
sealed interface UnderlayState {
    /** No underlay configured in the workspace. */
    data object None : UnderlayState

    /** Underlay is configured but loading. */
    data object Loading : UnderlayState

    /** Underlay loaded successfully with file path for Coil. */
    data class Loaded(val file: File) : UnderlayState

    /** Underlay file is configured but doesn't exist or failed to load. */
    data class Missing(val path: String) : UnderlayState
}

data class EditorState(
    val tool: EditorTool = EditorTool.SELECT,
    val selection: EditorSelection = EditorSelection.None,
    val drawing: Drawing2D = Drawing2D(nodes = emptyList(), members = emptyList()),
    val isLoading: Boolean = true,
    val lastError: String? = null,
    val dirtyFlag: Boolean = false,
    val viewTransform: ViewTransform = ViewTransform(),
    /** Underlay image state (if workspace has an underlay configured). */
    val underlayState: UnderlayState = UnderlayState.None,
)
