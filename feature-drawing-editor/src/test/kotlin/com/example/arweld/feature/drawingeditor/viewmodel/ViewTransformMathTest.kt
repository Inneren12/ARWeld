package com.example.arweld.feature.drawingeditor.viewmodel

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewTransformMathTest {

    private val epsilon = 0.0005f

    @Test
    fun `world to screen round trip is consistent`() {
        val transform = ViewTransform(scale = 2.5f, offsetX = 120f, offsetY = -48f)
        val points = listOf(
            Point2(0f, 0f),
            Point2(12.5f, -33.75f),
            Point2(-80f, 44f),
        )

        points.forEach { world ->
            val screen = worldToScreen(transform, world)
            val roundTrip = screenToWorld(transform, screen)
            assertClose(world.x, roundTrip.x)
            assertClose(world.y, roundTrip.y)
        }
    }

    @Test
    fun `screen to world round trip is consistent`() {
        val transform = ViewTransform(scale = 0.75f, offsetX = -16f, offsetY = 210f)
        val points = listOf(
            Point2(0f, 0f),
            Point2(320f, 240f),
            Point2(-50f, 1080f),
        )

        points.forEach { screen ->
            val world = screenToWorld(transform, screen)
            val roundTrip = worldToScreen(transform, world)
            assertClose(screen.x, roundTrip.x)
            assertClose(screen.y, roundTrip.y)
        }
    }

    @Test
    fun `zoom around focal point preserves screen mapping`() {
        val transform = ViewTransform(scale = 1.2f, offsetX = 40f, offsetY = -20f)
        val focal = Point2(320f, 200f)
        val worldAtFocal = screenToWorld(transform, focal)

        val zoomed = zoomBy(
            transform = transform,
            zoomFactor = 1.5f,
            focalX = focal.x,
            focalY = focal.y,
        )

        val focalAfterZoom = worldToScreen(zoomed, worldAtFocal)
        assertClose(focal.x, focalAfterZoom.x)
        assertClose(focal.y, focalAfterZoom.y)
    }

    @Test
    fun `zoom clamps to maximum scale`() {
        val transform = ViewTransform(scale = 5.9f, offsetX = 0f, offsetY = 0f)
        val zoomed = zoomBy(
            transform = transform,
            zoomFactor = 2f,
            focalX = 0f,
            focalY = 0f,
        )

        assertTrue("Scale should be clamped", zoomed.scale <= 6f)
    }

    private fun assertClose(expected: Float, actual: Float) {
        assertEquals(expected, actual, epsilon)
        assertTrue(abs(expected - actual) <= epsilon)
    }
}
