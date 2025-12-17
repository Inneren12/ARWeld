package com.example.arweld.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.arweld.feature.work.camera.PhotoCaptureResult
import com.example.arweld.feature.work.camera.PhotoCaptureService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CameraXPhotoCaptureService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : PhotoCaptureService {

    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    private val mainExecutor = ContextCompat.getMainExecutor(context)
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    @RequiresPermission(Manifest.permission.CAMERA)
    override suspend fun capturePhoto(): PhotoCaptureResult {
        ensureCameraPermission()

        val outputFile = prepareOutputFile()
        val lifecycleOwner = ImmediateLifecycleOwner()
        val cameraProvider = getCameraProvider()
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        withContext(mainDispatcher) {
            lifecycleOwner.start()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                imageCapture,
            )
        }

        return try {
            val outputResults = takePicture(imageCapture, outputFile)
            val savedUri = outputResults.savedUri ?: Uri.fromFile(outputFile)
            val sizeBytes = outputFile.length()
            PhotoCaptureResult(uri = savedUri, sizeBytes = sizeBytes)
        } finally {
            withContext(mainDispatcher) {
                cameraProvider.unbindAll()
                lifecycleOwner.destroy()
            }
        }
    }

    private fun ensureCameraPermission() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
        check(granted) { "Camera permission not granted" }
    }

    private fun prepareOutputFile(): File {
        val evidenceDir = File(context.filesDir, "evidence/photos")
        if (!evidenceDir.exists()) {
            check(evidenceDir.mkdirs()) { "Unable to create evidence directory" }
        }
        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        return File(evidenceDir, fileName)
    }

    private suspend fun getCameraProvider(): ProcessCameraProvider = suspendCancellableCoroutine { continuation ->
        cameraProviderFuture.addListener(
            { continuation.resume(cameraProviderFuture.get()) },
            mainExecutor,
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun takePicture(
        imageCapture: ImageCapture,
        outputFile: File,
    ): ImageCapture.OutputFileResults = suspendCancellableCoroutine { continuation ->
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
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
}

private class ImmediateLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_CREATE)
    }

    override fun getLifecycle(): androidx.lifecycle.Lifecycle = lifecycleRegistry

    fun start() {
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_RESUME)
    }

    fun destroy() {
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
    }
}
