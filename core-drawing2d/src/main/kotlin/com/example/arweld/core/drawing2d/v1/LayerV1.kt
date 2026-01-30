package com.example.arweld.core.drawing2d.v1

import kotlinx.serialization.Serializable

/**
 * Represents a layer in a Drawing2D document.
 *
 * Layers provide organizational structure for drawing entities.
 * Entities can be assigned to layers for grouping, visibility control, and rendering order.
 *
 * @property id Unique identifier for this layer within the document
 * @property name Human-readable name for the layer
 * @property order Stable render order (lower values render first/behind higher values)
 */
@Serializable
data class LayerV1(
    val id: String,
    val name: String,
    val order: Int
)
