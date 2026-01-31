package com.example.arweld.feature.drawingimport.preprocess

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

sealed class PageQuadSelectionResult {
    data class Success(val candidate: PageQuadCandidate) : PageQuadSelectionResult()
    data class Failure(val failure: PageDetectFailure) : PageQuadSelectionResult()
}

data class PageQuadCandidate(
    val points: List<PointV1>,
    val contourArea: Double,
    val score: Double,
)

enum class PageDetectFailureCode {
    PAGE_NOT_FOUND,
    NO_CONVEX_QUAD,
    QUAD_TOO_SMALL,
}

data class PageDetectFailure(
    val code: PageDetectFailureCode,
    val message: String,
)

data class PageQuadSelectionConfig(
    val minAreaFraction: Double = 0.15,
    val approxEpsilonRatio: Double = 0.02,
    val maxAspectRatio: Double = 4.0,
)

class PageQuadSelector(
    private val config: PageQuadSelectionConfig = PageQuadSelectionConfig(),
) {
    fun select(
        contours: List<ContourV1>,
        frameWidth: Int,
        frameHeight: Int,
    ): PageQuadSelectionResult {
        if (contours.isEmpty()) {
            return PageQuadSelectionResult.Failure(
                PageDetectFailure(
                    code = PageDetectFailureCode.PAGE_NOT_FOUND,
                    message = "No contours available for page detection.",
                ),
            )
        }
        require(frameWidth > 0 && frameHeight > 0) { "Frame size must be > 0." }
        val frameArea = frameWidth.toDouble() * frameHeight.toDouble()
        val minArea = frameArea * config.minAreaFraction
        val candidates = mutableListOf<PageQuadCandidate>()
        var hasConvexQuad = false
        var hasQuadAboveThreshold = false
        for (contour in contours) {
            val epsilon = contour.perimeter * config.approxEpsilonRatio
            val approx = approximatePolygon(contour.points, epsilon)
            if (approx.size != 4) continue
            if (!isConvexQuad(approx)) continue
            hasConvexQuad = true
            val quadArea = polygonArea(approx)
            if (quadArea <= 0.0) continue
            if (quadArea < minArea) {
                continue
            }
            hasQuadAboveThreshold = true
            val bbox = boundingBox(approx)
            val bboxArea = bbox.width.toDouble() * bbox.height.toDouble()
            val rectangularity = if (bboxArea > 0.0) quadArea / bboxArea else 0.0
            val aspectRatio = aspectRatio(bbox)
            val aspectScore = if (aspectRatio.isFinite()) {
                min(1.0, config.maxAspectRatio / aspectRatio)
            } else {
                0.0
            }
            val score = quadArea * rectangularity * aspectScore
            candidates.add(
                PageQuadCandidate(
                    points = approx,
                    contourArea = contour.area,
                    score = score,
                ),
            )
        }
        if (!hasConvexQuad) {
            return PageQuadSelectionResult.Failure(
                PageDetectFailure(
                    code = PageDetectFailureCode.NO_CONVEX_QUAD,
                    message = "No convex quad candidates after polygon approximation.",
                ),
            )
        }
        val viable = candidates.filter { it.score > 0.0 }
        if (viable.isEmpty() || !hasQuadAboveThreshold) {
            return PageQuadSelectionResult.Failure(
                PageDetectFailure(
                    code = PageDetectFailureCode.QUAD_TOO_SMALL,
                    message = "Convex quad candidates found but area below threshold.",
                ),
            )
        }
        val best = viable.maxWith(
            compareBy<PageQuadCandidate> { it.score }.thenBy { it.contourArea },
        )
        return PageQuadSelectionResult.Success(best)
    }

    private fun approximatePolygon(points: List<PointV1>, epsilon: Double): List<PointV1> {
        if (points.size <= 2) return points
        val simplified = ramerDouglasPeucker(points, epsilon)
        return if (simplified.size >= 3) simplified else points
    }

    private fun ramerDouglasPeucker(points: List<PointV1>, epsilon: Double): List<PointV1> {
        if (points.size < 3) return points
        val first = points.first()
        val last = points.last()
        var maxDist = 0.0
        var index = 0
        for (i in 1 until points.size - 1) {
            val dist = perpendicularDistance(points[i], first, last)
            if (dist > maxDist) {
                maxDist = dist
                index = i
            }
        }
        return if (maxDist > epsilon) {
            val left = ramerDouglasPeucker(points.subList(0, index + 1), epsilon)
            val right = ramerDouglasPeucker(points.subList(index, points.size), epsilon)
            left.dropLast(1) + right
        } else {
            listOf(first, last)
        }
    }

    private fun perpendicularDistance(point: PointV1, lineStart: PointV1, lineEnd: PointV1): Double {
        val dx = (lineEnd.x - lineStart.x).toDouble()
        val dy = (lineEnd.y - lineStart.y).toDouble()
        if (dx == 0.0 && dy == 0.0) {
            return hypot((point.x - lineStart.x).toDouble(), (point.y - lineStart.y).toDouble())
        }
        val numerator = abs(dy * point.x - dx * point.y + lineEnd.x * lineStart.y - lineEnd.y * lineStart.x)
        val denom = hypot(dx, dy)
        return numerator / denom
    }

    private fun isConvexQuad(points: List<PointV1>): Boolean {
        if (points.size != 4) return false
        var sign = 0
        for (i in points.indices) {
            val a = points[i]
            val b = points[(i + 1) % points.size]
            val c = points[(i + 2) % points.size]
            val cross = cross(a, b, c)
            if (cross == 0) return false
            val currentSign = if (cross > 0) 1 else -1
            if (sign == 0) {
                sign = currentSign
            } else if (sign != currentSign) {
                return false
            }
        }
        return true
    }

    private fun cross(a: PointV1, b: PointV1, c: PointV1): Int {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
    }

    private fun polygonArea(points: List<PointV1>): Double {
        if (points.size < 3) return 0.0
        var sum = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            sum += points[i].x * points[j].y - points[j].x * points[i].y
        }
        return abs(sum) / 2.0
    }

    private fun boundingBox(points: List<PointV1>): BboxV1 {
        var minX = points.first().x
        var maxX = points.first().x
        var minY = points.first().y
        var maxY = points.first().y
        for (point in points.drop(1)) {
            minX = min(minX, point.x)
            maxX = max(maxX, point.x)
            minY = min(minY, point.y)
            maxY = max(maxY, point.y)
        }
        return BboxV1(
            x = minX,
            y = minY,
            width = maxX - minX + 1,
            height = maxY - minY + 1,
        )
    }

    private fun aspectRatio(bbox: BboxV1): Double {
        val width = bbox.width.toDouble()
        val height = bbox.height.toDouble()
        if (width <= 0.0 || height <= 0.0) return Double.POSITIVE_INFINITY
        return max(width, height) / min(width, height)
    }
}
