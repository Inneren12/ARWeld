package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.core.drawing2d.editor.v1.ScaleInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class ScaleStatusTest {

    @Test
    fun `deriveScaleStatus returns missing when scale absent`() {
        val status = deriveScaleStatus(null)

        assertEquals(ScaleStatus.Missing, status.status)
    }

    @Test
    fun `deriveScaleStatus returns invalid when points overlap`() {
        val scale = ScaleInfo(
            pointA = Point2D(1.0, 1.0),
            pointB = Point2D(1.0, 1.0),
            realLengthMm = 100.0,
        )

        val status = deriveScaleStatus(scale)

        assertEquals(ScaleStatus.Invalid, status.status)
    }

    @Test
    fun `deriveScaleStatus returns invalid when length is non-positive`() {
        val scale = ScaleInfo(
            pointA = Point2D(0.0, 0.0),
            pointB = Point2D(0.0, 10.0),
            realLengthMm = 0.0,
        )

        val status = deriveScaleStatus(scale)

        assertEquals(ScaleStatus.Invalid, status.status)
    }

    @Test
    fun `deriveScaleStatus computes mmPerPx and formats deterministically`() {
        val scale = ScaleInfo(
            pointA = Point2D(0.0, 0.0),
            pointB = Point2D(0.0, 20.0),
            realLengthMm = 100.0,
        )

        val status = deriveScaleStatus(scale)

        assertEquals(ScaleStatus.Set, status.status)
        assertEquals("5.000", formatScaleMmPerPx(status.mmPerPx ?: 0.0))
        assertEquals("100.0", formatScaleLengthMm(status.referenceLengthMm ?: 0.0))
    }
}
