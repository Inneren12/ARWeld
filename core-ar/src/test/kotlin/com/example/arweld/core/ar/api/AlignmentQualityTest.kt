package com.example.arweld.core.ar.api

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AlignmentQualityTest {

    @Test
    fun `valid construction with all positive values`() {
        val quality = AlignmentQuality(
            meanPx = 1.5,
            maxPx = 3.2,
            samples = 10
        )

        assertThat(quality.meanPx).isEqualTo(1.5)
        assertThat(quality.maxPx).isEqualTo(3.2)
        assertThat(quality.samples).isEqualTo(10)
    }

    @Test
    fun `valid construction with zero values`() {
        val quality = AlignmentQuality(
            meanPx = 0.0,
            maxPx = 0.0,
            samples = 0
        )

        assertThat(quality.meanPx).isEqualTo(0.0)
        assertThat(quality.maxPx).isEqualTo(0.0)
        assertThat(quality.samples).isEqualTo(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative meanPx throws IllegalArgumentException`() {
        AlignmentQuality(
            meanPx = -0.1,
            maxPx = 1.0,
            samples = 5
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative maxPx throws IllegalArgumentException`() {
        AlignmentQuality(
            meanPx = 1.0,
            maxPx = -0.1,
            samples = 5
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative samples throws IllegalArgumentException`() {
        AlignmentQuality(
            meanPx = 1.0,
            maxPx = 2.0,
            samples = -1
        )
    }

    @Test
    fun `data class equality works correctly`() {
        val quality1 = AlignmentQuality(meanPx = 1.5, maxPx = 3.0, samples = 10)
        val quality2 = AlignmentQuality(meanPx = 1.5, maxPx = 3.0, samples = 10)
        val quality3 = AlignmentQuality(meanPx = 1.5, maxPx = 3.0, samples = 11)

        assertThat(quality1).isEqualTo(quality2)
        assertThat(quality1).isNotEqualTo(quality3)
    }

    @Test
    fun `data class copy works correctly`() {
        val original = AlignmentQuality(meanPx = 1.0, maxPx = 2.0, samples = 5)
        val copied = original.copy(samples = 10)

        assertThat(copied.meanPx).isEqualTo(1.0)
        assertThat(copied.maxPx).isEqualTo(2.0)
        assertThat(copied.samples).isEqualTo(10)
    }
}
