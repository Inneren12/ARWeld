package com.example.arweld.feature.arview.tracking

/**
 * Represents the overall reliability of AR tracking for alignment visuals.
 */
enum class TrackingQuality {
    GOOD,
    WARNING,
    POOR,
}

enum class PointCloudStatus {
    OK,
    EMPTY,
    FAILED,
    UNKNOWN,
}

/**
 * UI-facing tracking status with an optional human-readable reason.
 */
data class TrackingStatus(
    val quality: TrackingQuality,
    val reason: String? = null,
    val markerVisibility: Int = 0,
    val driftEstimateMm: Double? = null,
    val performanceMode: PerformanceMode = PerformanceMode.NORMAL,
)
