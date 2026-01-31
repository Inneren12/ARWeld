package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Test

class NodeEditReducerTest {

    @Test
    fun `node edit input updates draft and validates`() {
        val drawing = Drawing2D(
            nodes = listOf(Node2D(id = "N1", x = 1.0, y = 2.0)),
            members = emptyList(),
        )
        val selected = reduceEditorState(
            EditorState(drawing = drawing, selection = EditorSelection.None),
            EditorIntent.SelectEntity(EditorSelection.Node("N1"))
        )

        val updated = reduceEditorState(selected, EditorIntent.NodeEditXChanged("12,5"))

        assertEquals("12,5", updated.nodeEditDraft.xText)
        assertNotNull(updated.nodeEditDraft.xError)
        assertEquals(drawing, updated.drawing)
    }

    @Test
    fun `node edit apply updates node coordinates`() {
        val baseDrawing = Drawing2D(
            nodes = listOf(Node2D(id = "N1", x = 1.0, y = 2.0)),
            members = emptyList(),
        )
        val updatedDrawing = baseDrawing.copy(
            nodes = listOf(Node2D(id = "N1", x = -5.5, y = 10.25)),
        )
        val state = reduceEditorState(
            EditorState(drawing = baseDrawing, selection = EditorSelection.Node("N1")),
            EditorIntent.SelectEntity(EditorSelection.Node("N1"))
        )

        val applied = reduceEditorState(state, EditorIntent.NodeEditApplied(updatedDrawing, "N1"))

        assertEquals(-5.5, applied.drawing.nodes.first().x, 0.0)
        assertEquals(10.25, applied.drawing.nodes.first().y, 0.0)
        assertEquals(1, applied.undoStack.size)
        assertNull(applied.nodeEditDraft.applyError)
    }

    @Test
    fun `node edit apply failure records error without mutation`() {
        val drawing = Drawing2D(
            nodes = listOf(Node2D(id = "N1", x = 1.0, y = 2.0)),
            members = emptyList(),
        )
        val state = EditorState(drawing = drawing, selection = EditorSelection.Node("N1"))

        val failed = reduceEditorState(state, EditorIntent.NodeEditApplyFailed("Enter a valid X coordinate."))

        assertEquals("Enter a valid X coordinate.", failed.nodeEditDraft.applyError)
        assertEquals(drawing, failed.drawing)
    }
}
