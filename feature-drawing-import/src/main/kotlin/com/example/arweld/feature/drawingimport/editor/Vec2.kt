package com.example.arweld.feature.drawingimport.editor

data class Vec2(
    val x: Float,
    val y: Float,
) {
    operator fun plus(other: Vec2): Vec2 = Vec2(x + other.x, y + other.y)

    operator fun minus(other: Vec2): Vec2 = Vec2(x - other.x, y - other.y)

    operator fun times(scale: Float): Vec2 = Vec2(x * scale, y * scale)

    operator fun div(scale: Float): Vec2 = Vec2(x / scale, y / scale)
}
