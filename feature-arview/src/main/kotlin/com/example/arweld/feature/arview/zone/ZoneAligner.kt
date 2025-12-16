package com.example.arweld.feature.arview.zone

import com.example.arweld.core.domain.spatial.Pose3D

/**
 * Computes world-space poses for zones based on detected markers.
 */
class ZoneAligner(
    private val zoneRegistry: ZoneRegistry,
) {

    /**
     * T_world_zone = T_world_marker * T_marker_zone
     */
    fun computeWorldZoneTransform(
        markerPoseWorld: Pose3D,
        markerId: Int,
    ): Pose3D? {
        val zoneTransform = zoneRegistry.get(markerId) ?: return null
        return markerPoseWorld * zoneTransform.tMarkerZone
    }
}
