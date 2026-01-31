package com.example.arweld.feature.drawingimport.camera

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCameraFacade(
    initialState: CameraState = CameraState.Ready,
    private val fakeBitmapProvider: () -> Bitmap = {
        Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
    },
) : CameraFacade {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<CameraState> = _state.asStateFlow()

    override fun start(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
    ) {
        _state.value = CameraState.Ready
    }

    override fun stop() {
        _state.value = CameraState.Idle
    }

    override suspend fun capture(
        outputFile: File,
        targetRotation: Int?,
    ): CameraCaptureOutcome {
        return try {
            val bitmap = fakeBitmapProvider()
            outputFile.outputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            }
            CameraCaptureOutcome.Success(outputFile)
        } catch (error: Throwable) {
            CameraCaptureOutcome.Failure(error)
        }
    }
}
