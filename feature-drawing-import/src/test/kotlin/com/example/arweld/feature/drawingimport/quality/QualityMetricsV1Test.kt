package com.example.arweld.feature.drawingimport.quality

import android.graphics.Bitmap
import android.graphics.Color
import com.example.arweld.feature.drawingimport.preprocess.PointV1
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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

    @Test
    fun `blurVarianceLaplacian decreases after box blur`() {
        val sharp = createCheckerboardBitmap(width = 64, height = 64, cellSize = 4)
        val blurred = boxBlur(sharp, radius = 1)

        val sharpVar = QualityMetricsV1.blurVarianceLaplacian(sharp)
        val blurredVar = QualityMetricsV1.blurVarianceLaplacian(blurred)

        assertTrue(sharpVar.isFinite())
        assertTrue(blurredVar.isFinite())
        assertTrue(sharpVar > blurredVar)

        sharp.recycle()
        blurred.recycle()
    }

    private fun createCheckerboardBitmap(width: Int, height: Int, cellSize: Int): Bitmap {
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val yCell = y / cellSize
            for (x in 0 until width) {
                val xCell = x / cellSize
                val isWhite = (xCell + yCell) % 2 == 0
                pixels[y * width + x] = if (isWhite) Color.WHITE else Color.BLACK
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun boxBlur(source: Bitmap, radius: Int): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val out = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var rSum = 0
                var gSum = 0
                var bSum = 0
                var count = 0
                for (dy in -radius..radius) {
                    val yy = (y + dy).coerceIn(0, height - 1)
                    val row = yy * width
                    for (dx in -radius..radius) {
                        val xx = (x + dx).coerceIn(0, width - 1)
                        val pixel = pixels[row + xx]
                        rSum += (pixel shr 16) and 0xFF
                        gSum += (pixel shr 8) and 0xFF
                        bSum += pixel and 0xFF
                        count += 1
                    }
                }
                val r = rSum / count
                val g = gSum / count
                val b = bSum / count
                out[y * width + x] = Color.rgb(r, g, b)
            }
        }
        return Bitmap.createBitmap(out, width, height, Bitmap.Config.ARGB_8888)
    fun `exposure reports clipping for black image`() {
        val bitmap = solidBitmap(10, 10, Color.BLACK)
        val metrics = QualityMetricsV1.exposure(bitmap)

        assertEquals(0.0, metrics.meanY, 0.01)
        assertEquals(100.0, metrics.clipLowPct, 0.01)
        assertEquals(0.0, metrics.clipHighPct, 0.01)
    }

    @Test
    fun `exposure reports clipping for white image`() {
        val bitmap = solidBitmap(12, 8, Color.WHITE)
        val metrics = QualityMetricsV1.exposure(bitmap)

        assertEquals(255.0, metrics.meanY, 0.01)
        assertEquals(0.0, metrics.clipLowPct, 0.01)
        assertEquals(100.0, metrics.clipHighPct, 0.01)
    }

    @Test
    fun `exposure reports mid-gray without clipping`() {
        val gray = Color.rgb(128, 128, 128)
        val bitmap = solidBitmap(9, 9, gray)
        val metrics = QualityMetricsV1.exposure(bitmap)

        assertEquals(128.0, metrics.meanY, 0.01)
        assertEquals(0.0, metrics.clipLowPct, 0.01)
        assertEquals(0.0, metrics.clipHighPct, 0.01)
    }
}

private fun solidBitmap(width: Int, height: Int, color: Int): Bitmap {
    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
        eraseColor(color)
    }
}
