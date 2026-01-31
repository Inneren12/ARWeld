package com.example.arweld.feature.drawingimport.preprocess

/**
 * Page detection pipeline stages for v1.
 */
enum class PageDetectStageV1 {
    PREPROCESS,
    EDGES,
    CONTOURS,
    QUAD_SELECT,
    ORDER,
    REFINE,
    LOAD_UPRIGHT,
    RECTIFY_SIZE,
    RECTIFY,
    SAVE,
}

/**
 * Stable failure codes for page detection v1.
 *
 * These codes are part of a stable contract. Do not rename, reorder, or reuse
 * them for different semantics.
 */
enum class PageDetectFailureCodeV1 {
    /** Raw image decode failed (bitmap decode returned null or threw). */
    DECODE_FAILED,
    /** EXIF orientation read failed. */
    EXIF_FAILED,
    /** Edge detection failed. */
    EDGES_FAILED,
    /** Contour extraction produced no usable contours. */
    CONTOURS_EMPTY,
    /** No contours were available for quad selection. */
    PAGE_NOT_FOUND,
    /** No convex quad could be formed from contours. */
    NO_CONVEX_QUAD,
    /** Quad candidates were present but below area threshold. */
    QUAD_TOO_SMALL,
    /** Ordering received a non-quad point list. */
    ORDER_NOT_FOUR_POINTS,
    /** Ordering failed due to degenerate/invalid geometry. */
    ORDER_DEGENERATE,
    /** Corner refinement failed. */
    REFINE_FAILED,
    /** Unknown/unclassified failure. */
    UNKNOWN,
}

data class PageDetectFailureV1(
    val stage: PageDetectStageV1,
    val code: PageDetectFailureCodeV1,
    val debugMessage: String? = null,
)

sealed class PageDetectOutcomeV1<out T> {
    data class Success<T>(val value: T) : PageDetectOutcomeV1<T>()
    data class Failure(val failure: PageDetectFailureV1) : PageDetectOutcomeV1<Nothing>()
}
