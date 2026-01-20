package com.example.arweld.feature.arview.pose

import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import kotlin.math.sqrt

/**
 * Refines the world-zone pose using multiple markers.
 *
 * We solve for a small pose update Δξ = [ω, t] that minimizes:
 *   Σ || (R0 * exp([ω]x) * p + t0 + t) - q ||^2
 * Linearizing exp([ω]x) ≈ I + [ω]x gives a standard least-squares system:
 *   [ -R0 [p]x | I ] Δξ = (q - (R0 * p + t0))
 * This is solved via normal equations for a deterministic, single-step update.
 */
class MultiMarkerPoseRefiner {

    data class MarkerObservation(
        val markerId: String,
        val markerPoseCamera: Pose3D,
        val markerSizeMeters: Float,
        val tMarkerZone: Pose3D,
    )

    data class RefinedPoseResult(
        val worldZonePose: Pose3D,
        val residualErrorMm: Double,
        val usedMarkers: Int,
    )

    fun refinePose(
        cameraPoseWorld: Pose3D,
        observations: List<MarkerObservation>,
        initialPose: Pose3D? = null,
    ): RefinedPoseResult? {
        if (observations.isEmpty()) return null

        val referencePose = initialPose ?: seedPose(cameraPoseWorld, observations.first())
        val correspondences = buildCorrespondences(cameraPoseWorld, observations)
        if (correspondences.isEmpty()) return null

        val (modelPoints, worldPoints) = correspondences
        val delta = solveLinearizedUpdate(referencePose, modelPoints, worldPoints) ?: return null
        val refinedPose = applyDelta(referencePose, delta)
        val residualMm = computeResidualMm(refinedPose, modelPoints, worldPoints)

        return RefinedPoseResult(
            worldZonePose = refinedPose,
            residualErrorMm = residualMm,
            usedMarkers = observations.size,
        )
    }

    private fun seedPose(cameraPoseWorld: Pose3D, observation: MarkerObservation): Pose3D {
        val markerWorldPose = cameraPoseWorld.compose(observation.markerPoseCamera)
        return markerWorldPose.compose(observation.tMarkerZone)
    }

    private fun buildCorrespondences(
        cameraPoseWorld: Pose3D,
        observations: List<MarkerObservation>,
    ): Pair<List<Vector3>, List<Vector3>> {
        val modelPoints = mutableListOf<Vector3>()
        val worldPoints = mutableListOf<Vector3>()

        observations.forEach { observation ->
            val markerPoints = markerReferencePoints(observation.markerSizeMeters)
            val markerWorldPose = cameraPoseWorld.compose(observation.markerPoseCamera)
            markerPoints.forEach { markerPoint ->
                val zonePoint = transformPoint(observation.tMarkerZone, markerPoint)
                val worldPoint = transformPoint(markerWorldPose, markerPoint)
                modelPoints.add(zonePoint)
                worldPoints.add(worldPoint)
            }
        }

        return modelPoints to worldPoints
    }

    private fun markerReferencePoints(markerSizeMeters: Float): List<Vector3> {
        val half = markerSizeMeters.toDouble() * 0.5
        return listOf(
            Vector3(0.0, 0.0, 0.0),
            Vector3(half, 0.0, 0.0),
            Vector3(0.0, half, 0.0),
            Vector3(0.0, 0.0, half),
        )
    }

    private fun solveLinearizedUpdate(
        pose: Pose3D,
        modelPoints: List<Vector3>,
        worldPoints: List<Vector3>,
    ): DoubleArray? {
        if (modelPoints.size != worldPoints.size) return null
        val rotation = pose.rotation
        val rotationMatrix = rotationMatrix(rotation)
        val ata = Array(6) { DoubleArray(6) }
        val atb = DoubleArray(6)

        for (i in modelPoints.indices) {
            val p = modelPoints[i]
            val q = worldPoints[i]
            val predicted = transformPoint(pose, p)
            val b = q - predicted
            val skew = skewMatrix(p)
            val left = multiply3x3(rotationMatrix, skew).map { row -> row.map { -it }.toDoubleArray() }.toTypedArray()

            val rows = arrayOf(
                doubleArrayOf(left[0][0], left[0][1], left[0][2], 1.0, 0.0, 0.0),
                doubleArrayOf(left[1][0], left[1][1], left[1][2], 0.0, 1.0, 0.0),
                doubleArrayOf(left[2][0], left[2][1], left[2][2], 0.0, 0.0, 1.0),
            )
            val residuals = doubleArrayOf(b.x, b.y, b.z)

            for (rowIndex in 0 until 3) {
                val row = rows[rowIndex]
                val residual = residuals[rowIndex]
                for (col in 0 until 6) {
                    atb[col] += row[col] * residual
                    for (col2 in 0 until 6) {
                        ata[col][col2] += row[col] * row[col2]
                    }
                }
            }
        }

        return solveLinearSystem(ata, atb)
    }

    private fun applyDelta(reference: Pose3D, delta: DoubleArray): Pose3D {
        val wx = delta[0]
        val wy = delta[1]
        val wz = delta[2]
        val translation = Vector3(delta[3], delta[4], delta[5])
        val deltaRotation = Quaternion(wx * 0.5, wy * 0.5, wz * 0.5, 1.0).normalized()
        val updatedRotation = reference.rotation * deltaRotation
        return Pose3D(reference.position + translation, updatedRotation)
    }

    private fun computeResidualMm(
        pose: Pose3D,
        modelPoints: List<Vector3>,
        worldPoints: List<Vector3>,
    ): Double {
        if (modelPoints.isEmpty()) return 0.0
        var total = 0.0
        for (i in modelPoints.indices) {
            val predicted = transformPoint(pose, modelPoints[i])
            val error = worldPoints[i] - predicted
            total += error.dot(error)
        }
        val rms = sqrt(total / modelPoints.size)
        return rms * 1000.0
    }

    private fun rotationMatrix(rotation: Quaternion): Array<DoubleArray> {
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

        return arrayOf(
            doubleArrayOf(1 - 2 * (yy + zz), 2 * (xy - wz), 2 * (xz + wy)),
            doubleArrayOf(2 * (xy + wz), 1 - 2 * (xx + zz), 2 * (yz - wx)),
            doubleArrayOf(2 * (xz - wy), 2 * (yz + wx), 1 - 2 * (xx + yy)),
        )
    }

    private fun skewMatrix(point: Vector3): Array<DoubleArray> {
        return arrayOf(
            doubleArrayOf(0.0, -point.z, point.y),
            doubleArrayOf(point.z, 0.0, -point.x),
            doubleArrayOf(-point.y, point.x, 0.0),
        )
    }

    private fun multiply3x3(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        val result = Array(3) { DoubleArray(3) }
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                result[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j]
            }
        }
        return result
    }

    private fun transformPoint(pose: Pose3D, point: Vector3): Vector3 {
        return pose.rotation.rotate(point) + pose.position
    }

    private fun solveLinearSystem(a: Array<DoubleArray>, b: DoubleArray): DoubleArray? {
        val n = b.size
        val augmented = Array(n) { DoubleArray(n + 1) }
        for (i in 0 until n) {
            for (j in 0 until n) {
                augmented[i][j] = a[i][j]
            }
            augmented[i][n] = b[i]
        }

        for (col in 0 until n) {
            var pivot = col
            for (row in col + 1 until n) {
                if (kotlin.math.abs(augmented[row][col]) > kotlin.math.abs(augmented[pivot][col])) {
                    pivot = row
                }
            }
            if (kotlin.math.abs(augmented[pivot][col]) < 1e-9) return null
            if (pivot != col) {
                val temp = augmented[col]
                augmented[col] = augmented[pivot]
                augmented[pivot] = temp
            }
            val pivotValue = augmented[col][col]
            for (j in col until n + 1) {
                augmented[col][j] /= pivotValue
            }
            for (row in 0 until n) {
                if (row == col) continue
                val factor = augmented[row][col]
                for (j in col until n + 1) {
                    augmented[row][j] -= factor * augmented[col][j]
                }
            }
        }

        return DoubleArray(n) { augmented[it][n] }
    }
}
