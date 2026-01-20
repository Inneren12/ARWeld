package com.example.arweld.feature.arview.alignment

class DriftMonitor(
    private val windowSize: Int = DEFAULT_WINDOW_SIZE,
    private val degradeThreshold: Float = DEFAULT_DEGRADE_THRESHOLD,
    private val recoverThreshold: Float = DEFAULT_RECOVER_THRESHOLD,
    private val minSamples: Int = DEFAULT_MIN_SAMPLES,
) {
    private val scores = ArrayDeque<Float>(windowSize)
    private var totalScore = 0f
    private var isDegraded = false

    data class DriftState(
        val isDegraded: Boolean,
        val changed: Boolean,
        val averageScore: Float,
    )

    fun update(score: Float): DriftState {
        if (scores.size == windowSize) {
            totalScore -= scores.removeFirst()
        }
        scores.addLast(score)
        totalScore += score

        val average = totalScore / scores.size
        val previous = isDegraded
        if (scores.size >= minSamples) {
            if (!isDegraded && average <= degradeThreshold) {
                isDegraded = true
            } else if (isDegraded && average >= recoverThreshold) {
                isDegraded = false
            }
        }
        return DriftState(isDegraded = isDegraded, changed = previous != isDegraded, averageScore = average)
    }

    fun reset() {
        scores.clear()
        totalScore = 0f
        isDegraded = false
    }

    companion object {
        private const val DEFAULT_WINDOW_SIZE = 20
        private const val DEFAULT_MIN_SAMPLES = 6
        private const val DEFAULT_DEGRADE_THRESHOLD = 0.55f
        private const val DEFAULT_RECOVER_THRESHOLD = 0.65f
    }
}
