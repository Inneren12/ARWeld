package com.example.arweld.core.drawing2d.editor.v1

import com.example.arweld.core.drawing2d.v1.MetaEntryV1
import kotlinx.serialization.Serializable

/**
 * Manual 2D editor schema (v1) for 2D→3D authoring.
 */
@Serializable
data class Drawing2D(
    val schemaVersion: Int = 1,
    val nodes: List<Node2D>,
    val members: List<Member2D>,
    val scale: ScaleInfo? = null,
    val meta: List<MetaEntryV1>? = null
)

@Serializable
data class Node2D(
    val id: String,
    val x: Double,
    val y: Double,
    val meta: List<MetaEntryV1>? = null
)

@Serializable
data class Member2D(
    val id: String,
    val aNodeId: String,
    val bNodeId: String,
    val profileRef: String? = null,
    val meta: List<MetaEntryV1>? = null
)

@Serializable
data class ScaleInfo(
    val pointA: Point2D,
    val pointB: Point2D,
    val realLengthMm: Double
)

@Serializable
data class Point2D(
    val x: Double,
    val y: Double
)

data class MissingNodeReference(
    val memberId: String,
    val missingNodeId: String
)

/**
 * Non-throwing validation helper that reports members referencing missing nodes.
 */
fun Drawing2D.missingNodeReferences(): List<MissingNodeReference> {
    val nodeIds = nodes.map { it.id }.toSet()
    val missing = mutableListOf<MissingNodeReference>()
    for (member in members) {
        if (!nodeIds.contains(member.aNodeId)) {
            missing.add(MissingNodeReference(memberId = member.id, missingNodeId = member.aNodeId))
        }
        if (!nodeIds.contains(member.bNodeId)) {
            missing.add(MissingNodeReference(memberId = member.id, missingNodeId = member.bNodeId))
        }
    }
    return missing
}
