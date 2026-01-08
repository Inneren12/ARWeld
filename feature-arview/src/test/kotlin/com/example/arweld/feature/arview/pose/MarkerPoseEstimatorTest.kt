package com.example.arweld.feature.arview.pose

import android.graphics.PointF
import com.example.arweld.core.domain.spatial.CameraIntrinsics
import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import com.example.arweld.core.domain.spatial.angularDistance
import com.example.arweld.feature.arview.marker.DetectedMarker
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MarkerPoseEstimatorTest {

    private val estimator = MarkerPoseEstimator()

    @Test
    fun estimateMarkerPose_returnsPoseCloseToGroundTruth() {
        val intrinsics = CameraIntrinsics(
            fx = 600.0,
            fy = 600.0,
            cx = 320.0,
            cy = 240.0,
            width = 640,
            height = 480,
        )
        val markerSize = 0.2f
        val translation = Vector3(0.0, 0.0, 1.0)
        val worldCameraPose = Pose3D.Identity

        val detectedMarker = DetectedMarker(
            id = "42",
            corners = buildProjectedCorners(markerSize.toDouble(), translation, intrinsics),
            timestampNs = 0L,
        )

        val pose = estimator.estimateMarkerPose(
            intrinsics = intrinsics,
            marker = detectedMarker,
            markerSizeMeters = markerSize,
            cameraPoseWorld = worldCameraPose,
        )

        assertThat(pose).isNotNull()
        val markerPose = pose!!
        assertThat(markerPose.position.x).isWithin(1e-3).of(translation.x)
        assertThat(markerPose.position.y).isWithin(1e-3).of(translation.y)
        assertThat(markerPose.position.z).isWithin(1e-3).of(translation.z)
        assertThat(markerPose.rotation.angularDistance(Quaternion.Identity)).isAtMost(1e-2)
    }

    private fun buildProjectedCorners(
        markerSize: Double,
        translation: Vector3,
        intrinsics: CameraIntrinsics,
    ): List<PointF> {
        val half = markerSize / 2.0
        val objectPoints = listOf(
            Vector3(-half, half, 0.0),
            Vector3(half, half, 0.0),
            Vector3(half, -half, 0.0),
            Vector3(-half, -half, 0.0),
        )
        return objectPoints.map { point ->
            val cameraPoint = Vector3(
                point.x + translation.x,
                point.y + translation.y,
                point.z + translation.z,
            )
            val u = intrinsics.fx * (cameraPoint.x / cameraPoint.z) + intrinsics.cx
            val v = intrinsics.fy * (cameraPoint.y / cameraPoint.z) + intrinsics.cy
            PointF(u.toFloat(), v.toFloat())
        }
    }
}
