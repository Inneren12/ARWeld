package com.example.arweld.core.ar.api

/**
 * Quantitative measure of AR alignment quality based on reprojection error.
 *
 * Reprojection error is calculated as the L2 (Euclidean) distance in the image plane
 * between the projected 3D model points and the observed 2D feature points (e.g., marker corners).
 * Units are in **pixels** at the camera's native resolution.
 *
 * Lower values indicate better alignment:
 * - **< 2 px**: Excellent alignment, suitable for QC audit
 * - **2-5 px**: Good alignment, acceptable for most use cases
 * - **> 5 px**: Poor alignment, may require recalibration or marker re-detection
 *
 * @property meanPx Mean (average) reprojection error across all sample points, in pixels. Must be >= 0.
 * @property maxPx Maximum reprojection error observed among all sample points, in pixels. Must be >= 0.
 * @property samples Number of sample points used to compute the error statistics. Must be >= 0.
 *
 * @throws IllegalArgumentException if any field violates its constraints.
 */
data class AlignmentQuality(
    val meanPx: Double,
    val maxPx: Double,
    val samples: Int,
) {
    init {
        require(meanPx >= 0.0) { "meanPx must be >= 0, was $meanPx" }
        require(maxPx >= 0.0) { "maxPx must be >= 0, was $maxPx" }
        require(samples >= 0) { "samples must be >= 0, was $samples" }
    }
}
