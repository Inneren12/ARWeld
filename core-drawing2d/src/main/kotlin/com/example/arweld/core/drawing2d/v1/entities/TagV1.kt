package com.example.arweld.core.drawing2d.v1.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Key/value tag attached to a target entity.
 */
@Serializable
@SerialName("tag")
data class TagV1(
    override val id: String,
    override val layerId: String,
    override val styleId: String? = null,
    val targetId: String,
    val key: String,
    val value: String
) : EntityV1
