package com.example.arweld.feature.arview.arcore

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Placeholder controller for AR rendering surface.
 * Handles lifecycle callbacks and exposes a [SurfaceView] for Compose hosting.
 */
class ARViewController(
    context: Context,
) {

    private val surfaceView: SurfaceView = SurfaceView(context).apply {
        setBackgroundColor(Color.BLACK)
    }
    private val sessionManager = ARCoreSessionManager(context)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun onCreate() {
        Log.d(TAG, "ARViewController onCreate")
    }

    fun onResume() {
        Log.d(TAG, "ARViewController onResume")
        val rotation = currentRotation()
        val error = sessionManager.onResume(
            displayRotation = rotation,
            viewportWidth = surfaceView.width,
            viewportHeight = surfaceView.height,
        )
        _errorMessage.value = error
    }

    fun onPause() {
        Log.d(TAG, "ARViewController onPause")
        sessionManager.onPause()
    }

    fun onDestroy() {
        Log.d(TAG, "ARViewController onDestroy")
        sessionManager.onDestroy()
    }

    fun getView(): View = surfaceView

    private fun currentRotation(): Int {
        val windowManager = surfaceView.context.getSystemService<android.view.WindowManager>()
        val defaultRotation = windowManager?.defaultDisplay?.rotation
        val contextRotation = surfaceView.display?.rotation
        return (contextRotation ?: defaultRotation ?: Surface.ROTATION_0)
    }

    companion object {
        private const val TAG = "ARViewController"
    }
}
