package com.example.arweld.feature.drawingimport.quality

import com.example.arweld.feature.drawingimport.preprocess.PointV1
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QualityMetricsV1Test {
    @Test
    fun `skewFromQuad returns zeros for perfect rectangle`() {
        val corners = OrderedCornersV1.fromPoints(
            listOf(
                PointV1(0, 0),
                PointV1(100, 0),
                PointV1(100, 200),
                PointV1(0, 200),
            ),
        )
        requireNotNull(corners)

        val metrics = QualityMetricsV1.skewFromQuad(corners, imageW = 200, imageH = 400)

        assertEquals(MetricStatusV1.OK, metrics.status)
        assertEquals(0.0, metrics.angleMaxAbsDeg, 1e-6)
        assertEquals(0.0, metrics.angleMeanAbsDeg, 1e-6)
        assertEquals(1.0, metrics.keystoneWidthRatio, 1e-6)
        assertEquals(1.0, metrics.keystoneHeightRatio, 1e-6)
        assertEquals(0.25, metrics.pageFillRatio, 1e-6)
    }

    @Test
    fun `skewFromQuad detects keystone ratios`() {
        val corners = OrderedCornersV1.fromCornerPoints(
            listOf(
                CornerPointV1(0.0, 0.0),
                CornerPointV1(120.0, 0.0),
                CornerPointV1(80.0, 200.0),
                CornerPointV1(20.0, 200.0),
            ),
        )
        requireNotNull(corners)

        val metrics = QualityMetricsV1.skewFromQuad(corners, imageW = 200, imageH = 300)

        assertEquals(MetricStatusV1.OK, metrics.status)
        assertTrue(metrics.keystoneWidthRatio > 1.0)
        assertTrue(metrics.keystoneHeightRatio >= 1.0)
        assertTrue(metrics.angleMaxAbsDeg >= 0.0)
        assertFalse(metrics.keystoneWidthRatio.isNaN())
    }

    @Test
    fun `skewFromQuad handles degenerate quad`() {
        val corners = OrderedCornersV1.fromCornerPoints(
            listOf(
                CornerPointV1(0.0, 0.0),
                CornerPointV1(0.0, 0.0),
                CornerPointV1(0.0, 0.0),
                CornerPointV1(0.0, 0.0),
            ),
        )
        requireNotNull(corners)

        val metrics = QualityMetricsV1.skewFromQuad(corners, imageW = 200, imageH = 300)

        assertEquals(MetricStatusV1.DEGENERATE, metrics.status)
        assertTrue(metrics.angleMaxAbsDeg.isFinite())
        assertTrue(metrics.angleMeanAbsDeg.isFinite())
        assertTrue(metrics.keystoneWidthRatio.isFinite())
        assertTrue(metrics.keystoneHeightRatio.isFinite())
        assertTrue(metrics.pageFillRatio.isFinite())
    }
}
