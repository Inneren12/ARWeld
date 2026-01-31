package com.example.arweld.feature.drawingeditor.render

import android.util.Log
import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Member2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.feature.drawingeditor.viewmodel.Point2

/**
 * Result of resolving a member's endpoints.
 */
sealed interface MemberEndpointResult {
    /**
     * Successfully resolved both endpoints.
     * @param memberId the member ID
     * @param aNode the resolved start node
     * @param bNode the resolved end node
     */
    data class Resolved(
        val memberId: String,
        val aNode: Node2D,
        val bNode: Node2D,
    ) : MemberEndpointResult

    /**
     * Failed to resolve one or both endpoints due to missing node references.
     * @param memberId the member ID
     * @param missingNodeIds IDs of nodes that could not be found
     */
    data class MissingNodes(
        val memberId: String,
        val missingNodeIds: List<String>,
    ) : MemberEndpointResult
}

/**
 * Resolved member data ready for rendering.
 */
data class ResolvedMember(
    val memberId: String,
    val startPoint: Point2,
    val endPoint: Point2,
    val profileRef: String? = null,
)

private const val TAG = "DrawingRenderHelpers"

/**
 * Builds a lookup map from node ID to Node2D for efficient endpoint resolution.
 */
fun buildNodeLookup(drawing: Drawing2D): Map<String, Node2D> =
    drawing.nodes.associateBy { it.id }

/**
 * Resolves a single member's endpoints using the provided node lookup.
 * Returns [MemberEndpointResult.Resolved] if both nodes are found,
 * [MemberEndpointResult.MissingNodes] otherwise with debug logging.
 */
fun resolveMemberEndpoints(
    member: Member2D,
    nodeLookup: Map<String, Node2D>,
    logMissing: Boolean = true,
): MemberEndpointResult {
    val aNode = nodeLookup[member.aNodeId]
    val bNode = nodeLookup[member.bNodeId]

    val missingIds = mutableListOf<String>()
    if (aNode == null) {
        missingIds.add(member.aNodeId)
        if (logMissing) {
            Log.d(TAG, "Member ${member.id}: missing aNodeId=${member.aNodeId}")
        }
    }
    if (bNode == null) {
        missingIds.add(member.bNodeId)
        if (logMissing) {
            Log.d(TAG, "Member ${member.id}: missing bNodeId=${member.bNodeId}")
        }
    }

    return if (missingIds.isEmpty()) {
        MemberEndpointResult.Resolved(
            memberId = member.id,
            aNode = aNode!!,
            bNode = bNode!!,
        )
    } else {
        MemberEndpointResult.MissingNodes(
            memberId = member.id,
            missingNodeIds = missingIds,
        )
    }
}

/**
 * Resolves all members in a drawing to their endpoints.
 * Gracefully skips members with missing node references (debug logged).
 * @return list of [ResolvedMember] ready for rendering
 */
fun resolveAllMemberEndpoints(drawing: Drawing2D): List<ResolvedMember> {
    val nodeLookup = buildNodeLookup(drawing)
    return drawing.members.mapNotNull { member ->
        when (val result = resolveMemberEndpoints(member, nodeLookup)) {
            is MemberEndpointResult.Resolved -> ResolvedMember(
                memberId = result.memberId,
                startPoint = Point2(result.aNode.x.toFloat(), result.aNode.y.toFloat()),
                endPoint = Point2(result.bNode.x.toFloat(), result.bNode.y.toFloat()),
                profileRef = member.profileRef,
            )
            is MemberEndpointResult.MissingNodes -> null
        }
    }
}

/**
 * Converts Node2D coordinates to Point2 for rendering.
 */
fun Node2D.toPoint2(): Point2 = Point2(x.toFloat(), y.toFloat())

/**
 * Resolves all members and returns both resolved members and a count of skipped members.
 */
data class MemberResolutionSummary(
    val resolvedMembers: List<ResolvedMember>,
    val skippedCount: Int,
)

fun resolveAllMemberEndpointsWithSummary(drawing: Drawing2D): MemberResolutionSummary {
    val nodeLookup = buildNodeLookup(drawing)
    val resolved = mutableListOf<ResolvedMember>()
    var skipped = 0

    for (member in drawing.members) {
        when (val result = resolveMemberEndpoints(member, nodeLookup)) {
            is MemberEndpointResult.Resolved -> resolved.add(
                ResolvedMember(
                    memberId = result.memberId,
                    startPoint = Point2(result.aNode.x.toFloat(), result.aNode.y.toFloat()),
                    endPoint = Point2(result.bNode.x.toFloat(), result.bNode.y.toFloat()),
                    profileRef = member.profileRef,
                )
            )
            is MemberEndpointResult.MissingNodes -> skipped++
        }
    }

    return MemberResolutionSummary(
        resolvedMembers = resolved,
        skippedCount = skipped,
    )
}
