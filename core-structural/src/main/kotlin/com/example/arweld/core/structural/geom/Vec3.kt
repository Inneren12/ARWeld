package com.example.arweld.core.structural.geom

import kotlin.math.sqrt

/**
 * 3D vector with float precision.
 * Used for positions, directions, and normals in geometry generation.
 */
data class Vec3(val x: Float, val y: Float, val z: Float) {

    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)

    operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)

    operator fun div(scalar: Float) = Vec3(x / scalar, y / scalar, z / scalar)

    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vec3): Vec3 = Vec3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun lengthSquared(): Float = x * x + y * y + z * z

    fun normalize(): Vec3 {
        val len = length()
        return if (len > 1e-6f) this / len else Vec3(0f, 0f, 0f)
    }

    companion object {
        val ZERO = Vec3(0f, 0f, 0f)
        val X_AXIS = Vec3(1f, 0f, 0f)
        val Y_AXIS = Vec3(0f, 1f, 0f)
        val Z_AXIS = Vec3(0f, 0f, 1f)
    }
}
