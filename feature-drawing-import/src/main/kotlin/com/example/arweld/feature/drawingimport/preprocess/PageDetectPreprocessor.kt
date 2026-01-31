package com.example.arweld.feature.drawingimport.preprocess

import android.graphics.Bitmap
import kotlin.math.floor
import kotlin.math.max

class PageDetectPreprocessor {
    fun preprocess(input: PageDetectInput): PageDetectOutcomeV1<PageDetectFrame> {
        if (input.maxSide <= 0) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.PREPROCESS,
                    code = PageDetectFailureCodeV1.UNKNOWN,
                    debugMessage = "maxSide must be > 0.",
                ),
            )
        }
        val decodeOutcome = SafeBitmapDecodeV1.decodeUprightWithInfo(
            rawFile = input.rawImageFile,
            maxPixels = input.maxPixels,
            maxSide = input.maxSide,
        )
        val decodeResult = when (decodeOutcome) {
            is PageDetectOutcomeV1.Success -> decodeOutcome.value
            is PageDetectOutcomeV1.Failure -> {
                return PageDetectOutcomeV1.Failure(
                    decodeOutcome.failure.copy(stage = PageDetectStageV1.PREPROCESS),
                )
            }
        }
        val rotated = decodeResult.bitmap
        val originalWidth = decodeResult.info.originalWidth
        val originalHeight = decodeResult.info.originalHeight
        val targetSize = try {
            computeTargetSize(rotated.width, rotated.height, input.maxSide)
        } catch (error: Throwable) {
            rotated.recycle()
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.PREPROCESS,
                    code = PageDetectFailureCodeV1.UNKNOWN,
                    debugMessage = error.message,
                ),
            )
        }
        val scaled = if (targetSize.width == rotated.width && targetSize.height == rotated.height) {
            rotated
        } else {
            Bitmap.createScaledBitmap(rotated, targetSize.width, targetSize.height, true)
        }
        if (scaled !== rotated) {
            rotated.recycle()
        }
        val gray = try {
            toGrayscale(scaled)
        } catch (error: Throwable) {
            scaled.recycle()
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.PREPROCESS,
                    code = PageDetectFailureCodeV1.UNKNOWN,
                    debugMessage = error.message,
                ),
            )
        }
        val frame = PageDetectFrame(
            width = scaled.width,
            height = scaled.height,
            gray = gray,
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            downscaleFactor = targetSize.downscaleFactor,
            rotationAppliedDeg = decodeResult.info.rotationAppliedDeg,
        )
        scaled.recycle()
        return PageDetectOutcomeV1.Success(frame)
    }

    internal fun computeTargetSize(
        originalWidth: Int,
        originalHeight: Int,
        maxSide: Int,
    ): TargetSize {
        require(originalWidth > 0 && originalHeight > 0) { "Invalid source size." }
        require(maxSide > 0) { "maxSide must be > 0." }
        val originalMax = max(originalWidth, originalHeight)
        if (originalMax <= maxSide) {
            return TargetSize(
                width = originalWidth,
                height = originalHeight,
                downscaleFactor = 1.0,
            )
        }
        val ratio = maxSide.toDouble() / originalMax.toDouble()
        val targetWidth = floor(originalWidth * ratio).toInt().coerceAtLeast(1)
        val targetHeight = floor(originalHeight * ratio).toInt().coerceAtLeast(1)
        val targetMax = max(targetWidth, targetHeight)
        val downscaleFactor = originalMax.toDouble() / targetMax.toDouble()
        return TargetSize(
            width = targetWidth,
            height = targetHeight,
            downscaleFactor = downscaleFactor,
        )
    }

    private fun toGrayscale(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val gray = ByteArray(width * height)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val luma = (77 * r + 150 * g + 29 * b) shr 8
            gray[i] = luma.toByte()
        }
        return gray
    }

    internal data class TargetSize(
        val width: Int,
        val height: Int,
        val downscaleFactor: Double,
    )
}
