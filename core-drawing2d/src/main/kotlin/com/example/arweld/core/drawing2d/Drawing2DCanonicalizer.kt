package com.example.arweld.core.drawing2d

import com.example.arweld.core.drawing2d.v1.AttachmentRefV1
import com.example.arweld.core.drawing2d.v1.Drawing2D
import com.example.arweld.core.drawing2d.v1.LayerV1
import com.example.arweld.core.drawing2d.v1.entities.EntityV1

/**
 * Returns a canonicalized copy of this [Drawing2D] instance with deterministic ordering.
 *
 * Canonical ordering rules (v1):
 * - Layers are sorted by (order, id).
 * - Entities are sorted by (layerId, id).
 * - Attachments are sorted by (kind, relPath).
 *
 * This function does not mutate the original drawing; it returns a new instance with
 * sorted lists and shared scalar values.
 */
fun Drawing2D.canonicalize(): Drawing2D {
    val layerComparator = compareBy<LayerV1> { it.order }.thenBy { it.id }
    val entityComparator = compareBy<EntityV1> { it.layerId }.thenBy { it.id }
    val attachmentComparator = compareBy<AttachmentRefV1> { it.kind }.thenBy { it.relPath }

    return copy(
        layers = layers.sortedWith(layerComparator),
        entities = entities.sortedWith(entityComparator),
        attachments = attachments.sortedWith(attachmentComparator)
    )
}

/**
 * Encodes this drawing as canonical JSON with deterministic ordering.
 */
fun Drawing2D.toCanonicalJson(): String {
    return Drawing2DJson.encodeToString(canonicalize())
}
