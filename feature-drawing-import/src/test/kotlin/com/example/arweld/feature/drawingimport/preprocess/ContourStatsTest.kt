package com.example.arweld.feature.drawingimport.preprocess

import org.junit.Assert.assertEquals
import org.junit.Test

class ContourStatsTest {
    @Test
    fun `topByArea returns sorted contours by area`() {
        val contours = listOf(
            contour(area = 10.0),
            contour(area = 42.5),
            contour(area = 7.0),
        )

        val top = ContourStats.topByArea(contours, 2)

        assertEquals(2, top.size)
        assertEquals(42.5, top[0].area, 0.0)
        assertEquals(10.0, top[1].area, 0.0)
    }

    private fun contour(area: Double): ContourV1 {
        return ContourV1(
            points = listOf(PointV1(0, 0), PointV1(1, 0), PointV1(1, 1)),
            area = area,
            perimeter = 1.0,
            bbox = BboxV1(x = 0, y = 0, width = 1, height = 1),
        )
    }
}
