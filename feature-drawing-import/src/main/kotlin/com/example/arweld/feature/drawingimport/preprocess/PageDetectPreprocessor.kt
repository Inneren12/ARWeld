package com.example.arweld.feature.drawingimport.preprocess

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
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
        val decodeOptions = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = try {
            BitmapFactory.decodeFile(input.rawImageFile.absolutePath, decodeOptions)
        } catch (error: Throwable) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.PREPROCESS,
                    code = PageDetectFailureCodeV1.DECODE_FAILED,
                    debugMessage = error.message,
                ),
            )
        } ?: return PageDetectOutcomeV1.Failure(
            PageDetectFailureV1(
                stage = PageDetectStageV1.PREPROCESS,
                code = PageDetectFailureCodeV1.DECODE_FAILED,
                debugMessage = "Unable to decode image: ${input.rawImageFile}",
            ),
        )
        val originalWidth = decoded.width
        val originalHeight = decoded.height
        val exifOrientation = try {
            readExifOrientation(input.rawImageFile)
        } catch (error: Throwable) {
            decoded.recycle()
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.PREPROCESS,
                    code = PageDetectFailureCodeV1.EXIF_FAILED,
                    debugMessage = error.message,
                ),
            )
        }
        val transform = exifTransform(exifOrientation)
        val rotated = try {
            applyTransform(decoded, transform)
        } catch (error: Throwable) {
            decoded.recycle()
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.PREPROCESS,
                    code = PageDetectFailureCodeV1.UNKNOWN,
                    debugMessage = error.message,
                ),
            )
        }
        if (rotated !== decoded) {
            decoded.recycle()
        }
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
            rotationAppliedDeg = transform.rotationDeg,
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

    private fun readExifOrientation(file: java.io.File): Int {
        return ExifInterface(file).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED,
        )
    }

    private fun applyTransform(source: Bitmap, transform: ExifTransform): Bitmap {
        if (transform.rotationDeg == 0 && !transform.flipHorizontal) {
            return source
        }
        val matrix = Matrix().apply {
            if (transform.rotationDeg != 0) {
                postRotate(transform.rotationDeg.toFloat())
            }
            if (transform.flipHorizontal) {
                postScale(-1f, 1f)
            }
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
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

    private fun exifTransform(orientation: Int): ExifTransform {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> ExifTransform(rotationDeg = 90, flipHorizontal = false)
            ExifInterface.ORIENTATION_ROTATE_180 -> ExifTransform(rotationDeg = 180, flipHorizontal = false)
            ExifInterface.ORIENTATION_ROTATE_270 -> ExifTransform(rotationDeg = 270, flipHorizontal = false)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> ExifTransform(rotationDeg = 0, flipHorizontal = true)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> ExifTransform(rotationDeg = 180, flipHorizontal = true)
            ExifInterface.ORIENTATION_TRANSPOSE -> ExifTransform(rotationDeg = 90, flipHorizontal = true)
            ExifInterface.ORIENTATION_TRANSVERSE -> ExifTransform(rotationDeg = 270, flipHorizontal = true)
            else -> ExifTransform(rotationDeg = 0, flipHorizontal = false)
        }
    }

    internal data class TargetSize(
        val width: Int,
        val height: Int,
        val downscaleFactor: Double,
    )

    private data class ExifTransform(
        val rotationDeg: Int,
        val flipHorizontal: Boolean,
    )
}
