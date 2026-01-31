package com.example.arweld.feature.drawingimport.preprocess

/**
 * Tunable parameters for page detection preprocessing and edge detection (v1).
 */
data class PageDetectParamsV1(
    val maxSide: Int = 2048,
    val maxDecodePixels: Int = DrawingImportGuardrailsV1.MAX_DECODE_PIXELS,
    val maxDecodeSide: Int = DrawingImportGuardrailsV1.MAX_DECODE_SIDE,
    val edgeLowThreshold: Int = 60,
    val edgeHighThreshold: Int = 180,
    val refineParams: RefineParamsV1 = RefineParamsV1(
        windowRadiusPx = 6,
        maxIters = 6,
        epsilon = 0.25,
    ),
)
