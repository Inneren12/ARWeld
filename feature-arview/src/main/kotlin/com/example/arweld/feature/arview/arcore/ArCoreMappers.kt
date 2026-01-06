package com.example.arweld.feature.arview.arcore

import android.util.Log
import com.example.arweld.core.domain.spatial.CameraIntrinsics
import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import com.example.arweld.feature.arview.BuildConfig
import com.google.ar.core.Camera
import com.google.ar.core.Pose
import java.util.concurrent.atomic.AtomicBoolean

internal fun Camera.toCameraIntrinsics(rotationDegrees: Int? = null): CameraIntrinsics? {
    val intrinsics = runCatching { imageIntrinsics }
        .getOrElse {
            if (INTRINSICS_FAILURE_LOGGED.compareAndSet(false, true)) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "imageIntrinsics unavailable: ${it.message}")
                }
            }
            null
        }
        ?: runCatching { textureIntrinsics }
            .getOrElse { error ->
                if (INTRINSICS_FAILURE_LOGGED.compareAndSet(false, true)) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "textureIntrinsics unavailable: ${error.message}; will retry")
                    }
                }
                null
            }

    if (intrinsics == null) {
        if (INTRINSICS_FAILURE_LOGGED.compareAndSet(false, true)) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Camera intrinsics null; retrying on next frame")
            }
        }
        return null
    }

    val focalLength = intrinsics.focalLength
    val principalPoint = intrinsics.principalPoint
    val dimensions = intrinsics.imageDimensions
    val mapped = CameraIntrinsics(
        fx = focalLength[0].toDouble(),
        fy = focalLength[1].toDouble(),
        cx = principalPoint[0].toDouble(),
        cy = principalPoint[1].toDouble(),
        width = dimensions[0],
        height = dimensions[1],
    )

    if (INTRINSICS_LOGGED.compareAndSet(false, true)) {
        val rotation = rotationDegrees?.let { "$it°" } ?: "n/a"
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "Intrinsics fx=${mapped.fx}, fy=${mapped.fy}, cx=${mapped.cx}, cy=${mapped.cy}, " +
                    "size=${mapped.width}x${mapped.height}, rotation=$rotation",
            )
        }
    }

    return mapped
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

internal fun Pose3D.toArCorePose(): Pose {
    return Pose(
        floatArrayOf(
            position.x.toFloat(),
            position.y.toFloat(),
            position.z.toFloat(),
        ),
        floatArrayOf(
            rotation.x.toFloat(),
            rotation.y.toFloat(),
            rotation.z.toFloat(),
            rotation.w.toFloat(),
        ),
    )
}

private const val TAG = "ArCoreMappers"
private val INTRINSICS_LOGGED = AtomicBoolean(false)
private val INTRINSICS_FAILURE_LOGGED = AtomicBoolean(false)
