package com.example.arweld.core.drawing2d.v1.entities

import com.example.arweld.core.drawing2d.v1.PointV1
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Dimension annotation entity for measuring distances.
 */
@Serializable
@SerialName("dimension")
data class DimensionV1(
    override val id: String,
    override val layerId: String,
    override val styleId: String? = null,
    val kind: DimensionKindV1,
    val p1: PointV1,
    val p2: PointV1,
    val text: String? = null,
    val offsetPx: Double? = null
) : EntityV1

/**
 * Supported dimension types for v1.
 */
@Serializable
enum class DimensionKindV1 {
    LINEAR
}
