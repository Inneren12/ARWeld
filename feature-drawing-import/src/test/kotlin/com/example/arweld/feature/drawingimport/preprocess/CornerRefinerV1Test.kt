package com.example.arweld.feature.drawingimport.preprocess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CornerRefinerV1Test {
    @Test
    fun `refine is deterministic for the same input`() {
        val frame = syntheticFrame(width = 64, height = 64)
        val ordered = OrderedCornersV1(
            topLeft = CornerPointV1(12.0, 12.0),
            topRight = CornerPointV1(50.0, 12.0),
            bottomRight = CornerPointV1(50.0, 50.0),
            bottomLeft = CornerPointV1(12.0, 50.0),
        )
        val params = RefineParamsV1(windowRadiusPx = 5, maxIters = 5, epsilon = 0.25)

        val first = CornerRefinerV1.refine(frame, ordered, params)
        val second = CornerRefinerV1.refine(frame, ordered, params)

        assertTrue(first is PageDetectOutcomeV1.Success)
        assertTrue(second is PageDetectOutcomeV1.Success)
        val firstResult = (first as PageDetectOutcomeV1.Success).value
        val secondResult = (second as PageDetectOutcomeV1.Success).value
        assertEquals(firstResult.status, secondResult.status)
        assertEquals(firstResult.corners, secondResult.corners)
        assertEquals(firstResult.deltasPx.size, secondResult.deltasPx.size)
        firstResult.deltasPx.forEach { delta ->
            assertTrue(delta.isFinite())
        }
    }

    private fun syntheticFrame(width: Int, height: Int): PageDetectFrame {
        val gray = ByteArray(width * height)
        val minX = 16
        val maxX = 47
        val minY = 16
        val maxY = 47
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                val value = if (x in minX..maxX && y in minY..maxY) 255 else 0
                gray[idx] = value.toByte()
            }
        }
        return PageDetectFrame(
            width = width,
            height = height,
            gray = gray,
            originalWidth = width,
            originalHeight = height,
            downscaleFactor = 1.0,
            rotationAppliedDeg = 0,
        )
    }
}
