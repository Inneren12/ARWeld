package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import java.io.File
import com.example.arweld.core.drawing2d.editor.v1.Point2D

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

data class NodeDragState(
    val nodeId: String,
    val startWorldPos: Point2D,
    val startPointerWorld: Point2D,
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
data class ScaleDraft(
    val pointA: Point2D? = null,
    val pointB: Point2D? = null,
    val inputText: String = "",
    val inputError: String? = null,
    val applyError: String? = null,
    val pendingDistancePx: Double? = null,
    val pendingMmPerPx: Double? = null,
)

data class NodeEditDraft(
    val nodeId: String? = null,
    val xText: String = "",
    val yText: String = "",
    val xError: String? = null,
    val yError: String? = null,
    val applyError: String? = null,
)

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
    val nodeDragState: NodeDragState? = null,
    val scaleDraft: ScaleDraft = ScaleDraft(),
    val nodeEditDraft: NodeEditDraft = NodeEditDraft(),
    val undoStack: List<Drawing2D> = emptyList(),
    val redoStack: List<Drawing2D> = emptyList(),
)
