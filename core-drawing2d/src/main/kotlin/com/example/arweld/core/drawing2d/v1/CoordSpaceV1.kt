package com.example.arweld.core.drawing2d.v1

import kotlinx.serialization.Serializable

/**
 * Defines the coordinate space for a Drawing2D document.
 *
 * The coordinate space determines how [PointV1] coordinates are interpreted.
 *
 * @property type The type of coordinate system (e.g., RECTIFIED_PX)
 * @property origin The location of the coordinate origin
 * @property axis The direction of the X and Y axes
 */
@Serializable
data class CoordSpaceV1(
    val type: CoordSpaceTypeV1,
    val origin: OriginV1,
    val axis: AxisV1
)

/**
 * The type of coordinate system used in the drawing.
 *
 * For v1, only RECTIFIED_PX is supported (pixel coordinates in a rectified/undistorted image).
 */
@Serializable
enum class CoordSpaceTypeV1 {
    /**
     * Rectified pixel coordinates.
     * The drawing has been corrected for distortion and coordinates are in pixels.
     */
    RECTIFIED_PX
}

/**
 * The location of the coordinate origin.
 *
 * For v1, only TOP_LEFT is supported (standard image coordinate convention).
 */
@Serializable
enum class OriginV1 {
    /**
     * Origin at the top-left corner of the drawing/image.
     */
    TOP_LEFT
}

/**
 * Defines the direction of the X and Y axes.
 *
 * @property x The direction of the positive X axis
 * @property y The direction of the positive Y axis
 */
@Serializable
data class AxisV1(
    val x: AxisDirectionV1,
    val y: AxisDirectionV1
)

/**
 * Direction for a coordinate axis.
 */
@Serializable
enum class AxisDirectionV1 {
    /**
     * Positive values increase to the right.
     */
    RIGHT,

    /**
     * Positive values increase to the left.
     */
    LEFT,

    /**
     * Positive values increase upward.
     */
    UP,

    /**
     * Positive values increase downward.
     */
    DOWN
}
