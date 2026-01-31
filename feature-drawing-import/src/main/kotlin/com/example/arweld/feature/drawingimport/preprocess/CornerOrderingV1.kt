package com.example.arweld.feature.drawingimport.preprocess

import kotlin.math.atan2

object CornerOrderingV1 {
    fun order(points: List<PointV1>): OrderedCornersV1 {
        require(points.size == 4) { "Expected 4 points for ordering." }
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
        return OrderedCornersV1(
            topLeft = ordered[0].toCornerPoint(),
            topRight = ordered[1].toCornerPoint(),
            bottomRight = ordered[2].toCornerPoint(),
            bottomLeft = ordered[3].toCornerPoint(),
        )
    }

    private fun PointV1.toCornerPoint(): CornerPointV1 {
        return CornerPointV1(x.toDouble(), y.toDouble())
    }
}
