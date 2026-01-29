package com.example.arweld.core.ar.api

import com.example.arweld.core.ar.marker.MarkerDetector

fun interface MarkerDetectorFactory {
    fun create(rotationProvider: () -> Int): MarkerDetector
}
