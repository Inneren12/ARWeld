package com.example.arweld.core.ar.spatial

import android.graphics.PointF

/**
 * Pure Kotlin 2D point type for JVM-compatible geometry operations.
 * This avoids Android framework dependencies in unit tests.
 */
data class Point2f(val x: Float, val y: Float) {
    companion object {
        val ZERO = Point2f(0f, 0f)
    }
}

/**
 * Convert Android PointF to pure Kotlin Point2f
 */
fun PointF.toPoint2f(): Point2f = Point2f(x, y)

/**
 * Convert pure Kotlin Point2f to Android PointF
 */
fun Point2f.toPointF(): PointF = PointF(x, y)

/**
 * Convert list of PointF to Point2f
 */
fun List<PointF>.toPoint2fList(): List<Point2f> = map { it.toPoint2f() }

/**
 * Convert list of Point2f to PointF
 */
fun List<Point2f>.toPointFList(): List<PointF> = map { it.toPointF() }
