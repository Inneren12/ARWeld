package com.example.arweld.core.ar.pose

import com.example.arweld.core.ar.api.MarkerPoseEstimatorFactory
class DefaultMarkerPoseEstimatorFactory : MarkerPoseEstimatorFactory {
    override fun create(): MarkerPoseEstimator = MarkerPoseEstimator()
}
