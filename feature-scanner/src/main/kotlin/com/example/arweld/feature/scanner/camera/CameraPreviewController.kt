package com.example.arweld.feature.scanner.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraPreviewController(
    private val context: Context,
    private val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
) {

    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun bind(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        analyzer: ImageAnalysis.Analyzer,
    ) {
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(cameraExecutor, analyzer)
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    analysis,
                )
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    fun shutdown() {
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            },
            ContextCompat.getMainExecutor(context),
        )
        cameraExecutor.shutdown()
    }
}
