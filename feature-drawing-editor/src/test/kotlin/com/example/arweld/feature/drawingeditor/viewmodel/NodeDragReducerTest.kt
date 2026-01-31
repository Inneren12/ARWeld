package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NodeDragReducerTest {

    @Test
    fun `drag move updates node position deterministically`() {
        val drawing = Drawing2D(nodes = listOf(Node2D(id = "N1", x = 0.0, y = 0.0)), members = emptyList())
        val initial = EditorState(tool = EditorTool.NODE, drawing = drawing, isLoading = false)

        val started = reduceEditorState(
            initial,
            EditorIntent.NodeDragStart(nodeId = "N1", startPointerWorld = Point2D(1.0, 1.0))
        )
        val moved = reduceEditorState(started, EditorIntent.NodeDragMove(pointerWorld = Point2D(2.0, 3.0)))

        val node = moved.drawing.nodes.first()
        assertEquals(1.0, node.x, 0.0)
        assertEquals(2.0, node.y, 0.0)
        assertNotNull(moved.nodeDragState)
        assertEquals(0, moved.undoStack.size)
    }

    @Test
    fun `drag end commits history and clears drag state`() {
        val drawing = Drawing2D(nodes = listOf(Node2D(id = "N1", x = 2.0, y = 4.0)), members = emptyList())
        val initial = EditorState(tool = EditorTool.NODE, drawing = drawing, isLoading = false)

        val started = reduceEditorState(
            initial,
            EditorIntent.NodeDragStart(nodeId = "N1", startPointerWorld = Point2D(5.0, 5.0))
        )
        val moved = reduceEditorState(started, EditorIntent.NodeDragMove(pointerWorld = Point2D(6.0, 7.0)))
        val ended = reduceEditorState(moved, EditorIntent.NodeDragEnd(pointerWorld = Point2D(6.0, 7.0)))

        val node = ended.drawing.nodes.first()
        assertEquals(3.0, node.x, 0.0)
        assertEquals(6.0, node.y, 0.0)
        assertEquals(1, ended.undoStack.size)
        assertEquals(0, ended.redoStack.size)
        assertNull(ended.nodeDragState)

        val previousNode = ended.undoStack.first().nodes.first()
        assertEquals(2.0, previousNode.x, 0.0)
        assertEquals(4.0, previousNode.y, 0.0)
    }
}
