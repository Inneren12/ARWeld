package com.example.arweld.core.structural.geom

import kotlin.math.cos
import kotlin.math.sin

/**
 * 4x4 transformation matrix stored in column-major order.
 *
 * Column-major indexing:
 * m[0..3]   = first column  (right vector + wx)
 * m[4..7]   = second column (up vector + wy)
 * m[8..11]  = third column  (forward vector + wz)
 * m[12..15] = fourth column (translation + w)
 */
data class Mat4(val m: FloatArray) {

    init {
        require(m.size == 16) { "Mat4 requires exactly 16 elements" }
    }

    /**
     * Multiply this matrix by another: this * other.
     * Returns a new matrix representing the combined transformation.
     */
    operator fun times(other: Mat4): Mat4 {
        val result = FloatArray(16)
        for (row in 0..3) {
            for (col in 0..3) {
                var sum = 0f
                for (k in 0..3) {
                    sum += m[k * 4 + row] * other.m[col * 4 + k]
                }
                result[col * 4 + row] = sum
            }
        }
        return Mat4(result)
    }

    /**
     * Transform a point by this matrix (applies translation).
     */
    fun transformPoint(v: Vec3): Vec3 {
        val x = m[0] * v.x + m[4] * v.y + m[8] * v.z + m[12]
        val y = m[1] * v.x + m[5] * v.y + m[9] * v.z + m[13]
        val z = m[2] * v.x + m[6] * v.y + m[10] * v.z + m[14]
        return Vec3(x, y, z)
    }

    /**
     * Transform a direction by this matrix (ignores translation).
     */
    fun transformDirection(v: Vec3): Vec3 {
        val x = m[0] * v.x + m[4] * v.y + m[8] * v.z
        val y = m[1] * v.x + m[5] * v.y + m[9] * v.z
        val z = m[2] * v.x + m[6] * v.y + m[10] * v.z
        return Vec3(x, y, z)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Mat4) return false
        return m.contentEquals(other.m)
    }

    override fun hashCode(): Int = m.contentHashCode()

    companion object {
        /**
         * Identity matrix.
         */
        fun identity(): Mat4 = Mat4(
            floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
            )
        )

        /**
         * Translation matrix.
         */
        fun translation(x: Float, y: Float, z: Float): Mat4 = Mat4(
            floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                x, y, z, 1f
            )
        )

        fun translation(v: Vec3): Mat4 = translation(v.x, v.y, v.z)

        /**
         * Rotation matrix from axis and angle (in degrees).
         */
        fun rotationFromAxisAngle(axis: Vec3, angleDeg: Float): Mat4 {
            val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
            val c = cos(angleRad)
            val s = sin(angleRad)
            val t = 1f - c

            val n = axis.normalize()
            val x = n.x
            val y = n.y
            val z = n.z

            return Mat4(
                floatArrayOf(
                    t * x * x + c, t * x * y + s * z, t * x * z - s * y, 0f,
                    t * x * y - s * z, t * y * y + c, t * y * z + s * x, 0f,
                    t * x * z + s * y, t * y * z - s * x, t * z * z + c, 0f,
                    0f, 0f, 0f, 1f
                )
            )
        }

        /**
         * Create a rotation matrix from an orthonormal basis and position.
         * xAxis, yAxis, zAxis should be orthonormal unit vectors.
         */
        fun fromBasis(xAxis: Vec3, yAxis: Vec3, zAxis: Vec3, pos: Vec3 = Vec3.ZERO): Mat4 = Mat4(
            floatArrayOf(
                xAxis.x, xAxis.y, xAxis.z, 0f,
                yAxis.x, yAxis.y, yAxis.z, 0f,
                zAxis.x, zAxis.y, zAxis.z, 0f,
                pos.x, pos.y, pos.z, 1f
            )
        )
    }
}
