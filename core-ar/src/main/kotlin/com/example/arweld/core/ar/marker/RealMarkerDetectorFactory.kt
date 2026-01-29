package com.example.arweld.core.ar.marker

import com.example.arweld.core.ar.api.MarkerDetectorFactory
class RealMarkerDetectorFactory : MarkerDetectorFactory {
    override fun create(rotationProvider: () -> Int): MarkerDetector {
        return RealMarkerDetector(rotationProvider)
    }
}
