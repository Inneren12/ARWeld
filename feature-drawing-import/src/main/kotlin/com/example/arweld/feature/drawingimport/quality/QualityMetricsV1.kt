package com.example.arweld.feature.drawingimport.quality

import android.graphics.Bitmap
import com.example.arweld.feature.drawingimport.preprocess.PointV1
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.PI

/**
 * Quality metrics computed in the PageDetectFrame (downscaled) coordinate space.
 */
data class SkewMetricsV1(
    val angleMaxAbsDeg: Double,
    val angleMeanAbsDeg: Double,
    val keystoneWidthRatio: Double,
    val keystoneHeightRatio: Double,
    val pageFillRatio: Double,
    val status: MetricStatusV1 = MetricStatusV1.OK,
)

/**
 * Exposure metrics computed from Rec.601 luma values in [0..255].
 *
 * Uses the same grayscale conversion as PageDetectPreprocessor:
 *   luma = (77 * R + 150 * G + 29 * B) >> 8
 */
data class ExposureMetricsV1(
    val meanY: Double,
    val clipLowPct: Double,
    val clipHighPct: Double,
)

data class RectifiedQualityMetricsV1(
    val blurVariance: Double? = null,
)

enum class MetricStatusV1 {
    OK,
    DEGENERATE,
}

data class CornerPointV1(
    val x: Double,
    val y: Double,
)

data class OrderedCornersV1(
    val topLeft: CornerPointV1,
    val topRight: CornerPointV1,
    val bottomRight: CornerPointV1,
    val bottomLeft: CornerPointV1,
) {
    fun asList(): List<CornerPointV1> = listOf(topLeft, topRight, bottomRight, bottomLeft)

    companion object {
        fun fromPoints(points: List<PointV1>): OrderedCornersV1? {
            if (points.size < 4) return null
            val cornerPoints = points.take(4).map { CornerPointV1(it.x.toDouble(), it.y.toDouble()) }
            return fromCornerPoints(cornerPoints)
        }

        fun fromCornerPoints(points: List<CornerPointV1>): OrderedCornersV1? {
            if (points.size < 4) return null
            val ordered = orderClockwiseFromTopLeft(points)
            if (ordered.size < 4) return null
            return OrderedCornersV1(
                topLeft = ordered[0],
                topRight = ordered[1],
                bottomRight = ordered[2],
                bottomLeft = ordered[3],
            )
        }

        private fun orderClockwiseFromTopLeft(points: List<CornerPointV1>): List<CornerPointV1> {
            if (points.size < 4) return points
            val cx = points.map { it.x }.average()
            val cy = points.map { it.y }.average()
            val sortedByAngle = points.sortedBy { point ->
                atan2(point.y - cy, point.x - cx)
            }
            val topLeftIndex = sortedByAngle.indices.minByOrNull { i ->
                val p = sortedByAngle[i]
                p.y * 1000.0 + p.x
            } ?: 0
            val rotated = sortedByAngle.drop(topLeftIndex) + sortedByAngle.take(topLeftIndex)
            val v1x = rotated[1].x - rotated[0].x
            val v1y = rotated[1].y - rotated[0].y
            val v2x = rotated[3].x - rotated[0].x
            val v2y = rotated[3].y - rotated[0].y
            val cross = v1x * v2y - v1y * v2x
            return if (cross < 0) {
                listOf(rotated[0], rotated[3], rotated[2], rotated[1])
            } else {
                rotated
            }
        }
    }
}

object QualityMetricsV1 {
    private const val MIN_EDGE = 1e-6
    const val SHADOW_CLIP_T = 8
    const val HIGHLIGHT_CLIP_T = 247

    /**
     * Variance of Laplacian (3x3, 4-neighbor) for blur estimation on a rectified bitmap.
     *
     * Grayscale conversion uses deterministic Rec.601 coefficients:
     *   Y = 0.299R + 0.587G + 0.114B
     *
     * Laplacian kernel:
     *   [ 0  1  0 ]
     *   [ 1 -4  1 ]
     *   [ 0  1  0 ]
     */
    fun blurVarianceLaplacian(bitmap: Bitmap): Double {
        val width = bitmap.width
        val height = bitmap.height
        if (width < 3 || height < 3) return 0.0

        val pixelCount = width * height
        val pixels = IntArray(pixelCount)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val grayscale = DoubleArray(pixelCount)
        for (i in 0 until pixelCount) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            grayscale[i] = 0.299 * r + 0.587 * g + 0.114 * b
        }

        var sum = 0.0
        var sumSq = 0.0
        var count = 0
        for (y in 1 until height - 1) {
            val row = y * width
            val rowAbove = row - width
            val rowBelow = row + width
            for (x in 1 until width - 1) {
                val idx = row + x
                val laplacian = grayscale[rowAbove + x] +
                    grayscale[rowBelow + x] +
                    grayscale[idx - 1] +
                    grayscale[idx + 1] -
                    4.0 * grayscale[idx]
                sum += laplacian
                sumSq += laplacian * laplacian
                count += 1
            }
        }

        if (count == 0) return 0.0
        val mean = sum / count
        val variance = (sumSq / count) - (mean * mean)
        return if (variance.isFinite()) max(0.0, variance) else 0.0
    }

    fun skewFromQuad(corners: OrderedCornersV1, imageW: Int, imageH: Int): SkewMetricsV1 {
        val imageArea = imageW.toDouble() * imageH.toDouble()
        val points = corners.asList()
        val topWidth = distance(points[0], points[1])
        val bottomWidth = distance(points[3], points[2])
        val leftHeight = distance(points[0], points[3])
        val rightHeight = distance(points[1], points[2])

        val quadArea = polygonArea(points)

        val angleDeviations = listOfNotNull(
            angleDeviation(points[3], points[0], points[1]),
            angleDeviation(points[0], points[1], points[2]),
            angleDeviation(points[1], points[2], points[3]),
            angleDeviation(points[2], points[3], points[0]),
        )

        val isDegenerate = imageArea <= 0.0 || quadArea <= 0.0 ||
            topWidth <= MIN_EDGE || bottomWidth <= MIN_EDGE ||
            leftHeight <= MIN_EDGE || rightHeight <= MIN_EDGE ||
            angleDeviations.size != 4

        if (isDegenerate) {
            return SkewMetricsV1(
                angleMaxAbsDeg = 0.0,
                angleMeanAbsDeg = 0.0,
                keystoneWidthRatio = 1.0,
                keystoneHeightRatio = 1.0,
                pageFillRatio = 0.0,
                status = MetricStatusV1.DEGENERATE,
            )
        }

        val keystoneWidthRatio = ratioOrFallback(topWidth, bottomWidth)
        val keystoneHeightRatio = ratioOrFallback(leftHeight, rightHeight)
        val angleMaxAbsDeg = angleDeviations.maxOrNull() ?: 0.0
        val angleMeanAbsDeg = angleDeviations.average()
        val pageFillRatio = if (imageArea > 0.0) quadArea / imageArea else 0.0

        return SkewMetricsV1(
            angleMaxAbsDeg = angleMaxAbsDeg,
            angleMeanAbsDeg = angleMeanAbsDeg,
            keystoneWidthRatio = keystoneWidthRatio,
            keystoneHeightRatio = keystoneHeightRatio,
            pageFillRatio = pageFillRatio,
            status = MetricStatusV1.OK,
        )
    }

    fun exposure(bitmap: Bitmap): ExposureMetricsV1 {
        val width = bitmap.width
        val height = bitmap.height
        val totalPixels = width * height
        if (totalPixels <= 0) {
            return ExposureMetricsV1(
                meanY = 0.0,
                clipLowPct = 0.0,
                clipHighPct = 0.0,
            )
        }
        val pixels = IntArray(totalPixels)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        var sumY = 0L
        var lowCount = 0
        var highCount = 0
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val luma = (77 * r + 150 * g + 29 * b) shr 8
            sumY += luma.toLong()
            if (luma <= SHADOW_CLIP_T) {
                lowCount += 1
            }
            if (luma >= HIGHLIGHT_CLIP_T) {
                highCount += 1
            }
        }
        val meanY = sumY.toDouble() / totalPixels.toDouble()
        val clipLowPct = lowCount.toDouble() * 100.0 / totalPixels.toDouble()
        val clipHighPct = highCount.toDouble() * 100.0 / totalPixels.toDouble()
        return ExposureMetricsV1(
            meanY = meanY,
            clipLowPct = clipLowPct,
            clipHighPct = clipHighPct,
        )
    }

    private fun distance(a: CornerPointV1, b: CornerPointV1): Double {
        return hypot(a.x - b.x, a.y - b.y)
    }

    private fun ratioOrFallback(a: Double, b: Double): Double {
        if (a <= 0.0 || b <= 0.0) return 1.0
        val ratio = a / b
        val inverse = b / a
        return max(ratio, inverse)
    }

    private fun angleDeviation(prev: CornerPointV1, center: CornerPointV1, next: CornerPointV1): Double? {
        val v1x = prev.x - center.x
        val v1y = prev.y - center.y
        val v2x = next.x - center.x
        val v2y = next.y - center.y
        val len1 = hypot(v1x, v1y)
        val len2 = hypot(v2x, v2y)
        if (len1 <= MIN_EDGE || len2 <= MIN_EDGE) return null
        val dot = v1x * v2x + v1y * v2y
        val cos = (dot / (len1 * len2)).coerceIn(-1.0, 1.0)
        val angleDeg = acos(cos) * 180.0 / PI
        return abs(angleDeg - 90.0)
    }

    private fun polygonArea(points: List<CornerPointV1>): Double {
        if (points.size < 3) return 0.0
        var sum = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            sum += points[i].x * points[j].y - points[j].x * points[i].y
        }
        return abs(sum) / 2.0
    }
}

data class DrawingImportPipelineResultV1(
    val orderedCorners: OrderedCornersV1,
    val refinedCorners: OrderedCornersV1? = null,
    val imageWidth: Int,
    val imageHeight: Int,
    val skewMetrics: SkewMetricsV1,
    val blurVariance: Double? = null,
    val exposureMetrics: ExposureMetricsV1,
)
