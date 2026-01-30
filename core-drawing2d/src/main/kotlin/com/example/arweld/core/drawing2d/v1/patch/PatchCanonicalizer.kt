package com.example.arweld.core.drawing2d.v1.patch

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.v1.MetaEntryV1

/**
 * Returns a canonicalized copy of this [DrawingPatchEvent] with deterministic ordering.
 *
 * Canonical ordering rules (v1):
 * - Meta entries sorted by key (ascending).
 * - Ops are preserved in their provided order (semantically meaningful).
 */
fun DrawingPatchEvent.canonicalize(): DrawingPatchEvent {
    val metaComparator = compareBy<MetaEntryV1> { it.key }

    return copy(
        meta = meta.sortedWith(metaComparator)
    )
}

/**
 * Encodes this patch event as canonical JSON with deterministic ordering.
 */
fun DrawingPatchEvent.toCanonicalJson(): String {
    return Drawing2DJson.encodeToString(canonicalize())
}
