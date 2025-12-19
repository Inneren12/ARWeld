package com.example.arweld.core.structural.geom

/**
 * 3D mesh for a structural member.
 *
 * The mesh is stored in LOCAL member coordinates (where the member axis is +Z from 0 to length),
 * and the transform matrix converts from local to WORLD coordinates.
 *
 * The AABB is computed in WORLD space for efficient AR usage and culling.
 *
 * @property memberId Unique identifier of the member
 * @property vertices Vertex positions as [x,y,z, x,y,z, ...] in LOCAL coordinates
 * @property indices Triangle indices (each 3 consecutive indices form a triangle)
 * @property transform Local-to-world transformation matrix
 * @property aabb World-space axis-aligned bounding box
 */
data class MemberMesh(
    val memberId: String,
    val vertices: FloatArray,
    val indices: IntArray,
    val transform: Mat4,
    val aabb: Aabb
) {
    init {
        require(vertices.size % 3 == 0) { "Vertices must be in groups of 3 (x,y,z)" }
        require(indices.all { it >= 0 && it < vertices.size / 3 }) {
            "All indices must reference valid vertices"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MemberMesh) return false

        if (memberId != other.memberId) return false
        if (!vertices.contentEquals(other.vertices)) return false
        if (!indices.contentEquals(other.indices)) return false
        if (transform != other.transform) return false
        if (aabb != other.aabb) return false

        return true
    }

    override fun hashCode(): Int {
        var result = memberId.hashCode()
        result = 31 * result + vertices.contentHashCode()
        result = 31 * result + indices.contentHashCode()
        result = 31 * result + transform.hashCode()
        result = 31 * result + aabb.hashCode()
        return result
    }
}
