package com.example.arweld.core.drawing2d.v1

import kotlinx.serialization.Serializable

/**
 * Units of measurement for Drawing2D schema v1.
 *
 * For v1, only PX (pixels) is supported. Future versions may add
 * physical units (MM, IN, etc.) with associated scale factors.
 */
@Serializable
enum class UnitsV1 {
    /**
     * Pixels - the base unit for rectified drawings in v1.
     */
    PX
}
