package com.example.arweld.feature.arview.tracking

/**
 * Represents the overall reliability of AR tracking for alignment visuals.
 */
enum class TrackingQuality {
    GOOD,
    WARNING,
    POOR,
}

/**
 * UI-facing tracking status with an optional human-readable reason.
 */
data class TrackingStatus(
    val quality: TrackingQuality,
    val reason: String? = null,
)
