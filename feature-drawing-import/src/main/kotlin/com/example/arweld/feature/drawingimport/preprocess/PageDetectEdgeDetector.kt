package com.example.arweld.feature.drawingimport.preprocess

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class PageDetectEdgeDetector(
    private val lowThreshold: Int = 60,
    private val highThreshold: Int = 180,
) {
    /**
     * Deterministic edge detector:
     * - Gaussian blur (5x5 kernel, sigma~1, normalized by 159)
     * - Sobel gradients + non-maximum suppression
     * - Fixed double-threshold hysteresis (low/high)
     */
    fun detect(frame: PageDetectFrame): PageDetectOutcomeV1<EdgeMap> {
        if (frame.width <= 0 || frame.height <= 0) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.EDGES,
                    code = PageDetectFailureCodeV1.EDGES_FAILED,
                    debugMessage = "Frame size must be > 0.",
                ),
            )
        }
        if (frame.gray.size != frame.width * frame.height) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.EDGES,
                    code = PageDetectFailureCodeV1.EDGES_FAILED,
                    debugMessage = "Invalid grayscale buffer size.",
                ),
            )
        }
        return try {
            val blurred = gaussianBlur5x5(frame.gray, frame.width, frame.height)
            val gradients = sobelGradients(blurred, frame.width, frame.height)
            val suppressed = nonMaxSuppression(
                gradients.magnitude,
                gradients.gx,
                gradients.gy,
                frame.width,
                frame.height,
            )
            val edges = hysteresis(suppressed, frame.width, frame.height)
            PageDetectOutcomeV1.Success(
                EdgeMap(
                    width = frame.width,
                    height = frame.height,
                    edges = edges,
                ),
            )
        } catch (error: Throwable) {
            PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.EDGES,
                    code = PageDetectFailureCodeV1.EDGES_FAILED,
                    debugMessage = error.message,
                ),
            )
        }
    }

    private fun gaussianBlur5x5(gray: ByteArray, width: Int, height: Int): IntArray {
        val kernel = intArrayOf(
            2, 4, 5, 4, 2,
            4, 9, 12, 9, 4,
            5, 12, 15, 12, 5,
            4, 9, 12, 9, 4,
            2, 4, 5, 4, 2,
        )
        val kernelSum = 159
        val output = IntArray(width * height)
        for (y in 0 until height) {
            val yBase = y * width
            for (x in 0 until width) {
                var acc = 0
                var kIndex = 0
                for (ky in -2..2) {
                    val yClamped = min(height - 1, max(0, y + ky))
                    val rowBase = yClamped * width
                    for (kx in -2..2) {
                        val xClamped = min(width - 1, max(0, x + kx))
                        val pixel = gray[rowBase + xClamped].toInt() and 0xFF
                        acc += pixel * kernel[kIndex]
                        kIndex++
                    }
                }
                output[yBase + x] = acc / kernelSum
            }
        }
        return output
    }

    private data class GradientField(
        val gx: IntArray,
        val gy: IntArray,
        val magnitude: DoubleArray,
    )

    private fun sobelGradients(blurred: IntArray, width: Int, height: Int): GradientField {
        val gx = IntArray(width * height)
        val gy = IntArray(width * height)
        val magnitude = DoubleArray(width * height)
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                val p00 = blurred[(y - 1) * width + (x - 1)]
                val p01 = blurred[(y - 1) * width + x]
                val p02 = blurred[(y - 1) * width + (x + 1)]
                val p10 = blurred[y * width + (x - 1)]
                val p12 = blurred[y * width + (x + 1)]
                val p20 = blurred[(y + 1) * width + (x - 1)]
                val p21 = blurred[(y + 1) * width + x]
                val p22 = blurred[(y + 1) * width + (x + 1)]

                val gxVal = (-p00 + p02) + (-2 * p10 + 2 * p12) + (-p20 + p22)
                val gyVal = (p00 + 2 * p01 + p02) + (-p20 - 2 * p21 - p22)
                gx[idx] = gxVal
                gy[idx] = gyVal
                magnitude[idx] = hypot(gxVal.toDouble(), gyVal.toDouble())
            }
        }
        return GradientField(gx, gy, magnitude)
    }

    private fun nonMaxSuppression(
        magnitude: DoubleArray,
        gx: IntArray,
        gy: IntArray,
        width: Int,
        height: Int,
    ): DoubleArray {
        val suppressed = DoubleArray(width * height)
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                val angle = (atan2(gy[idx].toDouble(), gx[idx].toDouble()) * 180.0 / Math.PI + 180.0) % 180.0
                val mag = magnitude[idx]
                val (neighbor1, neighbor2) = when {
                    angle < 22.5 || angle >= 157.5 -> {
                        magnitude[idx - 1] to magnitude[idx + 1]
                    }
                    angle < 67.5 -> {
                        magnitude[idx - width + 1] to magnitude[idx + width - 1]
                    }
                    angle < 112.5 -> {
                        magnitude[idx - width] to magnitude[idx + width]
                    }
                    else -> {
                        magnitude[idx - width - 1] to magnitude[idx + width + 1]
                    }
                }
                suppressed[idx] = if (mag >= neighbor1 && mag >= neighbor2) mag else 0.0
            }
        }
        return suppressed
    }

    private fun hysteresis(
        suppressed: DoubleArray,
        width: Int,
        height: Int,
    ): ByteArray {
        val size = width * height
        val state = ByteArray(size)
        val stack = IntArray(size)
        var stackSize = 0
        for (i in 0 until size) {
            val value = suppressed[i]
            if (value >= highThreshold) {
                state[i] = 2
                stack[stackSize++] = i
            } else if (value >= lowThreshold) {
                state[i] = 1
            }
        }
        val offsets = intArrayOf(
            -width - 1, -width, -width + 1,
            -1, 1,
            width - 1, width, width + 1,
        )
        while (stackSize > 0) {
            val idx = stack[--stackSize]
            val x = idx % width
            val y = idx / width
            for (offset in offsets) {
                val nIdx = idx + offset
                if (nIdx < 0 || nIdx >= size) continue
                val nx = nIdx % width
                val ny = nIdx / width
                if (abs(nx - x) > 1 || abs(ny - y) > 1) continue
                if (state[nIdx].toInt() == 1) {
                    state[nIdx] = 2
                    stack[stackSize++] = nIdx
                }
            }
        }
        val edges = ByteArray(size)
        for (i in 0 until size) {
            edges[i] = if (state[i].toInt() == 2) 1 else 0
        }
        return edges
    }
}
