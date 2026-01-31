package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Drawing2DEditorMetaV1
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.feature.drawingeditor.editor.EditorVec2
import com.example.arweld.feature.drawingeditor.editor.ViewTransform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualEditorReducerTest {

    @Test
    fun `node tap on empty adds node with deterministic id and selects it`() {
        val initialDrawing = Drawing2D(
            nodes = emptyList(),
            members = emptyList(),
            meta = Drawing2DEditorMetaV1(nextNodeId = 1)
        )
        val state = ManualEditorUiState(
            selectedTool = ManualEditorTool.NODE,
            isLoading = false,
            drawing = initialDrawing,
            summary = initialDrawing.toSummary(),
            viewTransform = ViewTransform.identity(),
        )
        val result = reduceManualEditorState(
            state,
            ManualEditorIntent.CanvasTap(EditorVec2(100f, 200f))
        )

        val updated = result.state.drawing
        assertEquals(1, updated.nodes.size)
        assertEquals("N000001", updated.nodes.first().id)
        assertEquals(100.0, updated.nodes.first().x, 0.0)
        assertEquals(200.0, updated.nodes.first().y, 0.0)
        assertEquals(2, updated.meta?.nextNodeId)
        assertEquals(ManualEditorSelection.Node("N000001"), result.state.selection)
        assertEquals(1, result.state.undoStack.size)
        assertEquals(initialDrawing, result.state.undoStack.first())
        assertEquals(updated, result.persistDrawing)
    }

    @Test
    fun `node tap near existing node selects it without creating`() {
        val existingNode = Node2D(id = "N000005", x = 10.0, y = 10.0)
        val initialDrawing = Drawing2D(
            nodes = listOf(existingNode),
            members = emptyList(),
        )
        val state = ManualEditorUiState(
            selectedTool = ManualEditorTool.NODE,
            isLoading = false,
            drawing = initialDrawing,
            summary = initialDrawing.toSummary(),
            viewTransform = ViewTransform.identity(),
        )
        val result = reduceManualEditorState(
            state,
            ManualEditorIntent.CanvasTap(EditorVec2(12f, 11f))
        )

        assertEquals(1, result.state.drawing.nodes.size)
        assertEquals(ManualEditorSelection.Node("N000005"), result.state.selection)
        assertTrue(result.state.undoStack.isEmpty())
        assertEquals(null, result.persistDrawing)
    }
}
