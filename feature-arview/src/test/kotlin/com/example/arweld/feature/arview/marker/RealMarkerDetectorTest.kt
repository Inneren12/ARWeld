package com.example.arweld.feature.arview.marker

import android.graphics.Point
import android.graphics.PointF
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RealMarkerDetectorTest {

    private val detector = RealMarkerDetector()

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
            assertThat(mapped).isEqualTo(expected)
        }
    }

    @Test
    fun `orders marker corners clockwise from top-left`() {
        val unordered = listOf(
            PointF(50f, 80f),
            PointF(10f, 10f),
            PointF(90f, 20f),
            PointF(20f, 70f),
        )

        val ordered = detector.orderCorners(unordered)

        assertThat(ordered).containsExactly(
            PointF(10f, 10f),
            PointF(90f, 20f),
            PointF(50f, 80f),
            PointF(20f, 70f),
        ).inOrder()
    }
}
