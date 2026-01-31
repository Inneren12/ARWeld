package com.example.arweld.feature.drawingimport.preprocess

object DrawingImportGuardrailsV1 {
    /**
     * Maximum pixel count allowed for a decoded bitmap before preprocessing/rectification.
     * This guards against OOM risks on device.
     */
    const val MAX_DECODE_PIXELS: Int = 20_000_000

    /**
     * Maximum side length (px) allowed for the decoded upright bitmap.
     * Bitmaps larger than this are deterministically downsampled.
     */
    const val MAX_DECODE_SIDE: Int = 8000

    /**
     * Maximum pixel count allowed for the rectified output bitmap.
     * If exceeded, rectification fails with a stable failure code.
     */
    const val MAX_RECTIFIED_PIXELS: Int = 12_000_000

    /**
     * Maximum side length (px) allowed for the rectified output bitmap.
     * The rectified size policy clamps to this value.
     */
    const val MAX_RECTIFIED_SIDE: Int = 4096

    /**
     * Soft pipeline time budget (ms). Exceeding this is logged and can be enforced via a flag.
     */
    const val PIPELINE_MAX_MS: Long = 15_000L
}
