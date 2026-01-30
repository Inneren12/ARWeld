package com.example.arweld.feature.arview.ui.arview

import com.example.arweld.core.ar.api.ArCaptureMeta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArCaptureMetaMappingTest {

    @Test
    fun `maps distanceToMarker to float`() {
        val captureMeta = ArCaptureMeta(
            markerIds = listOf("marker-1"),
            trackingState = "TRACKING",
            alignmentQualityScore = 0.85f,
            distanceToMarker = 1.23,
            timestamp = 1234L,
        )

        val result = buildArScreenshotMeta(
            captureMeta = captureMeta,
            fallbackTimestamp = 9999L,
        )

        assertEquals(1.23f, result.distanceToMarker ?: error("distanceToMarker was null"), 0.0001f)
        assertEquals(1234L, result.timestamp)
    }

    @Test
    fun `keeps null distanceToMarker`() {
        val captureMeta = ArCaptureMeta(
            markerIds = emptyList(),
            trackingState = "UNKNOWN",
            alignmentQualityScore = 0f,
            distanceToMarker = null,
            timestamp = 5555L,
        )

        val result = buildArScreenshotMeta(
            captureMeta = captureMeta,
            fallbackTimestamp = 9999L,
        )

        assertNull(result.distanceToMarker)
    }
}
