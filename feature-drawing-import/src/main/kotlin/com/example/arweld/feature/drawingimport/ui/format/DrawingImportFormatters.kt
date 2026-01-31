package com.example.arweld.feature.drawingimport.ui.format

import com.example.arweld.feature.drawingimport.preprocess.ContourV1
import com.example.arweld.feature.drawingimport.preprocess.OrderedCornersV1 as PreprocessOrderedCornersV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFailureV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectStageV1
import com.example.arweld.feature.drawingimport.preprocess.PageQuadCandidate
import com.example.arweld.feature.drawingimport.preprocess.RectifiedSizeV1
import com.example.arweld.feature.drawingimport.preprocess.RefineResultV1
import com.example.arweld.feature.drawingimport.quality.ExposureMetricsV1
import com.example.arweld.feature.drawingimport.quality.QualityGateResultV1
import com.example.arweld.feature.drawingimport.quality.QualityGateV1
import com.example.arweld.feature.drawingimport.quality.SkewMetricsV1
import java.util.Locale

fun formatContourLabel(index: Int, contour: ContourV1): String {
    val area = "%.1f".format(Locale.US, contour.area)
    val bbox = "x=${contour.bbox.x}, y=${contour.bbox.y}, w=${contour.bbox.width}, h=${contour.bbox.height}"
    return "#${index + 1} area=$area • bbox=($bbox)"
}

fun formatQuadLabel(candidate: PageQuadCandidate): String {
    val area = "%.1f".format(Locale.US, candidate.contourArea)
    val score = "%.2f".format(Locale.US, candidate.score)
    val points = candidate.points.joinToString(prefix = "[", postfix = "]") { "(${it.x},${it.y})" }
    return "Quad area=$area • score=$score • points=$points"
}

fun formatRectifiedSizeLabel(size: RectifiedSizeV1): String {
    return "Rectified size=${size.width}x${size.height}"
}

fun formatSkewMetrics(metrics: SkewMetricsV1): String {
    val angleMax = "%.2f".format(Locale.US, metrics.angleMaxAbsDeg)
    val angleMean = "%.2f".format(Locale.US, metrics.angleMeanAbsDeg)
    val keystoneW = "%.3f".format(Locale.US, metrics.keystoneWidthRatio)
    val keystoneH = "%.3f".format(Locale.US, metrics.keystoneHeightRatio)
    val pageFill = "%.3f".format(Locale.US, metrics.pageFillRatio)
    return "Angle dev (max/mean): $angleMax°/$angleMean° • " +
        "Keystone W/H: $keystoneW/$keystoneH • Page fill: $pageFill • Status: ${metrics.status.name}"
}

fun formatBlurVarianceLabel(blurVariance: Double?): String {
    val formatted = blurVariance?.let { value ->
        String.format(Locale.US, "%.2f", value)
    } ?: "—"
    return "Blur (VarLap): $formatted"
}

fun formatExposureMetrics(metrics: ExposureMetricsV1): String {
    val meanY = "%.1f".format(Locale.US, metrics.meanY)
    val clipLow = "%.2f".format(Locale.US, metrics.clipLowPct)
    val clipHigh = "%.2f".format(Locale.US, metrics.clipHighPct)
    return "Mean Y: $meanY • Clipped low: $clipLow% • Clipped high: $clipHigh%"
}

fun formatQualityGateDecision(result: QualityGateResultV1): String {
    return "Quality: ${result.decision.name}"
}

fun formatQualityGateReasons(result: QualityGateResultV1): String {
    if (result.reasons.isEmpty()) return "Reasons: —"
    val formatted = result.reasons.joinToString { code ->
        val hint = QualityGateV1.hintFor(code)
        if (hint == null) {
            code.name
        } else {
            "${code.name} ($hint)"
        }
    }
    return "Reasons: $formatted"
}

fun formatFailureLabel(label: String, failure: PageDetectFailureV1): String {
    val guidance = "Try retake with better lighting and keep the page flat."
    val debugCode = "${failure.stage.name}:${failure.code.name}"
    val debugMessage = failure.debugMessage?.let { " • detail=$it" } ?: ""
    return "$label failed: stage=${failure.stage.name} • code=${failure.code.name} • $guidance • debug=$debugCode$debugMessage"
}

fun formatRectifiedFailureLabel(failure: PageDetectFailureV1): String {
    return formatFailureLabel("Rectified size", failure)
}

fun formatStageLabel(stage: PageDetectStageV1): String {
    return when (stage) {
        PageDetectStageV1.LOAD_UPRIGHT -> "Load upright bitmap"
        PageDetectStageV1.PREPROCESS -> "Preprocess"
        PageDetectStageV1.EDGES -> "Detect edges"
        PageDetectStageV1.CONTOURS -> "Extract contours"
        PageDetectStageV1.QUAD_SELECT -> "Select quad"
        PageDetectStageV1.ORDER -> "Order corners"
        PageDetectStageV1.REFINE -> "Refine corners"
        PageDetectStageV1.RECTIFY_SIZE -> "Rectified size"
        PageDetectStageV1.RECTIFY -> "Rectify bitmap"
        PageDetectStageV1.SAVE -> "Save artifacts"
    }
}

fun formatOrderedCornersLabel(corners: PreprocessOrderedCornersV1): String {
    val points = corners.toList()
        .joinToString(prefix = "[", postfix = "]") { "(${it.x.formatPx()},${it.y.formatPx()})" }
    return "Ordered corners (TL/TR/BR/BL): $points"
}

fun formatRefineLabel(result: RefineResultV1): String {
    val deltas = result.deltasPx.joinToString(prefix = "[", postfix = "]") { it.formatDeltaPx() }
    val status = result.status.name
    return "Refine $status • deltas=$deltas"
}

fun shortenSha(sha256: String, max: Int = 12): String {
    return if (sha256.length <= max) sha256 else sha256.take(max)
}

private fun Double.formatDeltaPx(): String = "%.2fpx".format(Locale.US, this)

private fun Double.formatPx(): String = "%.1f".format(Locale.US, this)
