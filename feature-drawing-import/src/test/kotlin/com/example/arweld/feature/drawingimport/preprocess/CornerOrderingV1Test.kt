package com.example.arweld.feature.drawingimport.preprocess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CornerOrderingV1Test {
    @Test
    fun `order returns degenerate when duplicate points supplied`() {
        val points = listOf(
            PointV1(0, 0),
            PointV1(100, 0),
            PointV1(100, 0),
            PointV1(0, 100),
        )

        val result = CornerOrderingV1.order(points)

        assertTrue(result is PageDetectOutcomeV1.Failure)
        val failure = (result as PageDetectOutcomeV1.Failure).failure
        assertEquals(PageDetectFailureCodeV1.ORDER_DEGENERATE, failure.code)
    }
}
