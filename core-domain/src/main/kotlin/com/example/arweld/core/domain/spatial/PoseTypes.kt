package com.example.arweld.core.domain.spatial

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Camera intrinsics in pixel units for pinhole projection.
 */
data class CameraIntrinsics(
    val fx: Double,
    val fy: Double,
    val cx: Double,
    val cy: Double,
    val width: Int,
    val height: Int,
)

/**
 * Basic 3D vector for spatial math.
 */
data class Vector3(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    operator fun plus(other: Vector3): Vector3 = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3): Vector3 = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scale: Double): Vector3 = Vector3(x * scale, y * scale, z * scale)

    fun dot(other: Vector3): Double = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3): Vector3 = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x,
    )

    fun norm(): Double = sqrt(dot(this))

    fun normalized(): Vector3 {
        val magnitude = norm()
        if (magnitude == 0.0) return this
        return this * (1.0 / magnitude)
    }
}

/**
 * Quaternion representation of rotation using the (x, y, z, w) convention.
 */
data class Quaternion(
    val x: Double,
    val y: Double,
    val z: Double,
    val w: Double,
) {
    operator fun times(other: Quaternion): Quaternion {
        return Quaternion(
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w,
            w * other.w - x * other.x - y * other.y - z * other.z,
        ).normalized()
    }

    fun rotate(vector: Vector3): Vector3 {
        val qVec = Vector3(x, y, z)
        val uv = qVec.cross(vector)
        val uuv = qVec.cross(uv)
        val twoW = 2.0 * w
        val two = 2.0
        return vector + (uv * twoW) + (uuv * two)
    }

    fun normalized(): Quaternion {
        val magnitude = sqrt(x * x + y * y + z * z + w * w)
        if (magnitude == 0.0) return this
        return Quaternion(x / magnitude, y / magnitude, z / magnitude, w / magnitude)
    }

    companion object {
        val Identity = Quaternion(0.0, 0.0, 0.0, 1.0)

        fun fromRotationMatrix(matrix: Array<DoubleArray>): Quaternion {
            val trace = matrix[0][0] + matrix[1][1] + matrix[2][2]
            return if (trace > 0) {
                val s = sqrt(trace + 1.0) * 2.0
                val invS = 1.0 / s
                Quaternion(
                    (matrix[2][1] - matrix[1][2]) * invS,
                    (matrix[0][2] - matrix[2][0]) * invS,
                    (matrix[1][0] - matrix[0][1]) * invS,
                    0.25 * s,
                ).normalized()
            } else if ((matrix[0][0] > matrix[1][1]) && (matrix[0][0] > matrix[2][2])) {
                val s = sqrt(1.0 + matrix[0][0] - matrix[1][1] - matrix[2][2]) * 2.0
                val invS = 1.0 / s
                Quaternion(
                    0.25 * s,
                    (matrix[0][1] + matrix[1][0]) * invS,
                    (matrix[0][2] + matrix[2][0]) * invS,
                    (matrix[2][1] - matrix[1][2]) * invS,
                ).normalized()
            } else if (matrix[1][1] > matrix[2][2]) {
                val s = sqrt(1.0 + matrix[1][1] - matrix[0][0] - matrix[2][2]) * 2.0
                val invS = 1.0 / s
                Quaternion(
                    (matrix[0][1] + matrix[1][0]) * invS,
                    0.25 * s,
                    (matrix[1][2] + matrix[2][1]) * invS,
                    (matrix[0][2] - matrix[2][0]) * invS,
                ).normalized()
            } else {
                val s = sqrt(1.0 + matrix[2][2] - matrix[0][0] - matrix[1][1]) * 2.0
                val invS = 1.0 / s
                Quaternion(
                    (matrix[0][2] + matrix[2][0]) * invS,
                    (matrix[1][2] + matrix[2][1]) * invS,
                    0.25 * s,
                    (matrix[1][0] - matrix[0][1]) * invS,
                ).normalized()
            }
        }
    }
}

/**
 * Pose consisting of translation and rotation in 3D space.
 */
data class Pose3D(
    val position: Vector3,
    val rotation: Quaternion,
) {
    fun compose(child: Pose3D): Pose3D {
        val rotatedChild = rotation.rotate(child.position)
        val newPosition = position + rotatedChild
        val newRotation = rotation * child.rotation
        return Pose3D(newPosition, newRotation)
    }

    fun inverse(): Pose3D {
        val invRotation = Quaternion(-rotation.x, -rotation.y, -rotation.z, rotation.w).normalized()
        val invPosition = invRotation.rotate(Vector3(-position.x, -position.y, -position.z))
        return Pose3D(invPosition, invRotation)
    }

    fun toMatrix4(): FloatArray {
        val x = rotation.x
        val y = rotation.y
        val z = rotation.z
        val w = rotation.w
        val xx = x * x
        val yy = y * y
        val zz = z * z
        val xy = x * y
        val xz = x * z
        val yz = y * z
        val wx = w * x
        val wy = w * y
        val wz = w * z

        return floatArrayOf(
            (1 - 2 * (yy + zz)).toFloat(), (2 * (xy - wz)).toFloat(), (2 * (xz + wy)).toFloat(), 0f,
            (2 * (xy + wz)).toFloat(), (1 - 2 * (xx + zz)).toFloat(), (2 * (yz - wx)).toFloat(), 0f,
            (2 * (xz - wy)).toFloat(), (2 * (yz + wx)).toFloat(), (1 - 2 * (xx + yy)).toFloat(), 0f,
            position.x.toFloat(), position.y.toFloat(), position.z.toFloat(), 1f,
        )
    }

    companion object {
        val Identity = Pose3D(Vector3(0.0, 0.0, 0.0), Quaternion.Identity)
    }
}

/**
 * Computes the angular difference between two orientations (in radians).
 */
fun Quaternion.angularDistance(other: Quaternion): Double {
    val dot = x * other.x + y * other.y + z * other.z + w * other.w
    val clamped = dot.coerceIn(-1.0, 1.0)
    return 2.0 * acos(abs(clamped))
}
