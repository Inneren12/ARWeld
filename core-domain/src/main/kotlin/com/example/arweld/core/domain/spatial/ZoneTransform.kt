package com.example.arweld.core.domain.spatial

/**
 * Describes how a physical zone is related to its fiducial marker.
 *
 * @property markerId Identifier of the marker that anchors the zone.
 * @property tMarkerZone Transform from marker coordinates into the zone/model coordinate system.
 * @property markerSizeMeters Physical size of the marker's edge (meters).
 */
data class ZoneTransform(
    val markerId: String,
    val tMarkerZone: Pose3D,
    val markerSizeMeters: Float,
)
