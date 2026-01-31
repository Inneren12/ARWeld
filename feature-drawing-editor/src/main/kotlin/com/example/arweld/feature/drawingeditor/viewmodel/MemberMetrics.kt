package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Member2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.core.drawing2d.editor.v1.ScaleInfo
import kotlin.math.sqrt

sealed interface MemberEndpointResolution {
    data class Resolved(
        val memberId: String,
        val nodeA: Node2D,
        val nodeB: Node2D,
    ) : MemberEndpointResolution

    data class MissingMember(val memberId: String) : MemberEndpointResolution

    data class MissingNodes(
        val memberId: String,
        val missingNodeIds: List<String>,
    ) : MemberEndpointResolution
}

private const val MEMBER_LENGTH_PX_DECIMALS = 2
private const val MEMBER_LENGTH_MM_DECIMALS = 1

fun resolveMemberEndpoints(drawing: Drawing2D, memberId: String): MemberEndpointResolution {
    val member = drawing.members.firstOrNull { it.id == memberId }
        ?: return MemberEndpointResolution.MissingMember(memberId)
    return resolveMemberEndpoints(drawing, member)
}

private fun resolveMemberEndpoints(
    drawing: Drawing2D,
    member: Member2D,
): MemberEndpointResolution {
    val nodeLookup = drawing.nodes.associateBy { it.id }
    val missingNodeIds = mutableListOf<String>()
    val nodeA = nodeLookup[member.aNodeId] ?: run {
        missingNodeIds.add(member.aNodeId)
        null
    }
    val nodeB = nodeLookup[member.bNodeId] ?: run {
        missingNodeIds.add(member.bNodeId)
        null
    }
    return if (missingNodeIds.isEmpty() && nodeA != null && nodeB != null) {
        MemberEndpointResolution.Resolved(
            memberId = member.id,
            nodeA = nodeA,
            nodeB = nodeB,
        )
    } else {
        MemberEndpointResolution.MissingNodes(
            memberId = member.id,
            missingNodeIds = missingNodeIds.distinct(),
        )
    }
}

fun computeLengthPx(nodeA: Node2D, nodeB: Node2D): Double {
    val dx = nodeA.x - nodeB.x
    val dy = nodeA.y - nodeB.y
    return sqrt(dx * dx + dy * dy)
}

fun computeMmPerPx(scaleInfo: ScaleInfo): Double {
    val distancePx = distanceBetween(scaleInfo.pointA, scaleInfo.pointB)
    if (distancePx <= 0.0) {
        return Double.NaN
    }
    return scaleInfo.realLengthMm / distancePx
}

fun computeLengthMm(lengthPx: Double, mmPerPx: Double): Double {
    return lengthPx * mmPerPx
}

fun computeLengthMmOrNull(lengthPx: Double, scaleInfo: ScaleInfo?): Double? {
    if (scaleInfo == null) {
        return null
    }
    val mmPerPx = computeMmPerPx(scaleInfo)
    if (!mmPerPx.isFinite() || mmPerPx <= 0.0) {
        return null
    }
    return computeLengthMm(lengthPx, mmPerPx)
}

fun formatMemberLengthPx(value: Double): String = formatScaleValue(value, MEMBER_LENGTH_PX_DECIMALS)

fun formatMemberLengthMm(value: Double): String = formatScaleValue(value, MEMBER_LENGTH_MM_DECIMALS)
