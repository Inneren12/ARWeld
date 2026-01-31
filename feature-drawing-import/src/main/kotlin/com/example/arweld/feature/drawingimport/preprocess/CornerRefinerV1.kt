package com.example.arweld.feature.drawingimport.preprocess

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class CornerKey {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT,
}

data class RefineParamsV1(
    val windowRadiusPx: Int,
    val maxIters: Int,
    val epsilon: Double,
)

enum class RefineStatusV1 {
    REFINED,
    SKIPPED,
    FAILED,
}

data class RefineResultV1(
    val corners: OrderedCornersV1,
    val deltasPx: List<Double>,
    val status: RefineStatusV1,
)

object CornerRefinerV1 {
    fun refine(
        frame: PageDetectFrame,
        ordered: OrderedCornersV1,
        params: RefineParamsV1,
    ): PageDetectOutcomeV1<RefineResultV1> {
        if (params.windowRadiusPx <= 0 || params.maxIters <= 0 || params.epsilon <= 0.0) {
            return failure("Invalid refine params.")
        }
        if (frame.width <= 1 || frame.height <= 1 || frame.gray.isEmpty()) {
            return failure("Invalid frame.")
        }
        if (frame.gray.size != frame.width * frame.height) {
            return failure("Invalid frame.")
        }
        return try {
            val edgeDetector = PageDetectEdgeDetector()
            val edgeOutcome = edgeDetector.detect(frame)
            val edgeMap = when (edgeOutcome) {
                is PageDetectOutcomeV1.Success -> edgeOutcome.value
                is PageDetectOutcomeV1.Failure -> return failure(edgeOutcome.failure.debugMessage)
            }
            val corners = listOf(
                ordered.topLeft,
                ordered.topRight,
                ordered.bottomRight,
                ordered.bottomLeft,
            )
            val refined = mutableListOf<CornerPointV1>()
            val deltas = mutableListOf<Double>()
            for (corner in corners) {
                val refinedCorner = refineCorner(edgeMap, corner, params)
                refined.add(refinedCorner)
                deltas.add(distance(corner, refinedCorner))
            }
            if (refined.any { !it.isFinite() }) {
                return failure("Refined corners contain non-finite values.")
            }
            val resultCorners = OrderedCornersV1(
                topLeft = refined[0].clamp(frame.width, frame.height),
                topRight = refined[1].clamp(frame.width, frame.height),
                bottomRight = refined[2].clamp(frame.width, frame.height),
                bottomLeft = refined[3].clamp(frame.width, frame.height),
            )
            PageDetectOutcomeV1.Success(
                RefineResultV1(
                    corners = resultCorners,
                    deltasPx = deltas,
                    status = RefineStatusV1.REFINED,
                ),
            )
        } catch (error: Throwable) {
            failure(error.message)
        }
    }

    private fun failure(message: String?): PageDetectOutcomeV1<RefineResultV1> {
        return PageDetectOutcomeV1.Failure(
            PageDetectFailureV1(
                stage = PageDetectStageV1.REFINE,
                code = PageDetectFailureCodeV1.REFINE_FAILED,
                debugMessage = message,
            )
        )
    }

    private fun refineCorner(
        edgeMap: EdgeMap,
        corner: CornerPointV1,
        params: RefineParamsV1,
    ): CornerPointV1 {
        val width = edgeMap.width
        val height = edgeMap.height
        val start = corner.clamp(width, height)
        var current = start
        var bestScore = scoreAt(edgeMap, current, params.windowRadiusPx)
        for (iter in 0 until params.maxIters) {
            val candidate = localSearch(edgeMap, current, params.windowRadiusPx)
            val candidateScore = scoreAt(edgeMap, candidate, params.windowRadiusPx)
            val move = distance(current, candidate)
            if (candidateScore >= bestScore) {
                current = candidate
                bestScore = candidateScore
            }
            if (move < params.epsilon) {
                break
            }
        }
        return current.clamp(width, height)
    }

    private fun localSearch(
        edgeMap: EdgeMap,
        corner: CornerPointV1,
        windowRadius: Int,
    ): CornerPointV1 {
        val width = edgeMap.width
        val height = edgeMap.height
        val startX = corner.x.roundToInt()
        val startY = corner.y.roundToInt()
        val minX = max(0, startX - windowRadius)
        val maxX = min(width - 1, startX + windowRadius)
        val minY = max(0, startY - windowRadius)
        val maxY = min(height - 1, startY + windowRadius)
        var bestX = startX
        var bestY = startY
        var bestScore = Double.NEGATIVE_INFINITY
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val score = windowScore(edgeMap, x, y, windowRadius)
                if (score > bestScore) {
                    bestScore = score
                    bestX = x
                    bestY = y
                } else if (score == bestScore) {
                    val currentDist = hypot((bestX - startX).toDouble(), (bestY - startY).toDouble())
                    val candidateDist = hypot((x - startX).toDouble(), (y - startY).toDouble())
                    if (candidateDist < currentDist) {
                        bestX = x
                        bestY = y
                    }
                }
            }
        }
        return CornerPointV1(bestX.toDouble(), bestY.toDouble())
    }

    private fun scoreAt(edgeMap: EdgeMap, corner: CornerPointV1, windowRadius: Int): Double {
        val x = corner.x.roundToInt().coerceIn(0, edgeMap.width - 1)
        val y = corner.y.roundToInt().coerceIn(0, edgeMap.height - 1)
        return windowScore(edgeMap, x, y, windowRadius)
    }

    private fun windowScore(edgeMap: EdgeMap, centerX: Int, centerY: Int, radius: Int): Double {
        val width = edgeMap.width
        val height = edgeMap.height
        val minX = max(0, centerX - radius)
        val maxX = min(width - 1, centerX + radius)
        val minY = max(0, centerY - radius)
        val maxY = min(height - 1, centerY + radius)
        var count = 0
        for (y in minY..maxY) {
            val row = y * width
            for (x in minX..maxX) {
                if (edgeMap.edges[row + x].toInt() != 0) {
                    count++
                }
            }
        }
        return count.toDouble()
    }

    private fun distance(a: CornerPointV1, b: CornerPointV1): Double {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return hypot(dx, dy)
    }
}
