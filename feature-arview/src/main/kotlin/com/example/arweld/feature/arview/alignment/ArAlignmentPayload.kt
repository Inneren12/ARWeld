package com.example.arweld.feature.arview.alignment

import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import kotlin.math.asin
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sign
import kotlin.math.toDegrees
import kotlinx.serialization.Serializable

@Serializable
data class ArAlignmentPayload(
    val method: String,
    val markerIds: List<Int> = emptyList(),
    val numPoints: Int? = null,
    val alignmentScore: Double? = null,
    val timestamp: Long,
    val worldPosition: Vector3Dto? = null,
    val worldRotationEuler: Vector3Dto? = null,
)

@Serializable
data class Vector3Dto(
    val x: Double,
    val y: Double,
    val z: Double,
)

internal fun Pose3D.toPayloadPosition(): Vector3Dto = Vector3Dto(
    x = position.x,
    y = position.y,
    z = position.z,
)

internal fun Pose3D.toPayloadEuler(): Vector3Dto = rotation.toEulerDegrees()

private fun Quaternion.toEulerDegrees(): Vector3Dto {
    val sinRCosP = 2 * (w * x + y * z)
    val cosRCosP = 1 - 2 * (x * x + y * y)
    val roll = atan2(sinRCosP, cosRCosP)

    val sinP = 2 * (w * y - z * x)
    val pitch = if (abs(sinP) >= 1) {
        kotlin.math.PI / 2 * sign(sinP)
    } else {
        asin(sinP)
    }

    val sinYCosP = 2 * (w * z + x * y)
    val cosYCosP = 1 - 2 * (y * y + z * z)
    val yaw = atan2(sinYCosP, cosYCosP)

    return Vector3Dto(
        x = toDegrees(roll),
        y = toDegrees(pitch),
        z = toDegrees(yaw),
    )
}
