package com.example.arweld.core.drawing2d.editor.v1

import com.example.arweld.core.drawing2d.Drawing2DJson

/**
 * Returns a canonicalized copy of this [Drawing2D] instance with deterministic ordering.
 *
 * Canonical ordering rules (editor v1):
 * - Nodes are sorted by id ascending.
 * - Members are sorted by id ascending.
 */
fun Drawing2D.canonicalize(): Drawing2D {
    val nodeComparator = compareBy<Node2D> { it.id }
    val memberComparator = compareBy<Member2D> { it.id }

    return copy(
        nodes = nodes.sortedWith(nodeComparator),
        members = members.sortedWith(memberComparator)
    )
}

/**
 * Encodes this drawing as canonical JSON with deterministic ordering.
 */
fun Drawing2D.toCanonicalJson(): String {
    return Drawing2DJson.encodeToString(canonicalize())
}
