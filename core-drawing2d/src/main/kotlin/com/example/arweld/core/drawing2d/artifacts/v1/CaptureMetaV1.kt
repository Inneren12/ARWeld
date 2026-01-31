package com.example.arweld.core.drawing2d.artifacts.v1

import com.example.arweld.core.drawing2d.v1.PointV1
import kotlinx.serialization.Serializable

@Serializable
data class CaptureMetaV1(
    val schemaVersion: Int = 1,
    val projectId: String,
    val raw: ImageInfoV1,
    val upright: ImageInfoV1,
    val downscaleFactor: Double,
    val cornersDownscaledPx: List<PointV1>,
    val cornersUprightPx: List<PointV1>,
    val homographyH: List<Double>,
    val rectified: ImageInfoV1,
    val metrics: MetricsBlockV1,
    val quality: QualityGateBlockV1,
)

@Serializable
data class ImageInfoV1(
    val widthPx: Int,
    val heightPx: Int,
    val rotationAppliedDeg: Int,
)

@Serializable
data class MetricsBlockV1(
    val blurVar: Double? = null,
    val exposure: ExposureMetricsV1,
    val skew: SkewMetricsV1,
)

@Serializable
data class ExposureMetricsV1(
    val meanY: Double,
    val clipLowPct: Double,
    val clipHighPct: Double,
)

@Serializable
data class SkewMetricsV1(
    val angleMaxAbsDeg: Double,
    val angleMeanAbsDeg: Double,
    val keystoneWidthRatio: Double,
    val keystoneHeightRatio: Double,
    val pageFillRatio: Double,
)

@Serializable
data class QualityGateBlockV1(
    val decision: String,
    val reasons: List<String>,
)
