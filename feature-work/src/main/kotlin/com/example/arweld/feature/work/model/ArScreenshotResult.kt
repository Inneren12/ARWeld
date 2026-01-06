package com.example.arweld.feature.work.model

import java.io.Serializable

/**
 * Result payload returned from AR view when a screenshot is captured for QC.
 */
data class ArScreenshotResult(
    val uriString: String,
    val markerIds: List<String> = emptyList(),
    val trackingState: String = "UNKNOWN",
    val alignmentQualityScore: Float = 0f,
    val distanceToMarker: Float? = null,
    val capturedAtMillis: Long = System.currentTimeMillis(),
) : Serializable

const val AR_SCREENSHOT_RESULT_KEY = "ar_screenshot_result"
const val AR_SCREENSHOT_REQUEST_KEY = "ar_screenshot_request"
