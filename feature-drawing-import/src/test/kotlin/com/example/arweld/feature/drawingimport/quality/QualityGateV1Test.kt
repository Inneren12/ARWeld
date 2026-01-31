package com.example.arweld.feature.drawingimport.quality

import org.junit.Assert.assertEquals
import org.junit.Test

class QualityGateV1Test {
    private val baseExposure = ExposureMetricsV1(
        meanY = 120.0,
        clipLowPct = 0.0,
        clipHighPct = 0.0,
    )

    private val baseSkew = SkewMetricsV1(
        angleMaxAbsDeg = 0.5,
        angleMeanAbsDeg = 0.2,
        keystoneWidthRatio = 1.02,
        keystoneHeightRatio = 1.01,
        pageFillRatio = 0.85,
        status = MetricStatusV1.OK,
    )

    @Test
    fun blurBelowFailTriggersFail() {
        val result = QualityGateV1.evaluate(
            blurVariance = 10.0,
            exposure = baseExposure,
            skew = baseSkew,
        )

        assertEquals(QualityDecisionV1.FAIL, result.decision)
        assertEquals(listOf(QualityReasonCodeV1.BLUR_TOO_HIGH), result.reasons)
    }

    @Test
    fun exposureTooBrightTriggersFail() {
        val result = QualityGateV1.evaluate(
            blurVariance = 200.0,
            exposure = baseExposure.copy(meanY = 220.0),
            skew = baseSkew,
        )

        assertEquals(QualityDecisionV1.FAIL, result.decision)
        assertEquals(listOf(QualityReasonCodeV1.EXPOSURE_TOO_BRIGHT), result.reasons)
    }

    @Test
    fun keystoneWarnTriggersWarn() {
        val result = QualityGateV1.evaluate(
            blurVariance = 200.0,
            exposure = baseExposure,
            skew = baseSkew.copy(keystoneWidthRatio = 1.2),
        )

        assertEquals(QualityDecisionV1.WARN, result.decision)
        assertEquals(listOf(QualityReasonCodeV1.KEYSTONE_WIDTH_HIGH), result.reasons)
    }

    @Test
    fun multipleReasonsStayStableBySeverityThenCode() {
        val result = QualityGateV1.evaluate(
            blurVariance = 110.0,
            exposure = baseExposure.copy(meanY = 40.0),
            skew = baseSkew.copy(angleMaxAbsDeg = 7.0),
        )

        assertEquals(QualityDecisionV1.FAIL, result.decision)
        assertEquals(
            listOf(
                QualityReasonCodeV1.EXPOSURE_TOO_DARK,
                QualityReasonCodeV1.ANGLE_DEVIATION_HIGH,
                QualityReasonCodeV1.BLUR_WARN,
            ),
            result.reasons,
        )
    }
}
