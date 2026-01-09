package com.example.arweld.feature.scanner.camera

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeDeduperTest {

    @Test
    fun `emits first value and suppresses duplicates within interval`() {
        val timeProvider = FakeTimeProvider()
        val deduper = CodeDeduper(timeProvider, deduplicationIntervalMillis = 1000L)

        assertTrue(deduper.shouldEmit("CODE-1"))

        timeProvider.advance(500L)
        assertFalse(deduper.shouldEmit("CODE-1"))

        timeProvider.advance(600L)
        assertTrue(deduper.shouldEmit("CODE-1"))
    }

    @Test
    fun `emits immediately when code changes`() {
        val timeProvider = FakeTimeProvider()
        val deduper = CodeDeduper(timeProvider, deduplicationIntervalMillis = 1000L)

        assertTrue(deduper.shouldEmit("CODE-1"))

        timeProvider.advance(100L)
        assertTrue(deduper.shouldEmit("CODE-2"))
    }
}

private class FakeTimeProvider : ElapsedRealtimeProvider {
    private var now: Long = 0L

    override fun elapsedRealtime(): Long = now

    fun advance(millis: Long) {
        now += millis
    }
}
