package com.example.arweld.feature.arview.arcore

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.SurfaceView
import android.view.View

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

    fun onCreate() {
        Log.d(TAG, "ARViewController onCreate")
    }

    fun onResume() {
        Log.d(TAG, "ARViewController onResume")
    }

    fun onPause() {
        Log.d(TAG, "ARViewController onPause")
    }

    fun onDestroy() {
        Log.d(TAG, "ARViewController onDestroy")
    }

    fun getView(): View = surfaceView

    companion object {
        private const val TAG = "ARViewController"
    }
}
