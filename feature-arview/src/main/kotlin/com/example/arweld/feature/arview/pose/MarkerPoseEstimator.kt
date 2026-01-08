package com.example.arweld.feature.arview.pose

import android.graphics.PointF
import com.example.arweld.core.domain.spatial.CameraIntrinsics
import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import com.example.arweld.feature.arview.marker.DetectedMarker
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Estimates the marker pose in world coordinates using a planar PnP approach
 * based on a homography decomposition.
 */
class MarkerPoseEstimator {

    /**
     * Estimates T_world_marker from 2D-3D correspondences, camera intrinsics,
     * and the current camera pose (T_world_camera).
     */
    fun estimateMarkerPose(
        intrinsics: CameraIntrinsics,
        marker: DetectedMarker,
        markerSizeMeters: Float,
        cameraPoseWorld: Pose3D,
    ): Pose3D? {
        if (marker.corners.size < 4) return null
        val objectPoints = buildSquarePoints(markerSizeMeters.toDouble())
        val imagePoints = marker.corners.take(4)
        val homography = computeHomography(objectPoints, imagePoints) ?: return null
        val (rotation, translation) = decomposeHomography(intrinsics, homography) ?: return null
        val markerPoseCamera = Pose3D(translation, rotation)
        return cameraPoseWorld.compose(markerPoseCamera)
    }

    private fun buildSquarePoints(sizeMeters: Double): List<Vector3> {
        val half = sizeMeters / 2.0
        // Corners in camera space with Y-down convention (matches image space projection)
        // When facing camera: TL, TR, BR, BL in image coordinates
        return listOf(
            Vector3(-half, -half, 0.0), // top-left (small X, small Y -> small u, small v)
            Vector3(half, -half, 0.0), // top-right (large X, small Y -> large u, small v)
            Vector3(half, half, 0.0), // bottom-right (large X, large Y -> large u, large v)
            Vector3(-half, half, 0.0), // bottom-left (small X, large Y -> small u, large v)
        )
    }

    private fun computeHomography(
        objectPoints: List<Vector3>,
        imagePoints: List<PointF>,
    ): Array<DoubleArray>? {
        val a = Array(8) { DoubleArray(8) }
        val b = DoubleArray(8)
        for (i in 0 until 4) {
            val X = objectPoints[i].x
            val Y = objectPoints[i].y
            val u = imagePoints[i].x.toDouble()
            val v = imagePoints[i].y.toDouble()

            val row1 = i * 2
            val row2 = row1 + 1

            a[row1][0] = -X
            a[row1][1] = -Y
            a[row1][2] = -1.0
            a[row1][6] = u * X
            a[row1][7] = u * Y
            b[row1] = -u

            a[row2][3] = -X
            a[row2][4] = -Y
            a[row2][5] = -1.0
            a[row2][6] = v * X
            a[row2][7] = v * Y
            b[row2] = -v
        }

        val h = solveLinearSystem(a, b) ?: return null
        return arrayOf(
            doubleArrayOf(h[0], h[1], h[2]),
            doubleArrayOf(h[3], h[4], h[5]),
            doubleArrayOf(h[6], h[7], 1.0),
        )
    }

    private fun decomposeHomography(
        intrinsics: CameraIntrinsics,
        homography: Array<DoubleArray>,
    ): Pair<Quaternion, Vector3>? {
        val k = arrayOf(
            doubleArrayOf(intrinsics.fx, 0.0, intrinsics.cx),
            doubleArrayOf(0.0, intrinsics.fy, intrinsics.cy),
            doubleArrayOf(0.0, 0.0, 1.0),
        )
        val kInv = invert3x3(k) ?: return null
        val b = multiply3x3(kInv, homography)
        val b1 = doubleArrayOf(b[0][0], b[1][0], b[2][0])
        val b2 = doubleArrayOf(b[0][1], b[1][1], b[2][1])
        val b3 = doubleArrayOf(b[0][2], b[1][2], b[2][2])

        val scale = 1.0 / vectorNorm(b1)
        var r1 = scaleVector(b1, scale)
        var r2 = scaleVector(b2, scale)

        val dot = r1[0] * r2[0] + r1[1] * r2[1] + r1[2] * r2[2]
        r2 = doubleArrayOf(
            r2[0] - dot * r1[0],
            r2[1] - dot * r1[1],
            r2[2] - dot * r1[2],
        )
        r2 = scaleVector(r2, 1.0 / vectorNorm(r2))
        var r3 = cross(r1, r2)

        val rotationMatrix = arrayOf(
            doubleArrayOf(r1[0], r2[0], r3[0]),
            doubleArrayOf(r1[1], r2[1], r3[1]),
            doubleArrayOf(r1[2], r2[2], r3[2]),
        )

        val det = determinant3x3(rotationMatrix)
        if (det < 0) {
            r1 = scaleVector(r1, -1.0)
            r2 = scaleVector(r2, -1.0)
            r3 = scaleVector(r3, -1.0)
        }

        val rotation = Quaternion.fromRotationMatrix(
            arrayOf(
                doubleArrayOf(r1[0], r2[0], r3[0]),
                doubleArrayOf(r1[1], r2[1], r3[1]),
                doubleArrayOf(r1[2], r2[2], r3[2]),
            ),
        )
        val translation = Vector3(scale * b3[0], scale * b3[1], scale * b3[2])
        return rotation to translation
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
                if (abs(augmented[row][col]) > abs(augmented[pivot][col])) {
                    pivot = row
                }
            }
            if (abs(augmented[pivot][col]) < 1e-9) return null
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

    private fun invert3x3(m: Array<DoubleArray>): Array<DoubleArray>? {
        val det = determinant3x3(m)
        if (abs(det) < 1e-9) return null
        val invDet = 1.0 / det
        val inv = Array(3) { DoubleArray(3) }
        inv[0][0] = (m[1][1] * m[2][2] - m[1][2] * m[2][1]) * invDet
        inv[0][1] = (m[0][2] * m[2][1] - m[0][1] * m[2][2]) * invDet
        inv[0][2] = (m[0][1] * m[1][2] - m[0][2] * m[1][1]) * invDet
        inv[1][0] = (m[1][2] * m[2][0] - m[1][0] * m[2][2]) * invDet
        inv[1][1] = (m[0][0] * m[2][2] - m[0][2] * m[2][0]) * invDet
        inv[1][2] = (m[0][2] * m[1][0] - m[0][0] * m[1][2]) * invDet
        inv[2][0] = (m[1][0] * m[2][1] - m[1][1] * m[2][0]) * invDet
        inv[2][1] = (m[0][1] * m[2][0] - m[0][0] * m[2][1]) * invDet
        inv[2][2] = (m[0][0] * m[1][1] - m[0][1] * m[1][0]) * invDet
        return inv
    }

    private fun determinant3x3(m: Array<DoubleArray>): Double {
        return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) -
            m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) +
            m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0])
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

    private fun vectorNorm(v: DoubleArray): Double = sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])

    private fun scaleVector(v: DoubleArray, scale: Double): DoubleArray =
        doubleArrayOf(v[0] * scale, v[1] * scale, v[2] * scale)

    private fun cross(a: DoubleArray, b: DoubleArray): DoubleArray {
        return doubleArrayOf(
            a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0],
        )
    }
}
