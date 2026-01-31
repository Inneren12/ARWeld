package com.example.arweld.core.drawing2d.artifacts.v1

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.v1.PointV1
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CaptureMetaV1Test {

    @Test
    fun `capture meta round trips through Drawing2DJson`() {
        val meta = CaptureMetaV1(
            corners = CaptureCornersV1(
                ordered = CornerQuadV1(
                    topLeft = PointV1(0.0, 0.0),
                    topRight = PointV1(100.0, 0.0),
                    bottomRight = PointV1(100.0, 200.0),
                    bottomLeft = PointV1(0.0, 200.0),
                ),
                refined = CornerQuadV1(
                    topLeft = PointV1(1.0, 1.0),
                    topRight = PointV1(99.0, 2.0),
                    bottomRight = PointV1(98.0, 198.0),
                    bottomLeft = PointV1(2.0, 199.0),
                ),
            ),
            rectified = RectifiedCaptureV1(widthPx = 1024, heightPx = 768),
            metrics = CaptureMetricsV1(blurVariance = 1234.5),
            homography = HomographyV1(
                m00 = 1.0,
                m01 = 0.0,
                m02 = 2.0,
                m10 = 0.0,
                m11 = 1.0,
                m12 = 3.0,
                m20 = 0.0,
                m21 = 0.0,
                m22 = 1.0,
            ),
        )

        val json = Drawing2DJson.encodeToString(meta)
        val decoded = Drawing2DJson.decodeFromString<CaptureMetaV1>(json)

        assertThat(decoded).isEqualTo(meta)
    }
}
