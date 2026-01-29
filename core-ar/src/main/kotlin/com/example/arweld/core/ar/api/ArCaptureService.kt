package com.example.arweld.core.ar.api

import android.net.Uri
import android.view.SurfaceView
import com.example.arweld.core.ar.capture.SurfaceViewArCaptureService

data class ArCaptureRequest(
    val workItemId: String,
    val meta: ArCaptureMeta? = null,
)

data class ArCaptureResult(
    val fileUri: Uri,
    val width: Int,
    val height: Int,
    val timestamp: Long,
    val meta: ArCaptureMeta? = null,
)

data class ArCaptureMeta(
    val markerIds: List<String>,
    val trackingState: String,
    val alignmentQualityScore: Float,
    val distanceToMarker: Double?,
    val timestamp: Long,
)

interface ArCaptureService {
    suspend fun captureScreenshot(request: ArCaptureRequest): ArCaptureResult
}

fun createSurfaceViewCaptureService(surfaceView: SurfaceView): ArCaptureService {
    return SurfaceViewArCaptureService(surfaceView)
}
