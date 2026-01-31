package com.example.arweld.feature.drawingimport.preprocess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PageQuadSelectorTest {
    @Test
    fun `select chooses largest viable quad by score`() {
        val selector = PageQuadSelector(
            PageQuadSelectionConfig(
                minAreaFraction = 0.15,
                approxEpsilonRatio = 0.01,
                maxAspectRatio = 4.0,
            ),
        )
        val large = contourForRect(100, 120, 500, 400)
        val small = contourForRect(50, 40, 200, 200)

        val result = selector.select(listOf(small, large), frameWidth = 1000, frameHeight = 1000)

        assertTrue(result is PageQuadSelectionResult.Success)
        val candidate = (result as PageQuadSelectionResult.Success).candidate
        assertEquals(large.area, candidate.contourArea, 0.0)
        assertEquals(4, candidate.points.size)
    }

    @Test
    fun `select returns no convex quad when only triangles exist`() {
        val selector = PageQuadSelector(
            PageQuadSelectionConfig(
                minAreaFraction = 0.05,
                approxEpsilonRatio = 0.01,
            ),
        )
        val triangle = ContourV1(
            points = listOf(
                PointV1(0, 0),
                PointV1(400, 0),
                PointV1(0, 400),
            ),
            area = 80000.0,
            perimeter = 1200.0,
            bbox = BboxV1(x = 0, y = 0, width = 401, height = 401),
        )

        val result = selector.select(listOf(triangle), frameWidth = 1000, frameHeight = 1000)

        assertTrue(result is PageQuadSelectionResult.Failure)
        val failure = (result as PageQuadSelectionResult.Failure).failure
        assertEquals(PageDetectFailureCode.NO_CONVEX_QUAD, failure.code)
    }

    private fun contourForRect(x: Int, y: Int, width: Int, height: Int): ContourV1 {
        val points = listOf(
            PointV1(x, y),
            PointV1(x + width, y),
            PointV1(x + width, y + height),
            PointV1(x, y + height),
        )
        val area = width.toDouble() * height.toDouble()
        val perimeter = 2.0 * (width + height).toDouble()
        val bbox = BboxV1(
            x = x,
            y = y,
            width = width + 1,
            height = height + 1,
        )
        return ContourV1(
            points = points,
            area = area,
            perimeter = perimeter,
            bbox = bbox,
        )
    }
}
