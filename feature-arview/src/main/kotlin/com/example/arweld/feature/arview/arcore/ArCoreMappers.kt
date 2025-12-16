package com.example.arweld.feature.arview.arcore

import com.example.arweld.core.domain.spatial.CameraIntrinsics
import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import com.google.ar.core.Camera
import com.google.ar.core.Pose

internal fun Camera.toCameraIntrinsics(): CameraIntrinsics? {
    return try {
        val intrinsics = imageIntrinsics
        val focalLength = intrinsics.focalLength
        val principalPoint = intrinsics.principalPoint
        val dimensions = intrinsics.imageDimensions
        CameraIntrinsics(
            fx = focalLength[0].toDouble(),
            fy = focalLength[1].toDouble(),
            cx = principalPoint[0].toDouble(),
            cy = principalPoint[1].toDouble(),
            width = dimensions[0],
            height = dimensions[1],
        )
    } catch (error: Exception) {
        null
    }
}

internal fun Pose.toPose3D(): Pose3D {
    val translation = this.translation
    val rotationQuaternion = this.rotationQuaternion
    return Pose3D(
        position = Vector3(translation[0].toDouble(), translation[1].toDouble(), translation[2].toDouble()),
        rotation = Quaternion(
            x = rotationQuaternion[0].toDouble(),
            y = rotationQuaternion[1].toDouble(),
            z = rotationQuaternion[2].toDouble(),
            w = rotationQuaternion[3].toDouble(),
        ).normalized(),
    )
}
