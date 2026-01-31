package com.example.arweld.feature.drawingimport.preprocess

/**
 * Tunable parameters for page detection preprocessing and edge detection (v1).
 */
data class PageDetectParamsV1(
    val maxSide: Int = 2048,
    val maxPixels: Int = DrawingImportGuardrailsV1.MAX_DECODE_PIXELS,
    val maxDecodePixels: Int = maxPixels,
    val maxDecodeSide: Int = maxSide,
    val edgeLowThreshold: Int = 60,
    val edgeHighThreshold: Int = 180,
    val refineParams: RefineParamsV1 = RefineParamsV1(
        windowRadiusPx = 6,
        maxIters = 6,
        epsilon = 0.25,
    ),
) {
    /**
     * Decode guardrails: caller limits (maxSide/maxPixels) are always honored, while
     * maxDecodeSide/maxDecodePixels serve as upper caps when set. This prevents unexpected
     * decodes larger than the requested preprocessing size.
     */
    fun effectiveDecodeLimits(): DecodeLimits {
        val effectiveMaxSide = when {
            maxDecodeSide <= 0 -> maxSide
            else -> minOf(maxSide, maxDecodeSide)
        }
        val effectiveMaxPixels = when {
            maxDecodePixels <= 0 -> maxPixels
            else -> minOf(maxPixels, maxDecodePixels)
        }
        return DecodeLimits(
            maxPixels = effectiveMaxPixels,
            maxSide = effectiveMaxSide,
        )
    }
}

data class DecodeLimits(
    val maxPixels: Int,
    val maxSide: Int,
)
