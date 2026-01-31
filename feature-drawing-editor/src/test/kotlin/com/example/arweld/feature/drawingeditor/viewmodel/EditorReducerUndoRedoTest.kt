package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.core.drawing2d.editor.v1.ScaleInfo
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class EditorReducerUndoRedoTest {

    @Test
    fun `undo redo restores scale state`() {
        val baseDrawing = Drawing2D(nodes = emptyList(), members = emptyList())
        val scaledDrawing = baseDrawing.copy(
            scale = ScaleInfo(
                pointA = Point2D(0.0, 0.0),
                pointB = Point2D(0.0, 25.0),
                realLengthMm = 250.0,
            )
        )
        val initialState = EditorState(drawing = baseDrawing)
        val applied = reduceEditorState(initialState, EditorIntent.ScaleApplied(scaledDrawing))
        assertEquals(scaledDrawing, applied.drawing)
        assertEquals(1, applied.undoStack.size)

        val undone = reduceEditorState(applied, EditorIntent.UndoRequested)
        assertEquals(baseDrawing, undone.drawing)
        assertNull(undone.drawing.scale)
        assertEquals(1, undone.redoStack.size)

        val redone = reduceEditorState(undone, EditorIntent.RedoRequested)
        assertEquals(scaledDrawing, redone.drawing)
        assertEquals(1, redone.undoStack.size)
        assertEquals(0, redone.redoStack.size)
    }
}
