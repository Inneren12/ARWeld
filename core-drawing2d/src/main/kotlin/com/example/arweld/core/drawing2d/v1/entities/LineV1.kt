package com.example.arweld.core.drawing2d.v1.entities

import com.example.arweld.core.drawing2d.v1.PointV1
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A straight line segment between two points.
 */
@Serializable
@SerialName("line")
data class LineV1(
    override val id: String,
    override val layerId: String,
    override val styleId: String? = null,
    val a: PointV1,
    val b: PointV1
) : EntityV1
