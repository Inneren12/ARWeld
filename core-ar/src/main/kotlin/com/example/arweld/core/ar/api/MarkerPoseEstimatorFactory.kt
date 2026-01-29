package com.example.arweld.core.ar.api

import com.example.arweld.core.ar.pose.MarkerPoseEstimator

fun interface MarkerPoseEstimatorFactory {
    fun create(): MarkerPoseEstimator
}
