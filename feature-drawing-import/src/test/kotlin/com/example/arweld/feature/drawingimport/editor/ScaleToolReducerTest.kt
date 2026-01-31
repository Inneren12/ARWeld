package com.example.arweld.feature.drawingimport.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScaleToolReducerTest {
    @Test
    fun `first tap sets point A only`() {
        val state = EditorState(tool = EditorTool.SCALE)

        val next = reduceEditorState(state, EditorIntent.ScaleTap(Vec2(12f, 8f)))

        assertEquals(Vec2(12f, 8f), next.scaleDraft?.pointA)
        assertNull(next.scaleDraft?.pointB)
    }

    @Test
    fun `second tap sets point B`() {
        val state = EditorState(
            tool = EditorTool.SCALE,
            scaleDraft = ScaleDraft(pointA = Vec2(1f, 2f), pointB = null),
        )

        val next = reduceEditorState(state, EditorIntent.ScaleTap(Vec2(4f, 6f)))

        assertEquals(Vec2(1f, 2f), next.scaleDraft?.pointA)
        assertEquals(Vec2(4f, 6f), next.scaleDraft?.pointB)
    }

    @Test
    fun `third tap resets point A and clears point B`() {
        val state = EditorState(
            tool = EditorTool.SCALE,
            scaleDraft = ScaleDraft(pointA = Vec2(1f, 2f), pointB = Vec2(3f, 4f)),
        )

        val next = reduceEditorState(state, EditorIntent.ScaleTap(Vec2(9f, 10f)))

        assertEquals(Vec2(9f, 10f), next.scaleDraft?.pointA)
        assertNull(next.scaleDraft?.pointB)
    }

    @Test
    fun `tool change away from scale clears draft`() {
        val state = EditorState(
            tool = EditorTool.SCALE,
            scaleDraft = ScaleDraft(pointA = Vec2(1f, 2f), pointB = Vec2(3f, 4f)),
        )

        val next = reduceEditorState(state, EditorIntent.ToolChanged(EditorTool.SELECT))

        assertEquals(EditorTool.SELECT, next.tool)
        assertNull(next.scaleDraft)
    }

    @Test
    fun `tool change to scale keeps existing draft`() {
        val state = EditorState(
            tool = EditorTool.PAN,
            scaleDraft = ScaleDraft(pointA = Vec2(1f, 2f), pointB = null),
        )

        val next = reduceEditorState(state, EditorIntent.ToolChanged(EditorTool.SCALE))

        assertEquals(EditorTool.SCALE, next.tool)
        assertEquals(Vec2(1f, 2f), next.scaleDraft?.pointA)
        assertNull(next.scaleDraft?.pointB)
    }

    @Test
    fun `scale tap ignored when tool is not scale`() {
        val state = EditorState(tool = EditorTool.SELECT)

        val next = reduceEditorState(state, EditorIntent.ScaleTap(Vec2(1f, 1f)))

        assertTrue(next.scaleDraft == null)
    }
}
