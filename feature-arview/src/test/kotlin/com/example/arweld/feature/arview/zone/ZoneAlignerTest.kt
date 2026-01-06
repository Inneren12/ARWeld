package com.example.arweld.feature.arview.zone

import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import com.example.arweld.core.domain.spatial.ZoneTransform
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ZoneAlignerTest {

    @Test
    fun `registry exposes marker size and aligner composes transforms`() {
        val tMarkerZone = Pose3D(
            position = Vector3(0.1, -0.2, 0.3),
            rotation = Quaternion(0.0, 0.0, 0.0, 1.0),
        )
        val registry = ZoneRegistry(
            zones = mapOf(
                "marker-1" to ZoneTransform(
                    markerId = "marker-1",
                    tMarkerZone = tMarkerZone,
                    markerSizeMeters = 0.12f,
                ),
            ),
        )
        val aligner = ZoneAligner(registry)
        val markerPoseWorld = Pose3D(
            position = Vector3(1.0, 0.5, -0.25),
            rotation = Quaternion.Identity,
        )
        val zoneTransform = registry.get("marker-1")

        val worldZonePose = aligner.computeWorldZoneTransform(markerPoseWorld, "marker-1")

        assertThat(worldZonePose).isNotNull()
        assertThat(zoneTransform?.markerSizeMeters).isEqualTo(0.12f)
        assertThat(zoneTransform?.tMarkerZone).isEqualTo(tMarkerZone)
        val expectedWorldPose = markerPoseWorld * tMarkerZone
        assertThat(worldZonePose).isEqualTo(expectedWorldPose)
        assertThat(aligner.lastAlignedPose()).isEqualTo(expectedWorldPose)
    }
}
