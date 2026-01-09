package com.example.arweld.feature.arview.arcore

import com.example.arweld.core.domain.diagnostics.ArTelemetrySnapshot
import com.example.arweld.feature.arview.tracking.PerformanceMode
import java.util.ArrayDeque
import kotlin.math.ceil

class ArTelemetryTracker(
    private val maxFrameSamples: Int = DEFAULT_FRAME_SAMPLES,
    private val maxCvSamples: Int = DEFAULT_CV_SAMPLES,
) {
    private val frameIntervalsNs = ArrayDeque<Long>()
    private val cvLatenciesNs = ArrayDeque<Long>()
    private var lastFps: Double = 0.0

    fun recordFrameIntervalNs(intervalNs: Long) {
        recordSample(frameIntervalsNs, intervalNs, maxFrameSamples)
    }

    fun recordCvLatencyNs(latencyNs: Long) {
        recordSample(cvLatenciesNs, latencyNs, maxCvSamples)
    }

    fun recordRenderFps(fps: Double) {
        lastFps = fps
    }

    fun snapshot(performanceMode: PerformanceMode): ArTelemetrySnapshot {
        return ArTelemetrySnapshot(
            timestampMillis = System.currentTimeMillis(),
            fps = lastFps,
            frameTimeP95Ms = percentileMs(frameIntervalsNs),
            cvLatencyP95Ms = percentileMs(cvLatenciesNs),
            performanceMode = performanceMode.name.lowercase(),
        )
    }

    private fun recordSample(buffer: ArrayDeque<Long>, value: Long, maxSize: Int) {
        if (buffer.size >= maxSize) {
            buffer.removeFirstOrNull()
        }
        buffer.addLast(value)
    }

    private fun percentileMs(values: ArrayDeque<Long>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val index = (ceil(sorted.size * P95_RATIO).toInt() - 1).coerceIn(0, sorted.lastIndex)
        return sorted[index] / NANOS_TO_MS
    }

    private companion object {
        const val DEFAULT_FRAME_SAMPLES = 120
        const val DEFAULT_CV_SAMPLES = 60
        const val NANOS_TO_MS = 1_000_000.0
        const val P95_RATIO = 0.95
    }
}
