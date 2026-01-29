package com.example.arweld.core.ar.alignment

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DriftMonitorTest {
    @Test
    fun `degrades after sustained low scores and recovers after sustained high scores`() {
        val monitor = DriftMonitor()

        val lowScores = List(6) { 0.5f }
        val lowStates = lowScores.map { monitor.update(it) }
        assertThat(lowStates.last().isDegraded).isTrue()
        assertThat(lowStates.last().averageScore).isWithin(0.0001f).of(0.5f)

        val highScores = List(6) { 0.8f }
        val highStates = highScores.map { monitor.update(it) }
        assertThat(highStates.last().isDegraded).isFalse()
        assertThat(highStates.last().averageScore).isWithin(0.0001f).of(0.65f)
    }
}
