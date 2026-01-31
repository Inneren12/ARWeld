package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class EditorReducerHistoryTest {

    @Test
    fun `node mutation undo redo restores drawing`() {
        val base = drawingWithNodes(0)
        val added = drawingWithNodes(1)
        val initial = EditorState(drawing = base)

        val mutated = reduceEditorState(initial, EditorIntent.DrawingMutationApplied(added))
        assertEquals(added, mutated.drawing)
        assertEquals(1, mutated.undoStack.size)

        val undone = reduceEditorState(mutated, EditorIntent.UndoRequested)
        assertEquals(base, undone.drawing)
        assertTrue(undone.redoStack.isNotEmpty())

        val redone = reduceEditorState(undone, EditorIntent.RedoRequested)
        assertEquals(added, redone.drawing)
        assertEquals(1, redone.undoStack.size)
        assertTrue(redone.redoStack.isEmpty())
    }

    @Test
    fun `redo is cleared after new mutation following undo`() {
        val base = drawingWithNodes(0)
        val first = drawingWithNodes(1)
        val second = drawingWithNodes(2)
        val third = drawingWithNodes(3)

        val initial = EditorState(drawing = base)
        val afterFirst = reduceEditorState(initial, EditorIntent.DrawingMutationApplied(first))
        val afterSecond = reduceEditorState(afterFirst, EditorIntent.DrawingMutationApplied(second))
        val undone = reduceEditorState(afterSecond, EditorIntent.UndoRequested)
        val afterThird = reduceEditorState(undone, EditorIntent.DrawingMutationApplied(third))

        assertTrue(afterThird.redoStack.isEmpty())
        assertEquals(listOf(base, first), afterThird.undoStack)
    }

    @Test
    fun `history stack enforces max depth`() {
        val maxDepth = EditorHistoryManager.MAX_DEPTH
        var state = EditorState(drawing = drawingWithNodes(0))
        for (count in 1..(maxDepth + 5)) {
            state = reduceEditorState(state, EditorIntent.DrawingMutationApplied(drawingWithNodes(count)))
        }

        assertEquals(maxDepth, state.undoStack.size)
        assertEquals(drawingWithNodes(5), state.undoStack.first())
    }

    private fun drawingWithNodes(count: Int): Drawing2D {
        return Drawing2D(
            nodes = (1..count).map { index ->
                Node2D(
                    id = "n$index",
                    x = index.toDouble(),
                    y = index.toDouble(),
                )
            },
            members = emptyList(),
        )
    }
}
