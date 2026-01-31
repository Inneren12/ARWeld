package com.example.arweld.feature.drawingimport.ui.util

import com.example.arweld.core.drawing2d.artifacts.v1.CaptureMetaV1
import com.example.arweld.feature.drawingimport.ui.DrawingImportUiState

fun resolvedBlurVariance(
    screenState: DrawingImportUiState,
    captureMeta: CaptureMetaV1?,
): Double? {
    val sessionBlur = (screenState as? DrawingImportUiState.Saved)
        ?.session
        ?.rectifiedQualityMetrics
        ?.blurVariance
    val metaBlur = captureMeta?.metrics?.blurVar
    return sessionBlur ?: metaBlur
}
