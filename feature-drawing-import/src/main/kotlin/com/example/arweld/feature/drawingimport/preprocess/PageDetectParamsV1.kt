package com.example.arweld.feature.drawingimport.preprocess

/**
 * Tunable parameters for page detection preprocessing and edge detection (v1).
 */
data class PageDetectParamsV1(
    val maxSide: Int = 2048,
    val maxPixels: Int = DrawingImportGuardrailsV1.MAX_DECODE_PIXELS,
    val maxDecodeSide: Int = DrawingImportGuardrailsV1.MAX_DECODE_SIDE,
    val maxDecodePixels: Int = DrawingImportGuardrailsV1.MAX_DECODE_PIXELS,
    val edgeLowThreshold: Int = 60,
    val edgeHighThreshold: Int = 180,
    val refineParams: RefineParamsV1 = RefineParamsV1(
        windowRadiusPx = 6,
        maxIters = 6,
        epsilon = 0.25,
    ),
) {
    /**
     * Decode guardrails: caller intent (maxSide/maxPixels) is always honored, while
     * maxDecodeSide/maxDecodePixels remain hard caps. Effective decode limits clamp
     * to the smaller value so callers cannot exceed guardrails by raising maxSide/maxPixels.
     */
    fun effectiveDecodeLimits(): DecodeLimitsV1 {
        require(maxSide > 0) { "maxSide must be > 0." }
        require(maxPixels > 0) { "maxPixels must be > 0." }
        require(maxDecodeSide > 0) { "maxDecodeSide must be > 0." }
        require(maxDecodePixels > 0) { "maxDecodePixels must be > 0." }
        return DecodeLimitsV1(
            decodeMaxSide = minOf(maxSide, maxDecodeSide),
            decodeMaxPixels = minOf(maxPixels, maxDecodePixels),
        )
    }
}

data class DecodeLimitsV1(
    val decodeMaxSide: Int,
    val decodeMaxPixels: Int,
)
