package com.example.arweld.core.ar.capture

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.SurfaceView
import androidx.core.net.toUri
import com.example.arweld.core.ar.api.ArCaptureRequest
import com.example.arweld.core.ar.api.ArCaptureResult
import com.example.arweld.core.ar.api.ArCaptureService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class SurfaceViewArCaptureService(
    private val surfaceView: SurfaceView,
) : ArCaptureService {
    private val mainHandler = Handler(Looper.getMainLooper())

    override suspend fun captureScreenshot(request: ArCaptureRequest): ArCaptureResult {
        val bitmap = copySurfaceBitmap()
        val timestamp = System.currentTimeMillis()
        val outputFile = saveBitmap(bitmap, request.workItemId, timestamp)
        return ArCaptureResult(
            fileUri = outputFile.toUri(),
            width = bitmap.width,
            height = bitmap.height,
            timestamp = timestamp,
            meta = request.meta,
        )
    }

    private suspend fun copySurfaceBitmap(): Bitmap {
        val width = surfaceView.width
        val height = surfaceView.height
        check(width > 0 && height > 0) { "SurfaceView not ready for screenshot" }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                PixelCopy.request(surfaceView, bitmap, { result ->
                    if (!continuation.isActive) return@request
                    if (result == PixelCopy.SUCCESS) {
                        continuation.resume(bitmap)
                    } else {
                        continuation.resumeWithException(
                            IllegalStateException("PixelCopy failed with code $result"),
                        )
                    }
                }, mainHandler)
            }
        }
    }

    private suspend fun saveBitmap(
        bitmap: Bitmap,
        workItemId: String,
        timestamp: Long,
    ): File = withContext(Dispatchers.IO) {
        val directory = File(surfaceView.context.filesDir, "evidence/ar_screenshots")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "${workItemId}_${timestamp}.png")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        file
    }
}
