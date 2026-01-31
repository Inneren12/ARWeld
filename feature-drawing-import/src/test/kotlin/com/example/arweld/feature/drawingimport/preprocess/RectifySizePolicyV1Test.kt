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
            topLeft = CornerPointV1(0.0, 0.0),
            topRight = CornerPointV1(200.0, 0.0),
            bottomRight = CornerPointV1(200.0, 100.0),
            bottomLeft = CornerPointV1(0.0, 100.0),
        )
        val params = RectifyParamsV1(maxSide = 2048, minSide = 1, enforceEven = false, maxPixels = 10_000_000)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Success)
        val size = (result as PageDetectOutcomeV1.Success).value
        assertEquals(200, size.width)
        assertEquals(100, size.height)
    }

    @Test
    fun `compute uses max of opposite sides for skewed quad`() {
        val corners = OrderedCornersV1(
            topLeft = CornerPointV1(0.0, 0.0),
            topRight = CornerPointV1(100.0, 0.0),
            bottomRight = CornerPointV1(110.0, 60.0),
            bottomLeft = CornerPointV1(-10.0, 50.0),
        )
        val params = RectifyParamsV1(maxSide = 2048, minSide = 1, enforceEven = false, maxPixels = 10_000_000)

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
            topLeft = CornerPointV1(0.0, 0.0),
            topRight = CornerPointV1(4000.0, 0.0),
            bottomRight = CornerPointV1(4000.0, 2000.0),
            bottomLeft = CornerPointV1(0.0, 2000.0),
        )
        val params = RectifyParamsV1(maxSide = 2048, minSide = 1, enforceEven = false, maxPixels = 10_000_000)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Success)
        val size = (result as PageDetectOutcomeV1.Success).value
        assertEquals(2048, size.width)
        assertEquals(1024, size.height)
    }

    @Test
    fun `compute clamps small quad to minSide with aspect ratio preserved`() {
        val corners = OrderedCornersV1(
            topLeft = CornerPointV1(0.0, 0.0),
            topRight = CornerPointV1(100.0, 0.0),
            bottomRight = CornerPointV1(100.0, 50.0),
            bottomLeft = CornerPointV1(0.0, 50.0),
        )
        val params = RectifyParamsV1(maxSide = 2048, minSide = 200, enforceEven = false, maxPixels = 10_000_000)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Success)
        val size = (result as PageDetectOutcomeV1.Success).value
        assertEquals(200, size.width)
        assertEquals(100, size.height)
    }

    @Test
    fun `compute enforces even size when requested`() {
        val corners = OrderedCornersV1(
            topLeft = CornerPointV1(0.0, 0.0),
            topRight = CornerPointV1(101.0, 0.0),
            bottomRight = CornerPointV1(101.0, 51.0),
            bottomLeft = CornerPointV1(0.0, 51.0),
        )
        val params = RectifyParamsV1(maxSide = 2048, minSide = 1, enforceEven = true, maxPixels = 10_000_000)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Success)
        val size = (result as PageDetectOutcomeV1.Success).value
        assertEquals(102, size.width)
        assertEquals(52, size.height)
    }

    @Test
    fun `compute fails for degenerate quad`() {
        val point = CornerPointV1(10.0, 10.0)
        val corners = OrderedCornersV1(
            topLeft = point,
            topRight = point,
            bottomRight = point,
            bottomLeft = point,
        )
        val params = RectifyParamsV1(maxSide = 2048, minSide = 1, enforceEven = false, maxPixels = 10_000_000)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Failure)
        val failure = (result as PageDetectOutcomeV1.Failure).failure
        assertEquals(PageDetectFailureCodeV1.ORDER_DEGENERATE, failure.code)
    }

    @Test
    fun `compute fails for extreme aspect ratio`() {
        val corners = OrderedCornersV1(
            topLeft = CornerPointV1(0.0, 0.0),
            topRight = CornerPointV1(500.0, 0.0),
            bottomRight = CornerPointV1(500.0, 50.0),
            bottomLeft = CornerPointV1(0.0, 50.0),
        )
        val params = RectifyParamsV1(maxSide = 2048, minSide = 1, enforceEven = false, maxPixels = 10_000_000)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Failure)
        val failure = (result as PageDetectOutcomeV1.Failure).failure
        assertEquals(PageDetectFailureCodeV1.UNKNOWN, failure.code)
    }

    @Test
    fun `compute fails when rectified pixel count exceeds maxPixels`() {
        val corners = OrderedCornersV1(
            topLeft = CornerPointV1(0.0, 0.0),
            topRight = CornerPointV1(5000.0, 0.0),
            bottomRight = CornerPointV1(5000.0, 4000.0),
            bottomLeft = CornerPointV1(0.0, 4000.0),
        )
        val params = RectifyParamsV1(maxSide = 5000, minSide = 1, enforceEven = false, maxPixels = 8_000_000)

        val result = RectifySizePolicyV1.compute(corners, params)

        assertTrue(result is PageDetectOutcomeV1.Failure)
        val failure = (result as PageDetectOutcomeV1.Failure).failure
        assertEquals(PageDetectFailureCodeV1.RECTIFIED_TOO_LARGE, failure.code)
    }
}
