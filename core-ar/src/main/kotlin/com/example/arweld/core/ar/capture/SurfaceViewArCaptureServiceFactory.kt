package com.example.arweld.core.ar.capture

import android.view.SurfaceView
import com.example.arweld.core.ar.api.ArCaptureService
import com.example.arweld.core.ar.api.ArCaptureServiceFactory
import com.example.arweld.core.ar.api.createSurfaceViewCaptureService
class SurfaceViewArCaptureServiceFactory : ArCaptureServiceFactory {
    override fun create(surfaceView: SurfaceView): ArCaptureService {
        return createSurfaceViewCaptureService(surfaceView)
    }
}
