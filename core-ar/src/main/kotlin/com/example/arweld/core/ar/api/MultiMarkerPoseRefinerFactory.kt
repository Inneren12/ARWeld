package com.example.arweld.core.ar.api

import com.example.arweld.core.ar.pose.MultiMarkerPoseRefiner

fun interface MultiMarkerPoseRefinerFactory {
    fun create(): MultiMarkerPoseRefiner
}
