package com.example.arweld.core.drawing2d.v1.entities

import com.example.arweld.core.drawing2d.v1.PointV1
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A polyline defined by an ordered list of points.
 */
@Serializable
@SerialName("polyline")
data class PolylineV1(
    override val id: String,
    override val layerId: String,
    override val styleId: String? = null,
    val points: List<PointV1>,
    val closed: Boolean
) : EntityV1
