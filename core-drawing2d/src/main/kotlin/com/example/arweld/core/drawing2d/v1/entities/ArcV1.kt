package com.example.arweld.core.drawing2d.v1.entities

import com.example.arweld.core.drawing2d.v1.PointV1
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An arc defined by its center, radius, and angular sweep.
 */
@Serializable
@SerialName("arc")
data class ArcV1(
    override val id: String,
    override val layerId: String,
    override val styleId: String? = null,
    val c: PointV1,
    val r: Double,
    val startAngleDeg: Double,
    val endAngleDeg: Double,
    val cw: Boolean
) : EntityV1
