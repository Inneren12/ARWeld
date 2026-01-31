package com.example.arweld.feature.drawingimport.camera

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RealCameraFacade(
    context: Context,
) : CameraFacade {
    private val cameraSession = CameraSession(context)
    private val _state = MutableStateFlow<CameraState>(CameraState.Idle)
    override val state: StateFlow<CameraState> = _state.asStateFlow()

    override fun start(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
    ) {
        cameraSession.start(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            onReady = { _state.value = CameraState.Ready },
            onError = { throwable ->
                _state.value = CameraState.Error(
                    throwable.message ?: "Camera failed to start. Please try again.",
                )
            },
        )
    }

    override fun stop() {
        cameraSession.stop()
        _state.value = CameraState.Idle
    }

    override suspend fun capture(
        outputFile: File,
        targetRotation: Int?,
    ): CameraCaptureOutcome {
        return try {
            cameraSession.captureImage(outputFile, targetRotation)
            CameraCaptureOutcome.Success(outputFile)
        } catch (error: Throwable) {
            CameraCaptureOutcome.Failure(error)
        }
    }
}
