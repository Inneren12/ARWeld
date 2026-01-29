package com.example.arweld.feature.arview.marker

import com.example.arweld.core.ar.marker.DetectedMarker
import com.example.arweld.core.ar.marker.MarkerDetector
import com.google.ar.core.Frame

/**
 * Temporary placeholder implementation that returns no detections.
 *
 * This allows the AR pipeline to be wired end-to-end while keeping the CV
 * backend pluggable. Replace with a real detector (e.g., ArUco) in a later
 * sprint.
 */
class StubMarkerDetector : MarkerDetector {
    override fun detectMarkers(frame: Frame): List<DetectedMarker> = emptyList()
}
