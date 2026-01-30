package com.example.arweld.core.drawing2d.v1.entities

import com.example.arweld.core.drawing2d.v1.PointV1
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Manual or placeholder annotation text anchored in 2D space.
 */
@Serializable
@SerialName("text")
data class TextV1(
    override val id: String,
    override val layerId: String,
    override val styleId: String? = null,
    val anchor: PointV1,
    val value: String,
    val rotationDeg: Double = 0.0
) : EntityV1
