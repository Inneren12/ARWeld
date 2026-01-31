package com.example.arweld.feature.drawingimport.camera

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import java.io.File
import kotlinx.coroutines.flow.StateFlow

sealed interface CameraState {
    data object Idle : CameraState
    data object Ready : CameraState
    data class Error(val message: String) : CameraState
}

sealed interface CameraCaptureOutcome {
    data class Success(val file: File) : CameraCaptureOutcome
    data class Failure(val error: Throwable) : CameraCaptureOutcome
}

interface CameraFacade {
    val state: StateFlow<CameraState>

    fun start(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
    )

    fun stop()

    suspend fun capture(
        outputFile: File,
        targetRotation: Int? = null,
    ): CameraCaptureOutcome
}
