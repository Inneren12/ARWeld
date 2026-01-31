package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Point2D
import kotlin.math.sqrt

private val STRICT_NUMBER_REGEX = Regex("^\\d+(\\.\\d+)?$")

fun parseStrictNumber(text: String): Double? {
    val trimmed = text.trim()
    if (!STRICT_NUMBER_REGEX.matches(trimmed)) {
        return null
    }
    return trimmed.toDoubleOrNull()
}

fun parseStrictPositiveNumber(text: String): Double? {
    val value = parseStrictNumber(text) ?: return null
    if (value <= 0.0) {
        return null
    }
    return value
}

fun distanceBetween(pointA: Point2D, pointB: Point2D): Double {
    val dx = pointA.x - pointB.x
    val dy = pointA.y - pointB.y
    return sqrt(dx * dx + dy * dy)
}
