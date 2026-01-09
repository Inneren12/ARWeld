package com.example.arweld.feature.scanner.camera

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

class CameraScannerEngine(
    context: Context,
    private val decoder: BarcodeDecoder,
    private val deduplicationIntervalMillis: Long = 1500L,
) : ScannerEngine {

    private val cameraController = CameraPreviewController(context)

    override fun bind(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onCodeDetected: (String) -> Unit,
    ) {
        val analyzer = BarcodeAnalyzer(
            onCodeDetected = onCodeDetected,
            decoder = decoder,
            deduplicationIntervalMillis = deduplicationIntervalMillis,
        )
        cameraController.bind(previewView, lifecycleOwner, analyzer)
    }

    override fun shutdown() {
        cameraController.shutdown()
    }
}
