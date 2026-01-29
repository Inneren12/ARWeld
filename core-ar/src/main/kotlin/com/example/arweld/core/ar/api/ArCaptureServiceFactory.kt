package com.example.arweld.core.ar.api

import android.view.SurfaceView

fun interface ArCaptureServiceFactory {
    fun create(surfaceView: SurfaceView): ArCaptureService
}
