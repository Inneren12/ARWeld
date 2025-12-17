package com.example.arweld.feature.work.camera

import android.net.Uri

/**
 * Captures photos from the device camera and stores them locally for QC evidence.
 */
data class PhotoCaptureResult(
    val uri: Uri,
    val sizeBytes: Long,
)

interface PhotoCaptureService {
    suspend fun capturePhoto(): PhotoCaptureResult
}
