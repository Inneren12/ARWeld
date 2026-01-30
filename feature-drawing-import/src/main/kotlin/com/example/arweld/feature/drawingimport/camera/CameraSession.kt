package com.example.arweld.feature.drawingimport.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraSession(
    private val context: Context,
    private val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
) {

    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    private var cameraExecutor: ExecutorService? = null
    private var imageCapture: ImageCapture? = null

    fun start(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
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
                    val preview = Preview.Builder()
                        .build()
                        .apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build()

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        analysis,
                        capture,
                    )
                    imageCapture = capture
                } catch (throwable: Throwable) {
                    onError(throwable)
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    suspend fun captureImage(
        outputFile: java.io.File,
        targetRotation: Int? = null,
    ): ImageCapture.OutputFileResults = suspendCancellableCoroutine { continuation ->
        val capture = imageCapture
        if (capture == null) {
            continuation.resumeWithException(IllegalStateException("ImageCapture not ready"))
            return@suspendCancellableCoroutine
        }
        targetRotation?.let { capture.targetRotation = it }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        capture.takePicture(
            outputOptions,
            cameraExecutor ?: Executors.newSingleThreadExecutor().also { cameraExecutor = it },
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    if (continuation.isActive) {
                        continuation.resume(outputFileResults)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(exception)
                    }
                }
            },
        )
        continuation.invokeOnCancellation {
            outputFile.delete()
        }
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
        imageCapture = null
    }
}
