package com.example.arweld.core.drawing2d.v1.entities

import kotlinx.serialization.Serializable

/**
 * Base contract for Drawing2D v1 geometric entities.
 */
@Serializable
sealed interface EntityV1 {
    val id: String
    val layerId: String
    val styleId: String?
}
