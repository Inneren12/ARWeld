package com.example.arweld.feature.arview.marker

import android.graphics.Point
import android.graphics.PointF
import com.example.arweld.feature.arview.geometry.Point2f
import com.example.arweld.feature.arview.geometry.orderCornersClockwiseFromTopLeft
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RealMarkerDetectorTest {

    private val detector = RealMarkerDetector()
    private val epsilon = 1e-3f

    @Test
    fun `maps camera point to image space for standard rotations`() {
        val width = 100
        val height = 50
        val point = Point(10, 5)

        val mappings = mapOf(
            0 to PointF(10f, 5f),
            90 to PointF(5f, 90f),
            180 to PointF(90f, 45f),
            270 to PointF(45f, 10f),
        )

        mappings.forEach { (rotation, expected) ->
            val mapped = detector.mapToImageSpace(point, width, height, rotation)
            assertThat(mapped.x).isWithin(epsilon).of(expected.x)
            assertThat(mapped.y).isWithin(epsilon).of(expected.y)
        }
    }

    @Test
    fun `orders marker corners clockwise from top-left`() {
        // Use Point2f directly to avoid PointF issues in JVM unit tests
        val unordered = listOf(
            Point2f(50f, 80f),
            Point2f(10f, 10f),
            Point2f(90f, 20f),
            Point2f(20f, 70f),
        )

        val ordered = orderCornersClockwiseFromTopLeft(unordered)

        val orderedPairs = ordered.map { it.x to it.y }
        assertThat(orderedPairs).containsExactly(
            10f to 10f,
            90f to 20f,
            50f to 80f,
            20f to 70f,
        ).inOrder()
    }
}
