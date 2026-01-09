package com.example.arweld.feature.scanner.camera

class CodeDeduper(
    private val timeProvider: ElapsedRealtimeProvider,
    private val deduplicationIntervalMillis: Long,
) {
    private var lastCode: String? = null
    private var lastTimestamp: Long = 0L

    fun shouldEmit(value: String): Boolean {
        val now = timeProvider.elapsedRealtime()
        val shouldEmit = value != lastCode || now - lastTimestamp > deduplicationIntervalMillis
        if (shouldEmit) {
            lastCode = value
            lastTimestamp = now
        }
        return shouldEmit
    }
}
