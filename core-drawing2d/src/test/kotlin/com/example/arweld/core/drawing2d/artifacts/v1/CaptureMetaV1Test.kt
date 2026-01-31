package com.example.arweld.core.drawing2d.artifacts.v1

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.v1.PointV1
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CaptureMetaV1Test {

    @Test
    fun `capture meta round trips through Drawing2DJson`() {
        val meta = CaptureMetaV1(
            projectId = "project-123",
            raw = ImageInfoV1(widthPx = 4032, heightPx = 3024, rotationAppliedDeg = 0),
            upright = ImageInfoV1(widthPx = 3024, heightPx = 4032, rotationAppliedDeg = 90),
            downscaleFactor = 2.0,
            cornersDownscaledPx = listOf(
                PointV1(0.0, 0.0),
                PointV1(100.0, 0.0),
                PointV1(100.0, 200.0),
                PointV1(0.0, 200.0),
            ),
            cornersUprightPx = listOf(
                PointV1(0.0, 0.0),
                PointV1(200.0, 0.0),
                PointV1(200.0, 400.0),
                PointV1(0.0, 400.0),
            ),
            homographyH = listOf(
                1.0, 0.0, 2.0,
                0.0, 1.0, 3.0,
                0.0, 0.0, 1.0,
            ),
            rectified = ImageInfoV1(widthPx = 1024, heightPx = 768, rotationAppliedDeg = 0),
            metrics = MetricsBlockV1(
                blurVar = 1234.5,
                exposure = ExposureMetricsV1(meanY = 128.0, clipLowPct = 1.2, clipHighPct = 0.8),
                skew = SkewMetricsV1(
                    angleMaxAbsDeg = 2.3,
                    angleMeanAbsDeg = 1.2,
                    keystoneWidthRatio = 1.04,
                    keystoneHeightRatio = 1.02,
                    pageFillRatio = 0.9,
                ),
            ),
            quality = QualityGateBlockV1(
                decision = "PASS",
                reasons = emptyList(),
            ),
        )

        val json = Drawing2DJson.encodeToString(meta)
        val decoded = Drawing2DJson.decodeFromString<CaptureMetaV1>(json)

        assertThat(decoded).isEqualTo(meta)
    }
}
