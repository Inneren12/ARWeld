package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Drawing2DIdAllocator
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.feature.drawingeditor.editor.EditorVec2

sealed interface ManualEditorIntent {
    data class ToolChanged(val tool: ManualEditorTool) : ManualEditorIntent

    data class CanvasTap(val worldPoint: EditorVec2) : ManualEditorIntent
}

data class ManualEditorReduceResult(
    val state: ManualEditorUiState,
    val persistDrawing: Drawing2D? = null,
)

fun reduceManualEditorState(
    state: ManualEditorUiState,
    intent: ManualEditorIntent,
): ManualEditorReduceResult {
    return when (intent) {
        is ManualEditorIntent.ToolChanged -> {
            ManualEditorReduceResult(state.copy(selectedTool = intent.tool))
        }
        is ManualEditorIntent.CanvasTap -> {
            if (state.selectedTool != ManualEditorTool.NODE) {
                return ManualEditorReduceResult(state)
            }
            val hitNode = findHitNode(state, intent.worldPoint)
            if (hitNode != null) {
                return ManualEditorReduceResult(
                    state.copy(selection = ManualEditorSelection.Node(hitNode.id))
                )
            }
            val allocation = Drawing2DIdAllocator.allocateNodeId(state.drawing)
            val newNode = Node2D(
                id = allocation.id,
                x = intent.worldPoint.x.toDouble(),
                y = intent.worldPoint.y.toDouble(),
            )
            val updatedDrawing = allocation.drawing.copy(
                nodes = allocation.drawing.nodes + newNode
            )
            ManualEditorReduceResult(
                state.copy(
                    drawing = updatedDrawing,
                    summary = updatedDrawing.toSummary(),
                    selection = ManualEditorSelection.Node(allocation.id),
                    undoStack = state.undoStack + state.drawing,
                ),
                persistDrawing = updatedDrawing,
            )
        }
    }
}

private fun findHitNode(
    state: ManualEditorUiState,
    worldPoint: EditorVec2,
): Node2D? {
    val hitRadiusWorld = HIT_RADIUS_PX / state.viewTransform.scale
    val nearest = state.drawing.nodes.minByOrNull { node ->
        worldPoint.distanceTo(node.toVec2())
    }
    val distance = nearest?.let { worldPoint.distanceTo(it.toVec2()) } ?: return null
    return if (distance <= hitRadiusWorld) nearest else null
}

private fun Node2D.toVec2(): EditorVec2 = EditorVec2(x.toFloat(), y.toFloat())

private const val HIT_RADIUS_PX = 24f
