package com.example.arweld.feature.arview.marker

import android.graphics.PointF
import com.google.ar.core.Frame

/**
 * Abstraction for marker detection that operates on AR camera frames.
 *
 * Implementations can wrap ArUco, custom fiducials, or other detection
 * pipelines without leaking the underlying CV library to the rest of the app.
 */
interface MarkerDetector {
    /**
     * Detects all visible markers in the provided ARCore [Frame].
     *
     * @param frame An ARCore camera frame with the latest image.
     * @return A list of detected markers with IDs and ordered corners.
     */
    fun detectMarkers(frame: Frame): List<DetectedMarker>
}

/**
 * Marker detection result containing the marker ID and four image-space
 * corners in pixel coordinates.
 *
 * The corner order is **top-left, top-right, bottom-right, bottom-left** to
 * stay consistent for downstream pose estimation.
 */
data class DetectedMarker(
    val id: String,
    val corners: List<PointF>,
    val timestampNs: Long,
)
