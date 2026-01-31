package com.example.arweld.feature.drawingimport.preprocess

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import kotlin.math.max

object SafeBitmapDecodeV1 {
    data class DecodeInfo(
        val originalWidth: Int,
        val originalHeight: Int,
        val decodedWidth: Int,
        val decodedHeight: Int,
        val sampleSize: Int,
        val rotationAppliedDeg: Int,
        val maxPixels: Int,
        val maxSide: Int,
    )

    data class DecodeResult(
        val bitmap: Bitmap,
        val info: DecodeInfo,
    )

    fun decodeUpright(
        rawFile: File,
        maxPixels: Int,
        maxSide: Int,
    ): PageDetectOutcomeV1<Bitmap> {
        return when (val result = decodeUprightWithInfo(rawFile, maxPixels, maxSide)) {
            is PageDetectOutcomeV1.Success -> PageDetectOutcomeV1.Success(result.value.bitmap)
            is PageDetectOutcomeV1.Failure -> result
        }
    }

    fun decodeUprightWithInfo(
        rawFile: File,
        maxPixels: Int,
        maxSide: Int,
    ): PageDetectOutcomeV1<DecodeResult> {
        if (maxPixels <= 0 || maxSide <= 0) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.LOAD_UPRIGHT,
                    code = PageDetectFailureCodeV1.UNKNOWN,
                    debugMessage = "maxPixels and maxSide must be > 0.",
                ),
            )
        }
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(rawFile.absolutePath, bounds)
        val originalWidth = bounds.outWidth
        val originalHeight = bounds.outHeight
        if (originalWidth <= 0 || originalHeight <= 0) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.LOAD_UPRIGHT,
                    code = PageDetectFailureCodeV1.DECODE_FAILED,
                    debugMessage = "Unable to read image bounds: $rawFile",
                ),
            )
        }
        val sampleSize = computeSampleSize(originalWidth, originalHeight, maxPixels, maxSide)
        val decodeOptions = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = sampleSize
        }
        val decoded = try {
            BitmapFactory.decodeFile(rawFile.absolutePath, decodeOptions)
        } catch (error: OutOfMemoryError) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.LOAD_UPRIGHT,
                    code = PageDetectFailureCodeV1.OOM_RISK,
                    debugMessage = error.message,
                ),
            )
        } catch (error: Throwable) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.LOAD_UPRIGHT,
                    code = PageDetectFailureCodeV1.DECODE_FAILED,
                    debugMessage = error.message,
                ),
            )
        } ?: return PageDetectOutcomeV1.Failure(
            PageDetectFailureV1(
                stage = PageDetectStageV1.LOAD_UPRIGHT,
                code = PageDetectFailureCodeV1.DECODE_FAILED,
                debugMessage = "Unable to decode image: $rawFile",
            ),
        )
        val exifOrientation = try {
            ExifInterface(rawFile).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED,
            )
        } catch (error: Throwable) {
            decoded.recycle()
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.LOAD_UPRIGHT,
                    code = PageDetectFailureCodeV1.EXIF_FAILED,
                    debugMessage = error.message,
                ),
            )
        }
        val transform = exifTransform(exifOrientation)
        val rotated = try {
            applyTransform(decoded, transform)
        } catch (error: OutOfMemoryError) {
            decoded.recycle()
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.LOAD_UPRIGHT,
                    code = PageDetectFailureCodeV1.OOM_RISK,
                    debugMessage = error.message,
                ),
            )
        } catch (error: Throwable) {
            decoded.recycle()
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.LOAD_UPRIGHT,
                    code = PageDetectFailureCodeV1.UNKNOWN,
                    debugMessage = error.message,
                ),
            )
        }
        if (rotated !== decoded) {
            decoded.recycle()
        }
        val decodedWidth = rotated.width
        val decodedHeight = rotated.height
        val decodedPixels = decodedWidth.toLong() * decodedHeight.toLong()
        if (decodedPixels > maxPixels.toLong() || max(decodedWidth, decodedHeight) > maxSide) {
            rotated.recycle()
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.LOAD_UPRIGHT,
                    code = PageDetectFailureCodeV1.INPUT_TOO_LARGE,
                    debugMessage = "Decoded image exceeds limits (${decodedWidth}x${decodedHeight}).",
                ),
            )
        }
        val info = DecodeInfo(
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            decodedWidth = decodedWidth,
            decodedHeight = decodedHeight,
            sampleSize = sampleSize,
            rotationAppliedDeg = transform.rotationDeg,
            maxPixels = maxPixels,
            maxSide = maxSide,
        )
        return PageDetectOutcomeV1.Success(DecodeResult(bitmap = rotated, info = info))
    }

    private fun computeSampleSize(
        width: Int,
        height: Int,
        maxPixels: Int,
        maxSide: Int,
    ): Int {
        var sample = 1
        var targetWidth = width / sample
        var targetHeight = height / sample
        while (targetWidth > maxSide || targetHeight > maxSide || targetWidth.toLong() * targetHeight.toLong() > maxPixels.toLong()) {
            sample *= 2
            targetWidth = width / sample
            targetHeight = height / sample
            if (targetWidth <= 0 || targetHeight <= 0) {
                break
            }
        }
        return sample.coerceAtLeast(1)
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

    private data class ExifTransform(
        val rotationDeg: Int,
        val flipHorizontal: Boolean,
    )
}
