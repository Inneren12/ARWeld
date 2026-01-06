package com.example.arweld.feature.arview.zone

import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import com.example.arweld.core.domain.spatial.ZoneTransform
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ZoneAlignerTest {

    @Test
    fun `computeWorldZoneTransform stores aligned pose`() {
        val registry = ZoneRegistry(
            zones = mapOf(
                "marker-1" to ZoneTransform(
                    markerId = "marker-1",
                    tMarkerZone = Pose3D.Identity,
                ),
            ),
        )
        val aligner = ZoneAligner(registry)
        val markerPoseWorld = Pose3D(
            position = Vector3(1.0, 0.5, -0.25),
            rotation = Quaternion.Identity,
        )

        val worldZonePose = aligner.computeWorldZoneTransform(markerPoseWorld, "marker-1")

        assertThat(worldZonePose).isNotNull()
        assertThat(worldZonePose).isEqualTo(markerPoseWorld)
        assertThat(aligner.lastAlignedPose()).isEqualTo(markerPoseWorld)
    }
}
