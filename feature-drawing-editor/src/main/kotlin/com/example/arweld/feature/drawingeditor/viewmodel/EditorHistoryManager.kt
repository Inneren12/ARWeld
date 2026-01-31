package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D

data class EditorHistory(
    val undoStack: List<Drawing2D> = emptyList(),
    val redoStack: List<Drawing2D> = emptyList(),
)

data class EditorHistoryResult(
    val drawing: Drawing2D,
    val history: EditorHistory,
)

object EditorHistoryManager {
    const val MAX_DEPTH = 50

    fun push(
        history: EditorHistory,
        currentDrawing: Drawing2D,
        maxDepth: Int = MAX_DEPTH,
    ): EditorHistory {
        val undoStack = (history.undoStack + currentDrawing).takeLast(maxDepth)
        return history.copy(undoStack = undoStack, redoStack = emptyList())
    }

    fun undo(
        history: EditorHistory,
        currentDrawing: Drawing2D,
        maxDepth: Int = MAX_DEPTH,
    ): EditorHistoryResult? {
        if (history.undoStack.isEmpty()) {
            return null
        }
        val newDrawing = history.undoStack.last()
        val redoStack = (history.redoStack + currentDrawing).takeLast(maxDepth)
        val updated = history.copy(
            undoStack = history.undoStack.dropLast(1),
            redoStack = redoStack,
        )
        return EditorHistoryResult(newDrawing, updated)
    }

    fun redo(
        history: EditorHistory,
        currentDrawing: Drawing2D,
        maxDepth: Int = MAX_DEPTH,
    ): EditorHistoryResult? {
        if (history.redoStack.isEmpty()) {
            return null
        }
        val newDrawing = history.redoStack.last()
        val undoStack = (history.undoStack + currentDrawing).takeLast(maxDepth)
        val updated = history.copy(
            undoStack = undoStack,
            redoStack = history.redoStack.dropLast(1),
        )
        return EditorHistoryResult(newDrawing, updated)
    }
}
