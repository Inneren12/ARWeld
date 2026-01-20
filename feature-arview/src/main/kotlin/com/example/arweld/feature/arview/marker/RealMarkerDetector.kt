package com.example.arweld.feature.arview.marker

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import android.view.Surface
import androidx.annotation.VisibleForTesting
import com.example.arweld.feature.arview.BuildConfig
import com.example.arweld.feature.arview.geometry.Point2f
import com.example.arweld.feature.arview.geometry.orderCornersClockwiseFromTopLeft
import com.example.arweld.feature.arview.geometry.toPoint2f
import com.example.arweld.feature.arview.geometry.toPointF
import com.google.android.gms.tasks.Tasks
import com.google.ar.core.Frame
import com.google.ar.core.exceptions.NotYetAvailableException
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Real marker detector backed by ML Kit barcode scanning running on
 * ARCore camera frames.
 */
class RealMarkerDetector(
    private val rotationProvider: () -> Int = { Surface.ROTATION_0 },
    private val minIntervalMs: Long = DEFAULT_MIN_INTERVAL_MS,
) : MarkerDetector {

    private val barcodeScanner by lazy {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_AZTEC,
                )
                .build(),
        )
    }
    private val lastDetectionMs = AtomicLong(0L)

    override fun detectMarkers(frame: Frame): List<DetectedMarker> {
        val now = SystemClock.elapsedRealtime()
        if (now - lastDetectionMs.get() < minIntervalMs) {
            if (BuildConfig.DEBUG) {
                Log.v(TAG, "Skipping detection; throttled to $minIntervalMs ms")
            }
            return emptyList()
        }

        val image = try {
            frame.acquireCameraImage()
        } catch (notReady: NotYetAvailableException) {
            if (BuildConfig.DEBUG) {
                Log.v(TAG, "Camera image not yet available for marker detection")
            }
            return emptyList()
        } catch (error: Exception) {
            Log.w(TAG, "Failed to acquire camera image", error)
            return emptyList()
        }

        try {
            lastDetectionMs.set(now)
            val rotationDegrees = rotationDegreesFromSurface(rotationProvider())
            val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
            val barcodes = Tasks.await(
                barcodeScanner.process(inputImage),
                DETECTION_TIMEOUT_MS,
                TimeUnit.MILLISECONDS,
            )
            if (barcodes.isEmpty()) return emptyList()

            val width = image.width
            val height = image.height

            return barcodes.mapNotNull { barcode ->
                val id = barcode.rawValue ?: barcode.displayValue ?: return@mapNotNull null
                val corners = (barcode.cornerPoints?.takeIf { it.size >= 4 }
                    ?.map { mapToImageSpace(it, width, height, rotationDegrees) }
                    ?: barcode.boundingBox?.let { box ->
                        boundingBoxCorners(box, width, height, rotationDegrees)
                    }) ?: return@mapNotNull null

                val orderedCorners = orderCorners(corners)

                DetectedMarker(
                    id = id,
                    corners = orderedCorners,
                    timestampNs = frame.timestamp,
                )
            }
        } catch (error: Exception) {
            Log.w(TAG, "Marker detection failed", error)
            return emptyList()
        } finally {
            image.close()
        }
    }

    @VisibleForTesting
    internal fun rotationDegreesFromSurface(rotation: Int): Int = when (rotation) {
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 270
        else -> 0
    }

    @VisibleForTesting
    internal fun boundingBoxCorners(
        box: Rect,
        width: Int,
        height: Int,
        rotationDegrees: Int,
    ): List<PointF> {
        val rawCorners = listOf(
            Point(box.left, box.top),
            Point(box.right, box.top),
            Point(box.right, box.bottom),
            Point(box.left, box.bottom),
        )
        return rawCorners.map { mapToImageSpace(it, width, height, rotationDegrees) }
    }

    @VisibleForTesting
    internal fun mapToImageSpace(point: Point, width: Int, height: Int, rotationDegrees: Int): PointF {
        return when (rotationDegrees % 360) {
            90 -> PointF(point.y.toFloat(), (width - point.x).toFloat())
            180 -> PointF((width - point.x).toFloat(), (height - point.y).toFloat())
            270 -> PointF((height - point.y).toFloat(), point.x.toFloat())
            else -> PointF(point.x.toFloat(), point.y.toFloat())
        }
    }

    @VisibleForTesting
    internal fun orderCorners(corners: List<PointF>): List<PointF> {
        if (corners.size < 4) return corners
        // Convert to pure Kotlin Point2f for JVM-compatible geometry operations
        val point2fCorners = corners.map { it.toPoint2f() }
        val orderedPoint2f = orderCornersClockwiseFromTopLeft(point2fCorners)
        // Convert back to PointF for Android API compatibility
        return orderedPoint2f.map { it.toPointF() }
    }

    companion object {
        private const val TAG = "RealMarkerDetector"
        private const val DETECTION_TIMEOUT_MS = 200L
        private const val DEFAULT_MIN_INTERVAL_MS = 80L
    }
}
