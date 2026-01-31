package com.example.arweld.feature.drawingimport.preprocess

import kotlin.math.atan2

object CornerOrderingV1 {
    fun order(points: List<PointV1>): PageDetectOutcomeV1<OrderedCornersV1> {
        if (points.size != 4) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.ORDER,
                    code = PageDetectFailureCodeV1.ORDER_NOT_FOUR_POINTS,
                    debugMessage = "Expected 4 points for ordering, got ${points.size}.",
                ),
            )
        }
        val uniquePoints = points.distinctBy { it.x to it.y }
        if (uniquePoints.size != 4) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.ORDER,
                    code = PageDetectFailureCodeV1.ORDER_DEGENERATE,
                    debugMessage = "Duplicate or overlapping corner points.",
                ),
            )
        }
        val centerX = points.map { it.x }.average()
        val centerY = points.map { it.y }.average()
        val sorted = points.sortedBy { point ->
            atan2((point.y - centerY), (point.x - centerX))
        }
        val topLeftIndex = sorted.indices.minByOrNull { index ->
            val point = sorted[index]
            point.y * 1000 + point.x
        } ?: 0
        val rotated = sorted.drop(topLeftIndex) + sorted.take(topLeftIndex)
        val tl = rotated[0]
        val tr = rotated[1]
        val br = rotated[2]
        val bl = rotated[3]
        val v1x = (tr.x - tl.x).toDouble()
        val v1y = (tr.y - tl.y).toDouble()
        val v2x = (bl.x - tl.x).toDouble()
        val v2y = (bl.y - tl.y).toDouble()
        val cross = v1x * v2y - v1y * v2x
        val ordered = if (cross < 0) listOf(tl, bl, br, tr) else listOf(tl, tr, br, bl)
        if (polygonArea(ordered) <= 0.0) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.ORDER,
                    code = PageDetectFailureCodeV1.ORDER_DEGENERATE,
                    debugMessage = "Corner ordering produced degenerate polygon.",
                ),
            )
        }
        return PageDetectOutcomeV1.Success(
            OrderedCornersV1(
                topLeft = ordered[0].toCornerPoint(),
                topRight = ordered[1].toCornerPoint(),
                bottomRight = ordered[2].toCornerPoint(),
                bottomLeft = ordered[3].toCornerPoint(),
            ),
        )
    }

    private fun PointV1.toCornerPoint(): CornerPointV1 {
        return CornerPointV1(x.toDouble(), y.toDouble())
    }

    private fun polygonArea(points: List<PointV1>): Double {
        if (points.size < 3) return 0.0
        var sum = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            sum += points[i].x * points[j].y - points[j].x * points[i].y
        }
        return kotlin.math.abs(sum) / 2.0
    }
}
