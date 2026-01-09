package com.example.arweld.feature.arview.geometry

import kotlin.math.atan2

/**
 * Orders marker corners clockwise from top-left.
 * Pure Kotlin implementation for JVM compatibility.
 *
 * Algorithm:
 * 1. Compute centroid
 * 2. Sort points by angle from centroid
 * 3. Rotate the list so top-left corner is first
 * 4. Verify clockwise order (in image coordinates where Y increases downward)
 *
 * Expected order: Top-Left, Top-Right, Bottom-Right, Bottom-Left (clockwise)
 */
fun orderCornersClockwiseFromTopLeft(corners: List<Point2f>): List<Point2f> {
    if (corners.size < 4) return corners

    // Compute centroid
    val cx = corners.map { it.x }.average().toFloat()
    val cy = corners.map { it.y }.average().toFloat()

    // Sort by angle from centroid (atan2 gives angle from +X axis, counter-clockwise in math coords)
    // In image coordinates (Y down), this naturally gives us clockwise order
    val sortedByAngle = corners.sortedBy { point ->
        atan2((point.y - cy).toDouble(), (point.x - cx).toDouble())
    }

    // Find the top-left corner (smallest y, then smallest x)
    val topLeftIndex = sortedByAngle.indices.minByOrNull { i ->
        val p = sortedByAngle[i]
        p.y * 1000 + p.x  // prioritize y, then x
    } ?: 0

    // Rotate list so top-left is first
    val rotated = sortedByAngle.drop(topLeftIndex) + sortedByAngle.take(topLeftIndex)

    // In image coordinates (Y down), clockwise from TL means: TL -> TR -> BR -> BL
    // Check if we have clockwise order by checking the cross product
    // Vector from corner 0 to corner 1 (should point right for TL->TR)
    val v1x = rotated[1].x - rotated[0].x
    val v1y = rotated[1].y - rotated[0].y
    // Vector from corner 0 to corner 3 (should point down-left for TL->BL)
    val v2x = rotated[3].x - rotated[0].x
    val v2y = rotated[3].y - rotated[0].y
    // Cross product z-component (v1 × v2)
    // In image coords (Y down), clockwise gives positive cross product
    val cross = v1x * v2y - v1y * v2x

    // If cross product is negative, we have counter-clockwise order, so reverse
    return if (cross < 0) {
        listOf(rotated[0], rotated[3], rotated[2], rotated[1])
    } else {
        rotated
    }
}
