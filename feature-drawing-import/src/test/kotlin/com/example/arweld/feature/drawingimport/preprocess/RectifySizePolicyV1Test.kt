package com.example.arweld.feature.drawingimport.preprocess

import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RectifySizePolicyV1Test {
    @Test
    fun `compute returns size for axis-aligned rectangle`() {
        val corners = OrderedCornersV1(
            topLeft = PointV1(0, 0),
            topRight = PointV1(200, 0),
            bottomRight = PointV1(200, 100),
            bottomLeft = PointV1(0, 100),
        )
        val params = RectifySizeParamsV1(maxSide = 2048, minSide = 1, enforceEven = false)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Success)
        val size = (result as PageDetectOutcomeV1.Success).value
        assertEquals(200, size.width)
        assertEquals(100, size.height)
    }

    @Test
    fun `compute uses max of opposite sides for skewed quad`() {
        val corners = OrderedCornersV1(
            topLeft = PointV1(0, 0),
            topRight = PointV1(100, 0),
            bottomRight = PointV1(110, 60),
            bottomLeft = PointV1(-10, 50),
        )
        val params = RectifySizeParamsV1(maxSide = 2048, minSide = 1, enforceEven = false)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Success)
        val size = (result as PageDetectOutcomeV1.Success).value
        val topWidth = hypot(100.0, 0.0)
        val bottomWidth = hypot(120.0, 10.0)
        val leftHeight = hypot(10.0, 50.0)
        val rightHeight = hypot(10.0, 60.0)
        val expectedWidth = max(topWidth, bottomWidth).roundToInt()
        val expectedHeight = max(leftHeight, rightHeight).roundToInt()
        assertEquals(expectedWidth, size.width)
        assertEquals(expectedHeight, size.height)
    }

    @Test
    fun `compute clamps large quad to maxSide with aspect ratio preserved`() {
        val corners = OrderedCornersV1(
            topLeft = PointV1(0, 0),
            topRight = PointV1(4000, 0),
            bottomRight = PointV1(4000, 2000),
            bottomLeft = PointV1(0, 2000),
        )
        val params = RectifySizeParamsV1(maxSide = 2048, minSide = 1, enforceEven = false)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Success)
        val size = (result as PageDetectOutcomeV1.Success).value
        assertEquals(2048, size.width)
        assertEquals(1024, size.height)
    }

    @Test
    fun `compute clamps small quad to minSide with aspect ratio preserved`() {
        val corners = OrderedCornersV1(
            topLeft = PointV1(0, 0),
            topRight = PointV1(100, 0),
            bottomRight = PointV1(100, 50),
            bottomLeft = PointV1(0, 50),
        )
        val params = RectifySizeParamsV1(maxSide = 2048, minSide = 200, enforceEven = false)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Success)
        val size = (result as PageDetectOutcomeV1.Success).value
        assertEquals(200, size.width)
        assertEquals(100, size.height)
    }

    @Test
    fun `compute enforces even size when requested`() {
        val corners = OrderedCornersV1(
            topLeft = PointV1(0, 0),
            topRight = PointV1(101, 0),
            bottomRight = PointV1(101, 51),
            bottomLeft = PointV1(0, 51),
        )
        val params = RectifySizeParamsV1(maxSide = 2048, minSide = 1, enforceEven = true)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Success)
        val size = (result as PageDetectOutcomeV1.Success).value
        assertEquals(102, size.width)
        assertEquals(52, size.height)
    }

    @Test
    fun `compute fails for degenerate quad`() {
        val point = PointV1(10, 10)
        val corners = OrderedCornersV1(
            topLeft = point,
            topRight = point,
            bottomRight = point,
            bottomLeft = point,
        )
        val params = RectifySizeParamsV1(maxSide = 2048, minSide = 1, enforceEven = false)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Failure)
        val failure = (result as PageDetectOutcomeV1.Failure).failure
        assertEquals(PageDetectFailureCode.DEGENERATE_QUAD, failure.code)
    }

    @Test
    fun `compute fails for extreme aspect ratio`() {
        val corners = OrderedCornersV1(
            topLeft = PointV1(0, 0),
            topRight = PointV1(500, 0),
            bottomRight = PointV1(500, 50),
            bottomLeft = PointV1(0, 50),
        )
        val params = RectifySizeParamsV1(maxSide = 2048, minSide = 1, enforceEven = false)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Failure)
        val failure = (result as PageDetectOutcomeV1.Failure).failure
        assertEquals(PageDetectFailureCode.EXTREME_ASPECT_RATIO, failure.code)
    }
}
