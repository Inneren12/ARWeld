package com.example.arweld.feature.arview.marker

import android.graphics.PointF
import com.example.arweld.feature.arview.BuildConfig
import com.google.ar.core.Frame
import java.util.concurrent.atomic.AtomicReference

/**
 * Debug-only marker detector that emits a deterministic marker when triggered
 * by a UI control. This allows the downstream pose estimation and alignment
 * pipeline to execute without requiring a CV backend in debug builds.
 */
class SimulatedMarkerDetector : MarkerDetector {

    private val pendingCorners: AtomicReference<List<PointF>?> = AtomicReference(null)

    override fun detectMarkers(frame: Frame): List<DetectedMarker> {
        if (!BuildConfig.DEBUG) return emptyList()
        val corners = pendingCorners.getAndSet(null) ?: return emptyList()
        if (corners.size < 4) return emptyList()
        return listOf(
            DetectedMarker(
                id = DEFAULT_MARKER_ID,
                corners = corners.take(4),
            ),
        )
    }

    /**
     * Request a simulated detection on the next processed frame.
     */
    fun triggerSimulatedDetection() {
        if (!BuildConfig.DEBUG) return
        pendingCorners.set(DEFAULT_CORNERS)
    }

    companion object {
        private const val DEFAULT_MARKER_ID = 7
        private val DEFAULT_CORNERS = listOf(
            PointF(320f, 240f),
            PointF(420f, 240f),
            PointF(420f, 340f),
            PointF(320f, 340f),
        )
    }
}
