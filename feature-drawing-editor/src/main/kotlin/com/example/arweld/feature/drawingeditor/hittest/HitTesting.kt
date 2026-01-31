package com.example.arweld.feature.drawingeditor.hittest

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Member2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.feature.drawingeditor.viewmodel.EditorSelection
import com.example.arweld.feature.drawingeditor.viewmodel.ViewTransform
import kotlin.math.abs

private const val MIN_SCALE_EPSILON = 1e-6
private const val DISTANCE_EPSILON = 1e-9

fun hitTestNode(
    worldTap: Point2D,
    nodes: List<Node2D>,
    tolerancePx: Float,
    viewTransform: ViewTransform,
): String? {
    val toleranceWorld = tolerancePxToWorld(tolerancePx, viewTransform)
    val toleranceSq = toleranceWorld * toleranceWorld
    var bestId: String? = null
    var bestDistanceSq = Double.POSITIVE_INFINITY

    for (node in nodes) {
        val dx = worldTap.x - node.x
        val dy = worldTap.y - node.y
        val distanceSq = dx * dx + dy * dy
        if (distanceSq <= toleranceSq) {
            val better = distanceSq + DISTANCE_EPSILON < bestDistanceSq
            val tieBreak = abs(distanceSq - bestDistanceSq) <= DISTANCE_EPSILON &&
                (bestId == null || node.id < bestId!!)
            if (better || tieBreak) {
                bestDistanceSq = distanceSq
                bestId = node.id
            }
        }
    }
    return bestId
}

fun hitTestMember(
    worldTap: Point2D,
    members: List<Member2D>,
    nodes: List<Node2D>,
    tolerancePx: Float,
    viewTransform: ViewTransform,
): String? {
    val toleranceWorld = tolerancePxToWorld(tolerancePx, viewTransform)
    val toleranceSq = toleranceWorld * toleranceWorld
    val nodeLookup = nodes.associateBy { it.id }
    var bestId: String? = null
    var bestDistanceSq = Double.POSITIVE_INFINITY

    for (member in members) {
        val aNode = nodeLookup[member.aNodeId] ?: continue
        val bNode = nodeLookup[member.bNodeId] ?: continue
        val distanceSq = distanceSquaredPointToSegment(
            point = worldTap,
            start = Point2D(aNode.x, aNode.y),
            end = Point2D(bNode.x, bNode.y),
        )
        if (distanceSq <= toleranceSq) {
            val better = distanceSq + DISTANCE_EPSILON < bestDistanceSq
            val tieBreak = abs(distanceSq - bestDistanceSq) <= DISTANCE_EPSILON &&
                (bestId == null || member.id < bestId!!)
            if (better || tieBreak) {
                bestDistanceSq = distanceSq
                bestId = member.id
            }
        }
    }
    return bestId
}

fun selectEntityAtTap(
    worldTap: Point2D,
    drawing: Drawing2D,
    tolerancePx: Float,
    viewTransform: ViewTransform,
): EditorSelection {
    val nodeId = hitTestNode(
        worldTap = worldTap,
        nodes = drawing.nodes,
        tolerancePx = tolerancePx,
        viewTransform = viewTransform,
    )
    if (nodeId != null) {
        return EditorSelection.Node(nodeId)
    }
    val memberId = hitTestMember(
        worldTap = worldTap,
        members = drawing.members,
        nodes = drawing.nodes,
        tolerancePx = tolerancePx,
        viewTransform = viewTransform,
    )
    return if (memberId != null) {
        EditorSelection.Member(memberId)
    } else {
        EditorSelection.None
    }
}

internal fun distanceSquaredPointToSegment(
    point: Point2D,
    start: Point2D,
    end: Point2D,
): Double {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val lengthSq = dx * dx + dy * dy
    if (lengthSq <= DISTANCE_EPSILON) {
        val px = point.x - start.x
        val py = point.y - start.y
        return px * px + py * py
    }
    val t = ((point.x - start.x) * dx + (point.y - start.y) * dy) / lengthSq
    val clampedT = t.coerceIn(0.0, 1.0)
    val projX = start.x + clampedT * dx
    val projY = start.y + clampedT * dy
    val px = point.x - projX
    val py = point.y - projY
    return px * px + py * py
}

private fun tolerancePxToWorld(tolerancePx: Float, viewTransform: ViewTransform): Double {
    val scale = viewTransform.scale.toDouble().coerceAtLeast(MIN_SCALE_EPSILON)
    return tolerancePx.toDouble() / scale
}
