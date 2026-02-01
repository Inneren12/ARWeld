package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Drawing2DIdAllocator
import com.example.arweld.core.drawing2d.editor.v1.Member2D
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
        nodeEditDraft = NodeEditDraft(),
        memberDraft = if (intent.tool == EditorTool.MEMBER) {
            state.memberDraft
        } else {
            MemberDraft()
        },
        profilePicker = ProfilePickerState(),
        lastErrorCode = null,
    )
    is EditorIntent.SelectEntity -> {
        val synced = syncNodeEditState(intent.selection, state.drawing)
        state.copy(
            selection = synced.selection,
            nodeEditDraft = synced.draft,
            profilePicker = ProfilePickerState(),
        )
    }
    EditorIntent.ClearSelection -> state.copy(
        selection = EditorSelection.None,
        nodeDragState = null,
        nodeEditDraft = NodeEditDraft(),
        profilePicker = ProfilePickerState(),
    )
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
        lastErrorCode = null,
        dirtyFlag = false,
        nodeDragState = null,
        undoStack = emptyList(),
        redoStack = emptyList(),
        nodeEditDraft = NodeEditDraft(),
        memberDraft = MemberDraft(),
        profilePicker = ProfilePickerState(),
    )
    EditorIntent.SaveRequested -> state.copy(
        isLoading = true,
        lastError = null,
        lastErrorCode = null,
    )
    EditorIntent.Saved -> state.copy(
        isLoading = false,
        lastError = null,
        lastErrorCode = null,
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
                val synced = syncNodeEditState(EditorSelection.Node(hitId), state.drawing)
                state.copy(
                    selection = synced.selection,
                    nodeEditDraft = synced.draft,
                )
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
    is EditorIntent.NodeDragStart -> {
        if (state.tool != EditorTool.NODE || state.nodeDragState != null) {
            state
        } else {
            val node = state.drawing.nodes.firstOrNull { it.id == intent.nodeId } ?: return state
            val synced = syncNodeEditState(EditorSelection.Node(node.id), state.drawing)
            state.copy(
                selection = synced.selection,
                nodeDragState = NodeDragState(
                    nodeId = node.id,
                    startWorldPos = Point2D(node.x, node.y),
                    startPointerWorld = intent.startPointerWorld,
                ),
                nodeEditDraft = synced.draft,
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
            val synced = syncNodeEditState(EditorSelection.Node(drag.nodeId), updatedDrawing)
            state.copy(
                drawing = updatedDrawing,
                selection = synced.selection,
                nodeEditDraft = synced.draft,
            )
        }
    }
    is EditorIntent.NodeDragEnd -> {
        val drag = state.nodeDragState ?: return state
        val updatedPos = applyNodeDrag(drag, intent.pointerWorld)
        val updatedDrawing = updateNodePosition(state.drawing, drag.nodeId, updatedPos)
        val previousDrawing = updateNodePosition(state.drawing, drag.nodeId, drag.startWorldPos)
        if (updatedPos == drag.startWorldPos) {
            val synced = syncNodeEditState(EditorSelection.Node(drag.nodeId), previousDrawing)
            state.copy(
                drawing = previousDrawing,
                nodeDragState = null,
                selection = synced.selection,
                nodeEditDraft = synced.draft,
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
        val synced = syncNodeEditState(EditorSelection.Node(drag.nodeId), restoredDrawing)
        state.copy(
            drawing = restoredDrawing,
            nodeDragState = null,
            selection = synced.selection,
            nodeEditDraft = synced.draft,
        )
    }
    is EditorIntent.NodeDeleteRequested -> {
        val updatedDrawing = deleteNodeAndMembers(state.drawing, intent.nodeId)
        if (updatedDrawing == state.drawing) {
            state
        } else {
            state.copy(
                selection = EditorSelection.None,
                nodeDragState = null,
                nodeEditDraft = NodeEditDraft(),
            ).pushHistory(updatedDrawing)
        }
    }
    is EditorIntent.MemberDeleteRequested -> {
        val updatedDrawing = deleteMember(state.drawing, intent.memberId)
        if (updatedDrawing == state.drawing) {
            state
        } else {
            state.copy(
                selection = EditorSelection.None,
                nodeDragState = null,
                nodeEditDraft = NodeEditDraft(),
            ).pushHistory(updatedDrawing)
        }
    }
    is EditorIntent.NodeEditXChanged -> {
        val draft = state.nodeEditDraft
        state.copy(
            nodeEditDraft = draft.copy(
                xText = intent.text,
                xError = validateNodeCoordinateText(intent.text, "X"),
                applyError = null,
            )
        )
    }
    is EditorIntent.NodeEditYChanged -> {
        val draft = state.nodeEditDraft
        state.copy(
            nodeEditDraft = draft.copy(
                yText = intent.text,
                yError = validateNodeCoordinateText(intent.text, "Y"),
                applyError = null,
            )
        )
    }
    is EditorIntent.NodeEditApplyRequested -> state.copy(
        nodeEditDraft = state.nodeEditDraft.copy(applyError = null),
    )
    is EditorIntent.NodeEditApplied -> {
        state.copy(selection = EditorSelection.Node(intent.nodeId))
            .pushHistory(intent.drawing)
    }
    is EditorIntent.NodeEditApplyFailed -> state.copy(
        nodeEditDraft = state.nodeEditDraft.copy(applyError = intent.message),
    )
    is EditorIntent.MemberNodeTapped -> {
        if (state.tool != EditorTool.MEMBER) {
            state
        } else if (state.memberDraft.nodeAId == null) {
            state.copy(
                memberDraft = MemberDraft(nodeAId = intent.nodeId),
                lastErrorCode = null,
            )
        } else {
            val nodeAId = state.memberDraft.nodeAId ?: return state
            if (isSameNode(nodeAId, intent.nodeId)) {
                state.withMemberError(EditorErrorCode.MemberSameNode)
            } else if (isDuplicateMember(state.drawing.members, nodeAId, intent.nodeId)) {
                state.withMemberError(EditorErrorCode.MemberDuplicate)
            } else {
                val allocation = Drawing2DIdAllocator.allocateMemberId(state.drawing)
                val endpoints = canonicalizeMemberEndpoints(nodeAId, intent.nodeId)
                val newMember = Member2D(
                    id = allocation.id,
                    aNodeId = endpoints.first,
                    bNodeId = endpoints.second,
                )
                val updatedDrawing = allocation.drawing
                    .copy(members = allocation.drawing.members + newMember)
                    .canonicalize()
                state.copy(
                    selection = EditorSelection.Member(newMember.id),
                    memberDraft = MemberDraft(),
                    lastErrorCode = null,
                ).pushHistory(updatedDrawing)
            }
        }
    }
    is EditorIntent.ProfilePickerOpen -> state.copy(
        profilePicker = ProfilePickerState(
            isOpen = true,
            memberId = intent.memberId,
            isLoading = true,
        ),
    )
    EditorIntent.ProfilePickerClose -> state.copy(
        profilePicker = ProfilePickerState(),
    )
    is EditorIntent.ProfileQueryChanged -> state.copy(
        profilePicker = state.profilePicker.copy(
            queryText = intent.text,
            isLoading = true,
            lastError = null,
        ),
    )
    is EditorIntent.ProfileSearchRequested -> state.copy(
        profilePicker = state.profilePicker.copy(
            isLoading = true,
            lastError = null,
        ),
    )
    is EditorIntent.ProfileSearchSucceeded -> state.copy(
        profilePicker = state.profilePicker.copy(
            results = intent.results,
            isLoading = false,
            lastError = null,
        ),
    )
    is EditorIntent.ProfileSearchFailed -> state.copy(
        profilePicker = state.profilePicker.copy(
            isLoading = false,
            lastError = intent.message,
        ),
    )
    is EditorIntent.ProfileSelectionApplied -> state.copy(
        profilePicker = ProfilePickerState(),
    ).pushHistory(intent.drawing)
    is EditorIntent.ProfileSelectionFailed -> state.copy(
        profilePicker = state.profilePicker.copy(
            isLoading = false,
            lastError = intent.message,
        ),
    )
    EditorIntent.UndoRequested -> state.applyHistoryUndo()
    EditorIntent.RedoRequested -> state.applyHistoryRedo()
}

private const val SCALE_DISTANCE_EPSILON = 1e-6

private fun EditorState.pushHistory(newDrawing: Drawing2D): EditorState {
    val history = EditorHistory(undoStack = undoStack, redoStack = redoStack)
    val updated = EditorHistoryManager.push(history, drawing)
    val synced = syncNodeEditState(selection, newDrawing)
    return copy(
        drawing = newDrawing,
        undoStack = updated.undoStack,
        redoStack = updated.redoStack,
        dirtyFlag = false,
        lastError = null,
        selection = synced.selection,
        nodeEditDraft = synced.draft,
    )
}

private fun EditorState.withMemberError(code: EditorErrorCode): EditorState = copy(
    lastErrorCode = code,
    lastErrorSequence = lastErrorSequence + 1,
)

private fun EditorState.pushHistoryFrom(previousDrawing: Drawing2D, newDrawing: Drawing2D): EditorState {
    val history = EditorHistory(undoStack = undoStack, redoStack = redoStack)
    val updated = EditorHistoryManager.push(history, previousDrawing)
    val synced = syncNodeEditState(selection, newDrawing)
    return copy(
        drawing = newDrawing,
        undoStack = updated.undoStack,
        redoStack = updated.redoStack,
        dirtyFlag = false,
        lastError = null,
        selection = synced.selection,
        nodeEditDraft = synced.draft,
    )
}

private fun EditorState.applyHistoryUndo(): EditorState {
    val history = EditorHistory(undoStack = undoStack, redoStack = redoStack)
    val updated = EditorHistoryManager.undo(history, drawing) ?: return this
    val synced = syncNodeEditState(selection, updated.drawing)
    return copy(
        drawing = updated.drawing,
        undoStack = updated.history.undoStack,
        redoStack = updated.history.redoStack,
        dirtyFlag = false,
        selection = synced.selection,
        nodeEditDraft = synced.draft,
    )
}

private fun EditorState.applyHistoryRedo(): EditorState {
    val history = EditorHistory(undoStack = undoStack, redoStack = redoStack)
    val updated = EditorHistoryManager.redo(history, drawing) ?: return this
    val synced = syncNodeEditState(selection, updated.drawing)
    return copy(
        drawing = updated.drawing,
        undoStack = updated.history.undoStack,
        redoStack = updated.history.redoStack,
        dirtyFlag = false,
        selection = synced.selection,
        nodeEditDraft = synced.draft,
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

private fun deleteNodeAndMembers(
    drawing: Drawing2D,
    nodeId: String,
): Drawing2D {
    if (drawing.nodes.none { it.id == nodeId }) {
        return drawing
    }
    val remainingNodes = drawing.nodes.filterNot { it.id == nodeId }
    val remainingMembers = drawing.members.filterNot { member ->
        member.aNodeId == nodeId || member.bNodeId == nodeId
    }
    return drawing.copy(nodes = remainingNodes, members = remainingMembers).canonicalize()
}

private fun deleteMember(
    drawing: Drawing2D,
    memberId: String,
): Drawing2D {
    if (drawing.members.none { it.id == memberId }) {
        return drawing
    }
    val remainingMembers = drawing.members.filterNot { it.id == memberId }
    return drawing.copy(members = remainingMembers).canonicalize()
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

private fun canonicalizeMemberEndpoints(nodeAId: String, nodeBId: String): Pair<String, String> {
    return if (nodeAId <= nodeBId) {
        nodeAId to nodeBId
    } else {
        nodeBId to nodeAId
    }
}

private fun isSameNode(nodeAId: String, nodeBId: String): Boolean = nodeAId == nodeBId

private fun isDuplicateMember(
    existingMembers: List<Member2D>,
    nodeAId: String,
    nodeBId: String,
): Boolean {
    val (candidateA, candidateB) = canonicalizeMemberEndpoints(nodeAId, nodeBId)
    return existingMembers.any { member ->
        val (memberA, memberB) = canonicalizeMemberEndpoints(member.aNodeId, member.bNodeId)
        memberA == candidateA && memberB == candidateB
    }
}

private data class SyncedNodeEditState(
    val selection: EditorSelection,
    val draft: NodeEditDraft,
)

private fun syncNodeEditState(selection: EditorSelection, drawing: Drawing2D): SyncedNodeEditState {
    return if (selection is EditorSelection.Node) {
        val node = drawing.nodes.firstOrNull { it.id == selection.id }
        if (node == null) {
            SyncedNodeEditState(EditorSelection.None, NodeEditDraft())
        } else {
            SyncedNodeEditState(selection, buildNodeEditDraft(node))
        }
    } else {
        SyncedNodeEditState(selection, NodeEditDraft())
    }
}
