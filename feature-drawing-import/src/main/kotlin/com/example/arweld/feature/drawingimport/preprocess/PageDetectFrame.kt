package com.example.arweld.feature.drawingimport.preprocess

/**
 * Deterministic preprocessing output for page detection (S2-PR07).
 *
 * Grayscale conversion uses Rec.601 luma approximation:
 *   luma = (77 * R + 150 * G + 29 * B) >> 8
 */
data class PageDetectFrame(
    val width: Int,
    val height: Int,
    val gray: ByteArray,
    val originalWidth: Int,
    val originalHeight: Int,
    val downscaleFactor: Double,
    val rotationAppliedDeg: Int,
)
