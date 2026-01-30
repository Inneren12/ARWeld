package com.example.arweld.feature.drawingimport.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraSession(
    private val context: Context,
    private val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
) {

    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    private var cameraExecutor: ExecutorService? = null

    fun start(
        lifecycleOwner: LifecycleOwner,
        onError: (Throwable) -> Unit,
    ) {
        val executor = cameraExecutor ?: Executors.newSingleThreadExecutor().also {
            cameraExecutor = it
        }
        cameraProviderFuture.addListener(
            {
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .apply {
                            setAnalyzer(executor) { imageProxy ->
                                imageProxy.close()
                            }
                        }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        analysis,
                    )
                } catch (throwable: Throwable) {
                    onError(throwable)
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    fun stop() {
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            },
            ContextCompat.getMainExecutor(context),
        )
        cameraExecutor?.shutdown()
        cameraExecutor = null
    }
}
