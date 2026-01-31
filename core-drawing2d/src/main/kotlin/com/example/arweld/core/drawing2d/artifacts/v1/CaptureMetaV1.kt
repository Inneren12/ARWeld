package com.example.arweld.core.drawing2d.artifacts.v1

import com.example.arweld.core.drawing2d.v1.PointV1
import kotlinx.serialization.Serializable

@Serializable
data class CaptureMetaV1(
    val schemaVersion: Int = 1,
    val homography: HomographyV1? = null,
    val corners: CaptureCornersV1? = null,
    val rectified: RectifiedCaptureV1? = null,
    val metrics: CaptureMetricsV1? = null,
)

@Serializable
data class CaptureCornersV1(
    val ordered: CornerQuadV1,
    val refined: CornerQuadV1? = null,
)

@Serializable
data class CornerQuadV1(
    val topLeft: PointV1,
    val topRight: PointV1,
    val bottomRight: PointV1,
    val bottomLeft: PointV1,
)

@Serializable
data class RectifiedCaptureV1(
    val widthPx: Int,
    val heightPx: Int,
)

@Serializable
data class CaptureMetricsV1(
    val blurVariance: Double? = null,
)

@Serializable
data class HomographyV1(
    val m00: Double,
    val m01: Double,
    val m02: Double,
    val m10: Double,
    val m11: Double,
    val m12: Double,
    val m20: Double,
    val m21: Double,
    val m22: Double,
)
