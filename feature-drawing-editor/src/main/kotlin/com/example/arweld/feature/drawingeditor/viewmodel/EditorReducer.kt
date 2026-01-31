package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Drawing2DIdAllocator
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.core.drawing2d.editor.v1.canonicalize
import com.example.arweld.feature.drawingeditor.hittest.hitTestNode

fun reduceEditorState(state: EditorState, intent: EditorIntent): EditorState = when (intent) {
    is EditorIntent.ToolChanged -> state.copy(
        tool = intent.tool,
        selection = EditorSelection.None,
        nodeDragState = null,
        scaleDraft = if (intent.tool == EditorTool.SCALE) {
            state.scaleDraft
        } else {
            ScaleDraft()
        },
    )
    is EditorIntent.SelectEntity -> state.copy(selection = intent.selection)
    EditorIntent.ClearSelection -> state.copy(selection = EditorSelection.None, nodeDragState = null)
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
        nodeDragState = null,
        undoStack = emptyList(),
        redoStack = emptyList(),
    )
    EditorIntent.SaveRequested -> state.copy(
        isLoading = true,
        lastError = null,
    )
    EditorIntent.Saved -> state.copy(
        isLoading = false,
        lastError = null,
        dirtyFlag = false,
        nodeDragState = null,
    )
    is EditorIntent.Error -> state.copy(
        isLoading = false,
        lastError = intent.message,
    )
    is EditorIntent.ScalePointSelected -> {
        val draft = state.scaleDraft
        val updatedDraft = when {
            draft.pointA == null -> draft.copy(pointA = intent.point)
            draft.pointB == null -> draft.copy(pointB = intent.point)
            else -> draft.copy(pointA = intent.point, pointB = null)
        }
        state.copy(scaleDraft = recomputeScaleDraft(updatedDraft))
    }
    is EditorIntent.ScaleLengthChanged -> {
        val updatedDraft = state.scaleDraft.copy(inputText = intent.text)
        state.copy(scaleDraft = recomputeScaleDraft(updatedDraft))
    }
    EditorIntent.ScaleApplyRequested -> state.copy(
        scaleDraft = state.scaleDraft.copy(applyError = null),
    )
    is EditorIntent.ScaleApplied -> state.copy(
        scaleDraft = ScaleDraft(),
    ).pushHistory(intent.drawing)
    is EditorIntent.ScaleApplyFailed -> state.copy(
        scaleDraft = state.scaleDraft.copy(applyError = intent.message),
    )
    EditorIntent.ScaleResetRequested -> state.copy(
        scaleDraft = state.scaleDraft.copy(applyError = null),
    )
    is EditorIntent.ScaleResetApplied -> state.copy(
        scaleDraft = ScaleDraft(),
    ).pushHistory(intent.drawing)
    is EditorIntent.ScaleResetFailed -> state.copy(
        scaleDraft = state.scaleDraft.copy(applyError = intent.message),
    )
    is EditorIntent.DrawingMutationApplied -> state.pushHistory(intent.drawing)
    is EditorIntent.NodeTap -> {
        if (state.tool != EditorTool.NODE) {
            state
        } else if (state.nodeDragState != null) {
            state
        } else {
            val hitId = hitTestNode(
                worldTap = intent.worldPoint,
                nodes = state.drawing.nodes,
                tolerancePx = intent.tolerancePx,
                viewTransform = state.viewTransform,
            )
            if (hitId != null) {
                state.copy(selection = EditorSelection.Node(hitId))
            } else {
                val allocation = Drawing2DIdAllocator.allocateNodeId(state.drawing)
                val newNode = Node2D(
                    id = allocation.id,
                    x = intent.worldPoint.x,
                    y = intent.worldPoint.y,
                )
                val updatedDrawing = allocation.drawing
                    .copy(nodes = allocation.drawing.nodes + newNode)
                    .canonicalize()
                state.copy(selection = EditorSelection.Node(allocation.id))
                    .pushHistory(updatedDrawing)
            }
        }
    }
    }
    is EditorIntent.NodeDragStart -> {
        if (state.tool != EditorTool.NODE || state.nodeDragState != null) {
            state
        } else {
            val node = state.drawing.nodes.firstOrNull { it.id == intent.nodeId } ?: return state
            state.copy(
                selection = EditorSelection.Node(node.id),
                nodeDragState = NodeDragState(
                    nodeId = node.id,
                    startWorldPos = Point2D(node.x, node.y),
                    startPointerWorld = intent.startPointerWorld,
                ),
            )
        }
    }
    is EditorIntent.NodeDragMove -> {
        val drag = state.nodeDragState ?: return state
        val updatedPos = applyNodeDrag(drag, intent.pointerWorld)
        val updatedDrawing = updateNodePosition(state.drawing, drag.nodeId, updatedPos)
        if (updatedDrawing == state.drawing) {
            state
        } else {
            state.copy(
                drawing = updatedDrawing,
                selection = EditorSelection.Node(drag.nodeId),
            )
        }
    }
    is EditorIntent.NodeDragEnd -> {
        val drag = state.nodeDragState ?: return state
        val updatedPos = applyNodeDrag(drag, intent.pointerWorld)
        val updatedDrawing = updateNodePosition(state.drawing, drag.nodeId, updatedPos)
        val previousDrawing = updateNodePosition(state.drawing, drag.nodeId, drag.startWorldPos)
        if (updatedPos == drag.startWorldPos) {
            state.copy(
                drawing = previousDrawing,
                nodeDragState = null,
                selection = EditorSelection.Node(drag.nodeId),
            )
        } else {
            state.copy(
                nodeDragState = null,
                selection = EditorSelection.Node(drag.nodeId),
            ).pushHistoryFrom(previousDrawing, updatedDrawing)
        }
    }
    EditorIntent.NodeDragCancel -> {
        val drag = state.nodeDragState ?: return state
        val restoredDrawing = updateNodePosition(state.drawing, drag.nodeId, drag.startWorldPos)
        state.copy(
            drawing = restoredDrawing,
            nodeDragState = null,
            selection = EditorSelection.Node(drag.nodeId),
        )
    }
    EditorIntent.UndoRequested -> state.applyHistoryUndo()
    EditorIntent.RedoRequested -> state.applyHistoryRedo()
}

private const val SCALE_DISTANCE_EPSILON = 1e-6

private fun EditorState.pushHistory(newDrawing: Drawing2D): EditorState {
    val history = EditorHistory(undoStack = undoStack, redoStack = redoStack)
    val updated = EditorHistoryManager.push(history, drawing)
    return copy(
        drawing = newDrawing,
        undoStack = updated.undoStack,
        redoStack = updated.redoStack,
        dirtyFlag = false,
        lastError = null,
    )
}

private fun EditorState.pushHistoryFrom(previousDrawing: Drawing2D, newDrawing: Drawing2D): EditorState {
    val history = EditorHistory(undoStack = undoStack, redoStack = redoStack)
    val updated = EditorHistoryManager.push(history, previousDrawing)
    return copy(
        drawing = newDrawing,
        undoStack = updated.undoStack,
        redoStack = updated.redoStack,
        dirtyFlag = false,
        lastError = null,
    )
}

private fun EditorState.applyHistoryUndo(): EditorState {
    val history = EditorHistory(undoStack = undoStack, redoStack = redoStack)
    val updated = EditorHistoryManager.undo(history, drawing) ?: return this
    return copy(
        drawing = updated.drawing,
        undoStack = updated.history.undoStack,
        redoStack = updated.history.redoStack,
        dirtyFlag = false,
    )
}

private fun EditorState.applyHistoryRedo(): EditorState {
    val history = EditorHistory(undoStack = undoStack, redoStack = redoStack)
    val updated = EditorHistoryManager.redo(history, drawing) ?: return this
    return copy(
        drawing = updated.drawing,
        undoStack = updated.history.undoStack,
        redoStack = updated.history.redoStack,
        dirtyFlag = false,
    )
}

private fun applyNodeDrag(
    drag: NodeDragState,
    pointerWorld: Point2D,
): Point2D {
    val deltaX = pointerWorld.x - drag.startPointerWorld.x
    val deltaY = pointerWorld.y - drag.startPointerWorld.y
    return Point2D(
        x = drag.startWorldPos.x + deltaX,
        y = drag.startWorldPos.y + deltaY,
    )
}

private fun updateNodePosition(
    drawing: Drawing2D,
    nodeId: String,
    worldPos: Point2D,
): Drawing2D {
    var updated = false
    val nodes = drawing.nodes.map { node ->
        if (node.id == nodeId) {
            updated = true
            node.copy(x = worldPos.x, y = worldPos.y)
        } else {
            node
        }
    }
    return if (updated) {
        drawing.copy(nodes = nodes)
    } else {
        drawing
    }
}

private fun recomputeScaleDraft(draft: ScaleDraft): ScaleDraft {
    val pointA = draft.pointA
    val pointB = draft.pointB
    if (pointA == null || pointB == null) {
        return draft.copy(
            inputError = null,
            applyError = null,
            pendingDistancePx = null,
            pendingMmPerPx = null,
        )
    }

    val distance = distanceBetween(pointA, pointB)
    if (distance <= SCALE_DISTANCE_EPSILON) {
        return draft.copy(
            inputError = "Points are too close. Select two distinct points.",
            applyError = null,
            pendingDistancePx = null,
            pendingMmPerPx = null,
        )
    }

    val input = draft.inputText.trim()
    if (input.isEmpty()) {
        return draft.copy(
            inputError = null,
            applyError = null,
            pendingDistancePx = distance,
            pendingMmPerPx = null,
        )
    }

    val parsedLength = parseStrictNumber(input)
    if (parsedLength == null) {
        return draft.copy(
            inputError = "Enter a valid length in mm (e.g., 1500.0).",
            applyError = null,
            pendingDistancePx = distance,
            pendingMmPerPx = null,
        )
    }
    if (parsedLength <= 0.0) {
        return draft.copy(
            inputError = "Length must be > 0 mm.",
            applyError = null,
            pendingDistancePx = distance,
            pendingMmPerPx = null,
        )
    }

    return draft.copy(
        inputError = null,
        applyError = null,
        pendingDistancePx = distance,
        pendingMmPerPx = parsedLength / distance,
    )
}
