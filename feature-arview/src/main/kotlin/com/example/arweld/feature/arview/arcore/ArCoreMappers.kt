package com.example.arweld.feature.arview.arcore

import android.os.SystemClock
import android.util.Log
import com.example.arweld.core.domain.spatial.CameraIntrinsics
import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import com.example.arweld.feature.arview.BuildConfig
import com.google.ar.core.Camera
import com.google.ar.core.Pose

internal fun Camera.toCameraIntrinsics(rotationDegrees: Int? = null): CameraIntrinsics? {
    val intrinsics = runCatching { imageIntrinsics }
        .getOrElse { error ->
            logIntrinsicsFailure("imageIntrinsics unavailable: ${error.message}")
            null
        }
        ?: runCatching { textureIntrinsics }
            .getOrElse { error ->
                logIntrinsicsFailure("textureIntrinsics unavailable: ${error.message}")
                null
            }

    if (intrinsics == null) {
        logIntrinsicsFailure("Camera intrinsics null; retrying on next frame")
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

    if (mapped.fx <= 0.0 || mapped.fy <= 0.0 || mapped.width <= 0 || mapped.height <= 0) {
        logIntrinsicsFailure(
            "Invalid intrinsics fx=${mapped.fx}, fy=${mapped.fy}, size=${mapped.width}x${mapped.height}",
        )
        return null
    }

    if (shouldLogIntrinsics()) {
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
private val LAST_INTRINSICS_LOG_MS = java.util.concurrent.atomic.AtomicLong(0L)
private const val INTRINSICS_LOG_INTERVAL_MS = 3000L

private fun logIntrinsicsFailure(message: String) {
    if (!BuildConfig.DEBUG) return
    if (shouldLogIntrinsics()) {
        Log.d(TAG, message)
    }
}

private fun shouldLogIntrinsics(): Boolean {
    val now = SystemClock.elapsedRealtime()
    val last = LAST_INTRINSICS_LOG_MS.get()
    if (now - last < INTRINSICS_LOG_INTERVAL_MS) return false
    return LAST_INTRINSICS_LOG_MS.compareAndSet(last, now)
}
