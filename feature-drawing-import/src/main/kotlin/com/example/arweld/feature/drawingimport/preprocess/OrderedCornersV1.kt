package com.example.arweld.feature.drawingimport.preprocess

import kotlin.math.max
import kotlin.math.min

data class CornerPointV1(
    val x: Double,
    val y: Double,
) {
    fun isFinite(): Boolean = x.isFinite() && y.isFinite()

    fun clamp(width: Int, height: Int): CornerPointV1 {
        val clampedX = min(max(x, 0.0), (width - 1).toDouble())
        val clampedY = min(max(y, 0.0), (height - 1).toDouble())
        return CornerPointV1(clampedX, clampedY)
    }
}

data class OrderedCornersV1(
    val topLeft: CornerPointV1,
    val topRight: CornerPointV1,
    val bottomRight: CornerPointV1,
    val bottomLeft: CornerPointV1,
) {
    fun toList(): List<CornerPointV1> = listOf(topLeft, topRight, bottomRight, bottomLeft)
}
