package com.example.arweld.core.drawing2d.v1

import kotlinx.serialization.Serializable

/**
 * A 2D point in the Drawing2D schema v1.
 *
 * Coordinates are expressed in the coordinate space defined by [CoordSpaceV1].
 * For RECTIFIED_PX space, x and y are pixel values.
 *
 * @property x The x-coordinate
 * @property y The y-coordinate
 */
@Serializable
data class PointV1(
    val x: Double,
    val y: Double
)
