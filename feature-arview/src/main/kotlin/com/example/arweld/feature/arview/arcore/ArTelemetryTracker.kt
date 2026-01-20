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
    private val cvIntervalsNs = ArrayDeque<Long>()
    private var lastFps: Double = 0.0
    private var lastCvTimestampNs: Long? = null
    private var cvSkippedFrames: Int = 0

    fun recordFrameIntervalNs(intervalNs: Long) {
        recordSample(frameIntervalsNs, intervalNs, maxFrameSamples)
    }

    fun recordCvLatencyNs(latencyNs: Long) {
        recordSample(cvLatenciesNs, latencyNs, maxCvSamples)
    }

    fun recordCvFrameTimestampNs(timestampNs: Long) {
        val previous = lastCvTimestampNs
        if (previous != null) {
            val interval = timestampNs - previous
            if (interval > 0) {
                recordSample(cvIntervalsNs, interval, maxCvSamples)
            }
        }
        lastCvTimestampNs = timestampNs
    }

    fun recordCvFrameSkipped() {
        cvSkippedFrames += 1
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
            cvFps = fpsFromIntervals(),
            cvSkippedFrames = cvSkippedFrames,
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

    private fun fpsFromIntervals(): Double {
        if (cvIntervalsNs.isEmpty()) return 0.0
        val averageInterval = cvIntervalsNs.average()
        if (averageInterval <= 0.0) return 0.0
        return NANOS_PER_SECOND / averageInterval
    }

    private companion object {
        const val DEFAULT_FRAME_SAMPLES = 120
        const val DEFAULT_CV_SAMPLES = 60
        const val NANOS_TO_MS = 1_000_000.0
        const val NANOS_PER_SECOND = 1_000_000_000.0
        const val P95_RATIO = 0.95
    }
}
