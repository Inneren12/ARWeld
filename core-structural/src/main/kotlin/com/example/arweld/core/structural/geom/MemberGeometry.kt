package com.example.arweld.core.structural.geom

import com.example.arweld.core.structural.model.Member
import com.example.arweld.core.structural.model.Node
import com.example.arweld.core.structural.model.StructuralModel
import kotlin.math.sqrt

/**
 * Generate 3D geometry for all members in a structural model.
 *
 * Each member is generated in LOCAL coordinates (member axis = +Z from 0 to length),
 * with the cross-section centered at the origin in the XY plane.
 * The transform matrix converts from local to WORLD coordinates.
 * The AABB is computed in WORLD space.
 *
 * @return List of MemberMesh objects, one per member
 */
fun StructuralModel.generateMembersGeometry(): List<MemberMesh> {
    val nodeMap = nodes.associateBy { it.id }
    return members.mapNotNull { member ->
        generateMemberMesh(member, nodeMap)
    }
}

/**
 * Generate mesh for a single member.
 */
private fun generateMemberMesh(member: Member, nodeMap: Map<String, Node>): MemberMesh? {
    val startNode = nodeMap[member.nodeStartId] ?: return null
    val endNode = nodeMap[member.nodeEndId] ?: return null

    // Convert node positions from Double (mm) to Float
    val start = Vec3(
        startNode.x.toFloat(),
        startNode.y.toFloat(),
        startNode.z.toFloat()
    )
    val end = Vec3(
        endNode.x.toFloat(),
        endNode.y.toFloat(),
        endNode.z.toFloat()
    )

    // Compute member direction and length
    val dir = end - start
    val length = dir.length()

    // Validate length
    if (length < 1e-3f) {
        // Skip members with near-zero length
        return null
    }

    // Build local-to-world transform
    val transform = buildMemberTransform(start, dir, length, member.orientationMeta?.rollAngleDeg ?: 0.0)

    // Generate mesh in local coordinates
    val (vertices, indices) = try {
        ProfileMeshBuilder.buildProfileMesh(member.profile, length)
    } catch (e: Exception) {
        // Skip members with invalid profile data
        return null
    }

    // Compute world-space AABB
    val aabb = computeWorldAabb(vertices, transform)

    return MemberMesh(
        memberId = member.id,
        vertices = vertices,
        indices = indices,
        transform = transform,
        aabb = aabb
    )
}

/**
 * Build transformation matrix from local member coordinates to world coordinates.
 *
 * Local coordinates:
 * - Member axis is +Z (from z=0 to z=length)
 * - Cross-section in XY plane centered at origin
 *
 * World coordinates:
 * - Member positioned from start to end
 * - Rotated to align +Z with member direction
 * - Additional roll rotation around member axis
 */
private fun buildMemberTransform(
    start: Vec3,
    dir: Vec3,
    length: Float,
    rollAngleDeg: Double
): Mat4 {
    // Normalize direction
    val zAxis = dir.normalize()

    // Build orthonormal basis
    // Choose an up vector that's not parallel to zAxis
    val up = if (kotlin.math.abs(zAxis.z) < 0.99f) {
        Vec3.Z_AXIS
    } else {
        Vec3.X_AXIS
    }

    // Compute right vector (X axis)
    val xAxis = up.cross(zAxis).normalize()

    // Compute actual up vector (Y axis)
    val yAxis = zAxis.cross(xAxis).normalize()

    // Create rotation matrix from basis
    var rotation = Mat4.fromBasis(xAxis, yAxis, zAxis, Vec3.ZERO)

    // Apply roll angle around member axis (Z axis in local coords)
    if (kotlin.math.abs(rollAngleDeg) > 1e-6) {
        val rollRotation = Mat4.rotationFromAxisAngle(Vec3.Z_AXIS, rollAngleDeg.toFloat())
        rotation = rotation * rollRotation
    }

    // Apply translation to start position
    val translation = Mat4.translation(start)

    return translation * rotation
}

/**
 * Compute world-space AABB from local vertices and transform.
 */
private fun computeWorldAabb(vertices: FloatArray, transform: Mat4): Aabb {
    var aabb = Aabb.empty()

    // Transform each vertex to world space and expand AABB
    var i = 0
    while (i < vertices.size) {
        val localPos = Vec3(vertices[i], vertices[i + 1], vertices[i + 2])
        val worldPos = transform.transformPoint(localPos)
        aabb = aabb.include(worldPos)
        i += 3
    }

    return aabb
}
