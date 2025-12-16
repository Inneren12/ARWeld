package com.example.arweld.feature.arview.alignment

import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Solves for the rigid transform (R, t) that aligns model-space points to
 * world-space observations using Horn's quaternion-based absolute orientation
 * method. The resulting pose expresses T_world_model.
 */
class RigidTransformSolver {

    fun solveRigidTransform(
        modelPoints: List<Vector3>,
        worldPoints: List<Vector3>,
    ): Pose3D? {
        if (modelPoints.size != worldPoints.size || modelPoints.size < 3) return null

        val modelCentroid = centroid(modelPoints)
        val worldCentroid = centroid(worldPoints)

        val centeredModel = modelPoints.map { it - modelCentroid }
        val centeredWorld = worldPoints.map { it - worldCentroid }

        val covariance = computeCrossCovariance(centeredModel, centeredWorld)
        val rotation = computeRotation(covariance) ?: return null
        val translatedModelCentroid = rotation.rotate(modelCentroid)
        val translation = worldCentroid - translatedModelCentroid

        return Pose3D(position = translation, rotation = rotation)
    }

    private fun centroid(points: List<Vector3>): Vector3 {
        if (points.isEmpty()) return Vector3(0.0, 0.0, 0.0)
        val sum = points.reduce { acc, v -> acc + v }
        val scale = 1.0 / points.size
        return sum * scale
    }

    private fun computeCrossCovariance(
        modelPoints: List<Vector3>,
        worldPoints: List<Vector3>,
    ): Array<DoubleArray> {
        val matrix = Array(3) { DoubleArray(3) }
        for (i in modelPoints.indices) {
            val m = modelPoints[i]
            val w = worldPoints[i]
            matrix[0][0] += w.x * m.x
            matrix[0][1] += w.x * m.y
            matrix[0][2] += w.x * m.z
            matrix[1][0] += w.y * m.x
            matrix[1][1] += w.y * m.y
            matrix[1][2] += w.y * m.z
            matrix[2][0] += w.z * m.x
            matrix[2][1] += w.z * m.y
            matrix[2][2] += w.z * m.z
        }
        return matrix
    }

    private fun computeRotation(covariance: Array<DoubleArray>): Quaternion? {
        val hornMatrix = buildHornMatrix(covariance)
        val eigenVector = dominantEigenVector(hornMatrix) ?: return null
        val norm = sqrt(eigenVector.sumOf { it * it })
        if (norm < 1e-9) return null
        val normalized = eigenVector.map { it / norm }
        return Quaternion(
            x = normalized[1],
            y = normalized[2],
            z = normalized[3],
            w = normalized[0],
        ).normalized()
    }

    private fun buildHornMatrix(covariance: Array<DoubleArray>): Array<DoubleArray> {
        val sxx = covariance[0][0]
        val sxy = covariance[0][1]
        val sxz = covariance[0][2]
        val syx = covariance[1][0]
        val syy = covariance[1][1]
        val syz = covariance[1][2]
        val szx = covariance[2][0]
        val szy = covariance[2][1]
        val szz = covariance[2][2]

        val trace = sxx + syy + szz

        return arrayOf(
            doubleArrayOf(trace, syz - szy, szx - sxz, sxy - syx),
            doubleArrayOf(syz - szy, sxx - syy - szz, sxy + syx, szx + sxz),
            doubleArrayOf(szx - sxz, sxy + syx, -sxx + syy - szz, syz + szy),
            doubleArrayOf(sxy - syx, szx + sxz, syz + szy, -sxx - syy + szz),
        )
    }

    private fun dominantEigenVector(matrix: Array<DoubleArray>, iterations: Int = 50): DoubleArray? {
        var vector = doubleArrayOf(1.0, 0.0, 0.0, 0.0)
        for (i in 0 until iterations) {
            val next = multiply4(matrix, vector)
            val norm = sqrt(next.sumOf { it * it })
            if (norm < 1e-12) return null
            val normalized = next.map { it / norm }.toDoubleArray()
            val delta = normalized.zip(vector) { a, b -> abs(a - b) }.maxOrNull() ?: 0.0
            vector = normalized
            if (delta < 1e-9) break
        }
        return vector
    }

    private fun multiply4(matrix: Array<DoubleArray>, vector: DoubleArray): DoubleArray {
        return doubleArrayOf(
            matrix[0][0] * vector[0] + matrix[0][1] * vector[1] + matrix[0][2] * vector[2] + matrix[0][3] * vector[3],
            matrix[1][0] * vector[0] + matrix[1][1] * vector[1] + matrix[1][2] * vector[2] + matrix[1][3] * vector[3],
            matrix[2][0] * vector[0] + matrix[2][1] * vector[1] + matrix[2][2] * vector[2] + matrix[2][3] * vector[3],
            matrix[3][0] * vector[0] + matrix[3][1] * vector[1] + matrix[3][2] * vector[2] + matrix[3][3] * vector[3],
        )
    }
}
