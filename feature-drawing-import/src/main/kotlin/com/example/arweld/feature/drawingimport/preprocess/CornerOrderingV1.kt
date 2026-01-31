package com.example.arweld.feature.drawingimport.preprocess

import kotlin.math.abs
import kotlin.math.atan2

data class OrderedCornersV1(
    val tl: PointV1,
    val tr: PointV1,
    val br: PointV1,
    val bl: PointV1,
)

enum class OrderFailureV1 {
    NOT_FOUR_POINTS,
    DUPLICATE_POINTS,
    DEGENERATE_QUAD,
}

sealed class OrderResult {
    data class Success(val ordered: OrderedCornersV1) : OrderResult()
    data class Failure(val code: OrderFailureV1) : OrderResult()
}

object CornerOrderingV1 {
    private const val MIN_AREA_EPSILON = 1e-3

    fun order(points4: List<PointV1>): OrderResult {
        if (points4.size != 4) {
            return OrderResult.Failure(OrderFailureV1.NOT_FOUR_POINTS)
        }
        if (points4.distinct().size != 4) {
            return OrderResult.Failure(OrderFailureV1.DUPLICATE_POINTS)
        }

        val centroidX = points4.map { it.x }.average()
        val centroidY = points4.map { it.y }.average()

        val sortedByAngle = points4.sortedBy { point ->
            atan2(point.y - centroidY, point.x - centroidX)
        }

        val topLeftIndex = sortedByAngle.indices.minByOrNull { index ->
            val point = sortedByAngle[index]
            (point.x + point.y).toLong() * 1_000_000L + point.x
        } ?: 0

        val rotated = sortedByAngle.drop(topLeftIndex) + sortedByAngle.take(topLeftIndex)

        val clockwise = ensureClockwiseFromTopLeft(rotated)
        if (isSelfCrossing(clockwise)) {
            return OrderResult.Failure(OrderFailureV1.DEGENERATE_QUAD)
        }
        val area = polygonArea(clockwise)
        if (abs(area) <= MIN_AREA_EPSILON) {
            return OrderResult.Failure(OrderFailureV1.DEGENERATE_QUAD)
        }

        return OrderResult.Success(
            OrderedCornersV1(
                tl = clockwise[0],
                tr = clockwise[1],
                br = clockwise[2],
                bl = clockwise[3],
            ),
        )
    }

    private fun ensureClockwiseFromTopLeft(points: List<PointV1>): List<PointV1> {
        val v1x = points[1].x - points[0].x
        val v1y = points[1].y - points[0].y
        val v2x = points[3].x - points[0].x
        val v2y = points[3].y - points[0].y
        val cross = v1x * v2y - v1y * v2x
        return if (cross < 0) {
            listOf(points[0], points[3], points[2], points[1])
        } else {
            points
        }
    }

    private fun polygonArea(points: List<PointV1>): Double {
        var sum = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            sum += points[i].x * points[j].y - points[j].x * points[i].y
        }
        return sum / 2.0
    }

    private fun isSelfCrossing(points: List<PointV1>): Boolean {
        return segmentsIntersect(points[0], points[1], points[2], points[3]) ||
            segmentsIntersect(points[1], points[2], points[3], points[0])
    }

    private fun segmentsIntersect(a: PointV1, b: PointV1, c: PointV1, d: PointV1): Boolean {
        val ab = orientation(a, b, c)
        val ab2 = orientation(a, b, d)
        val cd = orientation(c, d, a)
        val cd2 = orientation(c, d, b)
        if (ab == 0 && onSegment(a, c, b)) return true
        if (ab2 == 0 && onSegment(a, d, b)) return true
        if (cd == 0 && onSegment(c, a, d)) return true
        if (cd2 == 0 && onSegment(c, b, d)) return true
        return ab != ab2 && cd != cd2
    }

    private fun orientation(a: PointV1, b: PointV1, c: PointV1): Int {
        val value = (b.y - a.y).toLong() * (c.x - b.x) - (b.x - a.x).toLong() * (c.y - b.y)
        return when {
            value > 0 -> 1
            value < 0 -> -1
            else -> 0
        }
    }

    private fun onSegment(a: PointV1, b: PointV1, c: PointV1): Boolean {
        return b.x in minOf(a.x, c.x)..maxOf(a.x, c.x) &&
            b.y in minOf(a.y, c.y)..maxOf(a.y, c.y)
    }
}
