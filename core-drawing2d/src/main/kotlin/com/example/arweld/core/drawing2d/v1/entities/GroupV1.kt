package com.example.arweld.core.drawing2d.v1.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Grouping entity referencing other entities by id.
 */
@Serializable
@SerialName("group")
data class GroupV1(
    override val id: String,
    override val layerId: String,
    override val styleId: String? = null,
    val members: List<String>
) : EntityV1
