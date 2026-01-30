package com.example.arweld.core.drawing2d.v1.patch

import com.example.arweld.core.drawing2d.v1.LayerV1
import com.example.arweld.core.drawing2d.v1.entities.EntityV1
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Patch operation contract for Drawing2D v1.
 */
@Serializable
sealed interface PatchOpV1

/**
 * Add a new entity to the drawing.
 */
@Serializable
@SerialName("add_entity")
data class AddEntityOpV1(
    val entity: EntityV1
) : PatchOpV1

/**
 * Replace an existing entity (full replacement).
 */
@Serializable
@SerialName("replace_entity")
data class ReplaceEntityOpV1(
    val entity: EntityV1
) : PatchOpV1

/**
 * Remove an entity from the drawing.
 */
@Serializable
@SerialName("remove_entity")
data class RemoveEntityOpV1(
    val entityId: String
) : PatchOpV1

/**
 * Add a new layer to the drawing.
 */
@Serializable
@SerialName("add_layer")
data class AddLayerOpV1(
    val layer: LayerV1
) : PatchOpV1

/**
 * Replace an existing layer (full replacement).
 */
@Serializable
@SerialName("replace_layer")
data class ReplaceLayerOpV1(
    val layer: LayerV1
) : PatchOpV1

/**
 * Remove a layer from the drawing.
 */
@Serializable
@SerialName("remove_layer")
data class RemoveLayerOpV1(
    val layerId: String
) : PatchOpV1
