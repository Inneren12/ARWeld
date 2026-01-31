package com.example.arweld.feature.drawingimport.preprocess

import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class RectifiedSizeV1(
    val width: Int,
    val height: Int,
)

data class RectifySizeParamsV1(
    val maxSide: Int,
    val minSide: Int,
    val enforceEven: Boolean,
)

object RectifySizePolicyV1 {
    private const val MAX_ASPECT_RATIO = 4.0
    private const val MIN_DISTANCE_PX = 1.0

    fun compute(
        corners: OrderedCornersV1,
        params: RectifySizeParamsV1,
    ): PageDetectOutcomeV1<RectifiedSizeV1> {
        if (params.maxSide <= 0 || params.minSide <= 0 || params.maxSide < params.minSide) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.RECTIFY_SIZE,
                    code = PageDetectFailureCodeV1.UNKNOWN,
                    debugMessage = "Invalid size bounds. maxSide must be >= minSide and > 0.",
                ),
            )
        }
        val widthRaw = max(
            distance(corners.topLeft, corners.topRight),
            distance(corners.bottomLeft, corners.bottomRight),
        )
        val heightRaw = max(
            distance(corners.topLeft, corners.bottomLeft),
            distance(corners.topRight, corners.bottomRight),
        )
        if (widthRaw <= MIN_DISTANCE_PX || heightRaw <= MIN_DISTANCE_PX) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.RECTIFY_SIZE,
                    code = PageDetectFailureCodeV1.ORDER_DEGENERATE,
                    debugMessage = "Degenerate quad: width/height too small.",
                ),
            )
        }
        val aspectRatio = max(widthRaw, heightRaw) / min(widthRaw, heightRaw)
        if (!aspectRatio.isFinite() || aspectRatio > MAX_ASPECT_RATIO) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.RECTIFY_SIZE,
                    code = PageDetectFailureCodeV1.UNKNOWN,
                    debugMessage = "Aspect ratio exceeds ${MAX_ASPECT_RATIO}:1.",
                ),
            )
        }
        val maxSideRaw = max(widthRaw, heightRaw)
        val minSideRaw = min(widthRaw, heightRaw)
        var scale = 1.0
        if (maxSideRaw > params.maxSide) {
            scale = params.maxSide / maxSideRaw
        }
        if (minSideRaw * scale < params.minSide) {
            scale = params.minSide / minSideRaw
        }
        if (maxSideRaw * scale > params.maxSide + 1e-6) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.RECTIFY_SIZE,
                    code = PageDetectFailureCodeV1.UNKNOWN,
                    debugMessage = "Size constraints cannot be satisfied with aspect ratio preserved.",
                ),
            )
        }
        val scaledWidth = widthRaw * scale
        val scaledHeight = heightRaw * scale
        var roundedWidth = scaledWidth.roundToInt().coerceAtLeast(1)
        var roundedHeight = scaledHeight.roundToInt().coerceAtLeast(1)
        if (params.enforceEven) {
            roundedWidth = enforceEvenWithinBounds(roundedWidth, params)
                ?: return PageDetectOutcomeV1.Failure(
                    PageDetectFailureV1(
                        stage = PageDetectStageV1.RECTIFY_SIZE,
                        code = PageDetectFailureCodeV1.UNKNOWN,
                        debugMessage = "Unable to satisfy even-size constraint for width.",
                    ),
                )
            roundedHeight = enforceEvenWithinBounds(roundedHeight, params)
                ?: return PageDetectOutcomeV1.Failure(
                    PageDetectFailureV1(
                        stage = PageDetectStageV1.RECTIFY_SIZE,
                        code = PageDetectFailureCodeV1.UNKNOWN,
                        debugMessage = "Unable to satisfy even-size constraint for height.",
                    ),
                )
        }
        if (roundedWidth < params.minSide || roundedHeight < params.minSide ||
            roundedWidth > params.maxSide || roundedHeight > params.maxSide
        ) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.RECTIFY_SIZE,
                    code = PageDetectFailureCodeV1.UNKNOWN,
                    debugMessage = "Rounded size violates min/max constraints.",
                ),
            )
        }
        return PageDetectOutcomeV1.Success(RectifiedSizeV1(width = roundedWidth, height = roundedHeight))
    }

    private fun enforceEvenWithinBounds(value: Int, params: RectifySizeParamsV1): Int? {
        if (value % 2 == 0) return value
        val up = value + 1
        val down = value - 1
        return when {
            up <= params.maxSide -> up
            down >= params.minSide -> down
            else -> null
        }
    }

    private fun distance(a: CornerPointV1, b: CornerPointV1): Double {
        return hypot((b.x - a.x), (b.y - a.y))
    }
}
