package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Point2D
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScaleDraftReducerTest {

    @Test
    fun `scale draft computes pending mmPerPx from input length`() {
        val initial = EditorState()
        val withPointA = reduceEditorState(initial, EditorIntent.ScalePointSelected(Point2D(0.0, 0.0)))
        val withPointB = reduceEditorState(withPointA, EditorIntent.ScalePointSelected(Point2D(0.0, 100.0)))

        val updated = reduceEditorState(withPointB, EditorIntent.ScaleLengthChanged("250"))

        assertEquals(100.0, updated.scaleDraft.pendingDistancePx)
        assertEquals(2.5, updated.scaleDraft.pendingMmPerPx)
        assertNull(updated.scaleDraft.inputError)
    }

    @Test
    fun `scale draft reports error on non-positive length`() {
        val initial = EditorState()
        val withPointA = reduceEditorState(initial, EditorIntent.ScalePointSelected(Point2D(0.0, 0.0)))
        val withPointB = reduceEditorState(withPointA, EditorIntent.ScalePointSelected(Point2D(0.0, 10.0)))

        val updated = reduceEditorState(withPointB, EditorIntent.ScaleLengthChanged("0"))

        assertEquals("Length must be > 0 mm.", updated.scaleDraft.inputError)
        assertNull(updated.scaleDraft.pendingMmPerPx)
    }

    @Test
    fun `scale draft reports error on tiny distance`() {
        val initial = EditorState()
        val withPointA = reduceEditorState(initial, EditorIntent.ScalePointSelected(Point2D(0.0, 0.0)))
        val updated = reduceEditorState(withPointA, EditorIntent.ScalePointSelected(Point2D(0.0, 1e-9)))

        assertEquals("Points are too close. Select two distinct points.", updated.scaleDraft.inputError)
        assertNull(updated.scaleDraft.pendingMmPerPx)
    }
}
