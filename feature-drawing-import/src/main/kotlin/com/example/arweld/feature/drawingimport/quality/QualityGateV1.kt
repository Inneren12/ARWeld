package com.example.arweld.feature.drawingimport.quality


enum class QualityDecisionV1 {
    PASS,
    WARN,
    FAIL,
}

enum class QualityReasonCodeV1 {
    PAGE_NOT_FOUND,
    BLUR_TOO_HIGH,
    BLUR_WARN,
    EXPOSURE_TOO_DARK,
    EXPOSURE_TOO_BRIGHT,
    CLIP_SHADOWS_HIGH,
    CLIP_HIGHLIGHTS_HIGH,
    KEYSTONE_WIDTH_HIGH,
    KEYSTONE_HEIGHT_HIGH,
    ANGLE_DEVIATION_HIGH,
    PAGE_FILL_LOW,
    DEGENERATE_QUAD,
}

data class QualityGateResultV1(
    val decision: QualityDecisionV1,
    val reasons: List<QualityReasonCodeV1>,
)

data class QualityGateParamsV1(
    val blurFailMin: Double,
    val blurWarnMin: Double,
    val meanYLowFail: Double,
    val meanYHighFail: Double,
    val clipLowFailPct: Double,
    val clipHighFailPct: Double,
    val keystoneFailRatio: Double,
    val keystoneWarnRatio: Double,
    val angleDevFailDeg: Double,
    val angleDevWarnDeg: Double,
    val pageFillFailMin: Double,
    val pageFillWarnMin: Double,
) {
    companion object {
        val Default = QualityGateParamsV1(
            blurFailMin = 80.0,
            blurWarnMin = 140.0,
            meanYLowFail = 60.0,
            meanYHighFail = 200.0,
            clipLowFailPct = 18.0,
            clipHighFailPct = 18.0,
            keystoneFailRatio = 1.35,
            keystoneWarnRatio = 1.18,
            angleDevFailDeg = 10.0,
            angleDevWarnDeg = 6.0,
            pageFillFailMin = 0.45,
            pageFillWarnMin = 0.62,
        )
    }
}

object QualityGateV1 {
    private enum class Severity(val rank: Int) {
        FAIL(0),
        WARN(1),
    }

    private data class Reason(val code: QualityReasonCodeV1, val severity: Severity)

    fun evaluate(
        blurVariance: Double?,
        exposure: ExposureMetricsV1,
        skew: SkewMetricsV1,
        params: QualityGateParamsV1 = QualityGateParamsV1.Default,
    ): QualityGateResultV1 {
        val reasons = buildList {
            if (skew.status == MetricStatusV1.DEGENERATE) {
                add(Reason(QualityReasonCodeV1.DEGENERATE_QUAD, Severity.FAIL))
            }

            blurVariance?.let { blur ->
                when {
                    blur < params.blurFailMin -> add(Reason(QualityReasonCodeV1.BLUR_TOO_HIGH, Severity.FAIL))
                    blur < params.blurWarnMin -> add(Reason(QualityReasonCodeV1.BLUR_WARN, Severity.WARN))
                }
            }

            if (exposure.meanY < params.meanYLowFail) {
                add(Reason(QualityReasonCodeV1.EXPOSURE_TOO_DARK, Severity.FAIL))
            }
            if (exposure.meanY > params.meanYHighFail) {
                add(Reason(QualityReasonCodeV1.EXPOSURE_TOO_BRIGHT, Severity.FAIL))
            }
            if (exposure.clipLowPct > params.clipLowFailPct) {
                add(Reason(QualityReasonCodeV1.CLIP_SHADOWS_HIGH, Severity.FAIL))
            }
            if (exposure.clipHighPct > params.clipHighFailPct) {
                add(Reason(QualityReasonCodeV1.CLIP_HIGHLIGHTS_HIGH, Severity.FAIL))
            }

            if (skew.status == MetricStatusV1.OK) {
                addSkewReasons(skew, params)
            }
        }

        val sortedReasons = reasons
            .sortedWith(compareBy<Reason> { it.severity.rank }.thenBy { it.code.name })
            .map { it.code }

        val decision = when {
            reasons.any { it.severity == Severity.FAIL } -> QualityDecisionV1.FAIL
            reasons.any { it.severity == Severity.WARN } -> QualityDecisionV1.WARN
            else -> QualityDecisionV1.PASS
        }

        return QualityGateResultV1(decision = decision, reasons = sortedReasons)
    }

    fun hintFor(code: QualityReasonCodeV1): String? {
        return when (code) {
            QualityReasonCodeV1.PAGE_NOT_FOUND -> "page not found"
            QualityReasonCodeV1.BLUR_TOO_HIGH -> "blurry"
            QualityReasonCodeV1.BLUR_WARN -> "slightly blurry"
            QualityReasonCodeV1.EXPOSURE_TOO_DARK -> "too dark"
            QualityReasonCodeV1.EXPOSURE_TOO_BRIGHT -> "too bright"
            QualityReasonCodeV1.CLIP_SHADOWS_HIGH -> "shadows clipped"
            QualityReasonCodeV1.CLIP_HIGHLIGHTS_HIGH -> "highlights clipped"
            QualityReasonCodeV1.KEYSTONE_WIDTH_HIGH -> "keystone width"
            QualityReasonCodeV1.KEYSTONE_HEIGHT_HIGH -> "keystone height"
            QualityReasonCodeV1.ANGLE_DEVIATION_HIGH -> "angle deviation"
            QualityReasonCodeV1.PAGE_FILL_LOW -> "page fill low"
            QualityReasonCodeV1.DEGENERATE_QUAD -> "degenerate quad"
        }
    }

    private fun MutableList<Reason>.addSkewReasons(skew: SkewMetricsV1, params: QualityGateParamsV1) {
        when {
            skew.keystoneWidthRatio > params.keystoneFailRatio -> {
                add(Reason(QualityReasonCodeV1.KEYSTONE_WIDTH_HIGH, Severity.FAIL))
            }
            skew.keystoneWidthRatio > params.keystoneWarnRatio -> {
                add(Reason(QualityReasonCodeV1.KEYSTONE_WIDTH_HIGH, Severity.WARN))
            }
        }

        when {
            skew.keystoneHeightRatio > params.keystoneFailRatio -> {
                add(Reason(QualityReasonCodeV1.KEYSTONE_HEIGHT_HIGH, Severity.FAIL))
            }
            skew.keystoneHeightRatio > params.keystoneWarnRatio -> {
                add(Reason(QualityReasonCodeV1.KEYSTONE_HEIGHT_HIGH, Severity.WARN))
            }
        }

        when {
            skew.angleMaxAbsDeg > params.angleDevFailDeg -> {
                add(Reason(QualityReasonCodeV1.ANGLE_DEVIATION_HIGH, Severity.FAIL))
            }
            skew.angleMaxAbsDeg > params.angleDevWarnDeg -> {
                add(Reason(QualityReasonCodeV1.ANGLE_DEVIATION_HIGH, Severity.WARN))
            }
        }

        when {
            skew.pageFillRatio < params.pageFillFailMin -> {
                add(Reason(QualityReasonCodeV1.PAGE_FILL_LOW, Severity.FAIL))
            }
            skew.pageFillRatio < params.pageFillWarnMin -> {
                add(Reason(QualityReasonCodeV1.PAGE_FILL_LOW, Severity.WARN))
            }
        }
    }
}
