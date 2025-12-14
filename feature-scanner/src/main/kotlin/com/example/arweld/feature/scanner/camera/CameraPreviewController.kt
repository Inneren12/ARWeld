package com.example.arweld.feature.scanner.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

/**
 * Lightweight helper that wires CameraX preview to a [PreviewView] and the provided [LifecycleOwner].
 * This class intentionally avoids any decoding logic; it only prepares the live camera feed.
 */
class CameraPreviewController(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null

    fun bindPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onCameraReady: () -> Unit = {},
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider

            val preview = Preview.Builder().build().also { builtPreview ->
                builtPreview.setSurfaceProvider(previewView.surfaceProvider)
            }

            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
            )

            onCameraReady()
        }, executor)
    }

    fun unbind() {
        cameraProvider?.unbindAll()
    }
}
