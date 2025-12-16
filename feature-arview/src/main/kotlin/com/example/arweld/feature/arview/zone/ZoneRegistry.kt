package com.example.arweld.feature.arview.zone

import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.ZoneTransform

/**
 * Provides known marker-to-zone transforms for aligning the AR model.
 */
class ZoneRegistry(
    private val zones: Map<Int, ZoneTransform> = DEFAULT_ZONES,
) {

    fun get(markerId: Int): ZoneTransform? = zones[markerId]

    companion object {
        private val TEST_ZONE = ZoneTransform(
            markerId = 1,
            tMarkerZone = Pose3D.Identity,
        )

        private val DEFAULT_ZONES: Map<Int, ZoneTransform> = mapOf(
            TEST_ZONE.markerId to TEST_ZONE,
        )
    }
}
