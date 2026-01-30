package com.example.arweld.core.drawing2d.v1.entities

import com.example.arweld.core.drawing2d.v1.PointV1
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A circle defined by its center and radius.
 */
@Serializable
@SerialName("circle")
data class CircleV1(
    override val id: String,
    override val layerId: String,
    override val styleId: String? = null,
    val c: PointV1,
    val r: Double
) : EntityV1
