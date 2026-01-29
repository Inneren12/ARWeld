package com.example.arweld.core.ar.marker

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.view.Surface
import com.example.arweld.core.ar.spatial.Point2f
import com.example.arweld.core.ar.spatial.orderCornersClockwiseFromTopLeft
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
    fun `maps surface rotation constants to degrees`() {
        val mappings = mapOf(
            Surface.ROTATION_0 to 0,
            Surface.ROTATION_90 to 90,
            Surface.ROTATION_180 to 180,
            Surface.ROTATION_270 to 270,
        )

        mappings.forEach { (surfaceRotation, expectedDegrees) ->
            val degrees = detector.rotationDegreesFromSurface(surfaceRotation)
            assertThat(degrees).isEqualTo(expectedDegrees)
        }
    }

    @Test
    fun `maps bounding box corners into image space for rotations`() {
        val width = 100
        val height = 50
        val box = Rect(10, 5, 30, 25)

        val rotations = mapOf(
            0 to listOf(
                PointF(10f, 5f),
                PointF(30f, 5f),
                PointF(30f, 25f),
                PointF(10f, 25f),
            ),
            90 to listOf(
                PointF(5f, 90f),
                PointF(5f, 70f),
                PointF(25f, 70f),
                PointF(25f, 90f),
            ),
            180 to listOf(
                PointF(90f, 45f),
                PointF(70f, 45f),
                PointF(70f, 25f),
                PointF(90f, 25f),
            ),
            270 to listOf(
                PointF(45f, 10f),
                PointF(45f, 30f),
                PointF(25f, 30f),
                PointF(25f, 10f),
            ),
        )

        rotations.forEach { (rotation, expectedCorners) ->
            val mapped = detector.boundingBoxCorners(box, width, height, rotation)
            assertThat(mapped).hasSize(4)
            mapped.zip(expectedCorners).forEach { (actual, expected) ->
                assertThat(actual.x).isWithin(epsilon).of(expected.x)
                assertThat(actual.y).isWithin(epsilon).of(expected.y)
            }
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
