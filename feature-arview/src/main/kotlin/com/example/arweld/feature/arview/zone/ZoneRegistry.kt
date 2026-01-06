package com.example.arweld.feature.arview.zone

import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.ZoneTransform

/**
 * Provides known marker-to-zone transforms for aligning the AR model.
 */
class ZoneRegistry(
    private val zones: Map<String, ZoneTransform> = DEFAULT_ZONES,
) {

    private val alignedZones: MutableMap<String, Pose3D> = mutableMapOf()

    fun get(markerId: String): ZoneTransform? = zones[markerId]

    @Synchronized
    fun recordAlignment(markerId: String, worldZonePose: Pose3D) {
        alignedZones[markerId] = worldZonePose
    }

    @Synchronized
    fun lastAlignedPose(): Pose3D? = alignedZones.values.lastOrNull()

    companion object {
        private val TEST_ZONE = ZoneTransform(
            markerId = "marker-1",
            tMarkerZone = Pose3D.Identity,
        )

        private val DEFAULT_ZONES: Map<String, ZoneTransform> = mapOf(
            TEST_ZONE.markerId to TEST_ZONE,
        )
    }
}
