package com.example.arweld.core.ar.pose

import com.example.arweld.core.ar.api.MultiMarkerPoseRefinerFactory
class DefaultMultiMarkerPoseRefinerFactory : MultiMarkerPoseRefinerFactory {
    override fun create(): MultiMarkerPoseRefiner = MultiMarkerPoseRefiner()
}
