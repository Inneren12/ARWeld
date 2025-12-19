package com.example.arweld.core.structural.geom

import com.example.arweld.core.structural.model.StructuralModel

/**
 * Type of AR element for export.
 */
enum class ArElementType {
    MEMBER,
    NODE,
    PLATE
}

/**
 * AR-ready export element with transform and bounding box.
 *
 * This lightweight representation allows the AR layer to consume
 * spatial data without needing full mesh details.
 *
 * @property id Unique identifier
 * @property type Element type (MEMBER, NODE, or PLATE)
 * @property transform Local-to-world transformation matrix
 * @property aabb World-space axis-aligned bounding box
 * @property meta Optional metadata (profile designation, dimensions, etc.)
 */
data class ArElement(
    val id: String,
    val type: ArElementType,
    val transform: Mat4,
    val aabb: Aabb,
    val meta: Map<String, String> = emptyMap()
)

/**
 * Export structural model as AR-ready elements.
 *
 * This function generates lightweight spatial representations suitable for AR visualization:
 * - MEMBERS: Transform and AABB from mesh generation, with profile metadata
 * - NODES: Small cube representation at node position
 * - PLATES: Minimal AABB at origin (reserved for future implementation)
 *
 * @return List of ArElement objects ready for AR consumption
 */
fun StructuralModel.exportForAR(): List<ArElement> {
    val elements = mutableListOf<ArElement>()

    // Build member and node lookup maps for O(1) access
    val memberMap = members.associateBy { it.id }
    val nodeMap = nodes.associateBy { it.id }

    // Generate member elements
    val memberMeshes = generateMembersGeometry()
    for (mesh in memberMeshes) {
        val member = memberMap[mesh.memberId] ?: continue

        val meta = mutableMapOf<String, String>()
        meta["kind"] = member.kind.name
        meta["profile"] = member.profile.designation
        meta["type"] = member.profile.type.name

        // Add length computed from node positions (robust for diagonal members)
        val startNode = nodeMap[member.nodeStartId]
        val endNode = nodeMap[member.nodeEndId]
        val length = if (startNode != null && endNode != null) {
            val dx = endNode.x - startNode.x
            val dy = endNode.y - startNode.y
            val dz = endNode.z - startNode.z
            kotlin.math.sqrt(dx * dx + dy * dy + dz * dz).toFloat()
        } else {
            // Fallback to AABB if nodes not found (shouldn't happen)
            val size = mesh.aabb.size()
            maxOf(size.x, size.y, size.z)
        }
        meta["lengthMm"] = "%.1f".format(length)

        elements.add(
            ArElement(
                id = mesh.memberId,
                type = ArElementType.MEMBER,
                transform = mesh.transform,
                aabb = mesh.aabb,
                meta = meta
            )
        )
    }

    // Generate node elements (small cubes at node positions)
    val nodeCubeSize = 20f // 20mm cube for visualization
    for (node in nodes) {
        val pos = Vec3(node.x.toFloat(), node.y.toFloat(), node.z.toFloat())
        val halfSize = nodeCubeSize / 2f

        val aabb = Aabb(
            min = pos - Vec3(halfSize, halfSize, halfSize),
            max = pos + Vec3(halfSize, halfSize, halfSize)
        )

        val transform = Mat4.translation(pos)

        elements.add(
            ArElement(
                id = node.id,
                type = ArElementType.NODE,
                transform = transform,
                aabb = aabb,
                meta = mapOf(
                    "x" to "%.1f".format(node.x),
                    "y" to "%.1f".format(node.y),
                    "z" to "%.1f".format(node.z)
                )
            )
        )
    }

    // Generate plate elements (minimal implementation for v0.1)
    for (plate in plates) {
        val t = plate.thickness.toFloat()
        val w = plate.width.toFloat()
        val l = plate.length.toFloat()

        // Simplified: plate at origin with identity transform
        // Future: proper placement based on connection geometry
        val aabb = Aabb(
            min = Vec3(-w / 2f, -t / 2f, -l / 2f),
            max = Vec3(w / 2f, t / 2f, l / 2f)
        )

        elements.add(
            ArElement(
                id = plate.id,
                type = ArElementType.PLATE,
                transform = Mat4.identity(),
                aabb = aabb,
                meta = mapOf(
                    "thickness" to "%.1f".format(t),
                    "width" to "%.1f".format(w),
                    "length" to "%.1f".format(l)
                )
            )
        )
    }

    return elements
}
