package com.example.arweld.feature.drawingimport.preprocess

data class EdgeMap(
    val width: Int,
    val height: Int,
    val edges: ByteArray,
)

data class PointV1(
    val x: Int,
    val y: Int,
)

data class BboxV1(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

data class ContourV1(
    val points: List<PointV1>,
    val area: Double,
    val perimeter: Double,
    val bbox: BboxV1,
)
