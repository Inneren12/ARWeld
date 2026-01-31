package com.example.arweld.feature.drawingimport.preprocess

import org.junit.Assert.assertEquals
import org.junit.Test

class PageDetectParamsV1Test {
    @Test
    fun `effective decode limits honor caller maxSide`() {
        val params = PageDetectParamsV1(
            maxSide = 512,
            maxDecodeSide = 8000,
        )

        val limits = params.effectiveDecodeLimits()

        assertEquals(512, limits.maxSide)
    }

    @Test
    fun `effective decode limits honor stricter decode side`() {
        val params = PageDetectParamsV1(
            maxSide = 1024,
            maxDecodeSide = 512,
        )

        val limits = params.effectiveDecodeLimits()

        assertEquals(512, limits.maxSide)
    }

    @Test
    fun `effective decode limits honor caller maxPixels`() {
        val params = PageDetectParamsV1(
            maxPixels = 1_000_000,
            maxDecodePixels = 5_000_000,
        )

        val limits = params.effectiveDecodeLimits()

        assertEquals(1_000_000, limits.maxPixels)
    }
}
