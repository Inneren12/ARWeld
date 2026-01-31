package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.ScaleInfo
import java.util.Locale

enum class ScaleStatus {
    Missing,
    Invalid,
    Set,
}

data class ScaleStatusDisplay(
    val status: ScaleStatus,
    val mmPerPx: Double? = null,
    val referenceLengthMm: Double? = null,
)

private const val SCALE_DISTANCE_EPSILON = 1e-6
private const val SCALE_MM_PER_PX_DECIMALS = 3
private const val SCALE_LENGTH_MM_DECIMALS = 1

fun deriveScaleStatus(scale: ScaleInfo?): ScaleStatusDisplay {
    if (scale == null) {
        return ScaleStatusDisplay(status = ScaleStatus.Missing)
    }
    if (scale.realLengthMm <= 0.0) {
        return ScaleStatusDisplay(status = ScaleStatus.Invalid)
    }
    val distance = distanceBetween(scale.pointA, scale.pointB)
    if (distance <= SCALE_DISTANCE_EPSILON) {
        return ScaleStatusDisplay(status = ScaleStatus.Invalid)
    }
    return ScaleStatusDisplay(
        status = ScaleStatus.Set,
        mmPerPx = scale.realLengthMm / distance,
        referenceLengthMm = scale.realLengthMm,
    )
}

fun formatScaleMmPerPx(value: Double): String = formatScaleValue(value, SCALE_MM_PER_PX_DECIMALS)

fun formatScaleLengthMm(value: Double): String = formatScaleValue(value, SCALE_LENGTH_MM_DECIMALS)

fun formatScaleValue(value: Double, decimals: Int): String {
    return String.format(Locale.US, "%.${decimals}f", value)
}
