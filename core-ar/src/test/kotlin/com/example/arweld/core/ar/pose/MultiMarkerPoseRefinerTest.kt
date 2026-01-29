package com.example.arweld.core.ar.pose

import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MultiMarkerPoseRefinerTest {

    private val refiner = MultiMarkerPoseRefiner()

    @Test
    fun refinePose_returnsSeedPoseForConsistentObservations() {
        val cameraPoseWorld = Pose3D.Identity
        val markerPoseCamera = Pose3D(Vector3(1.0, 2.0, 3.0), Quaternion.Identity)
        val observations = listOf(
            MultiMarkerPoseRefiner.MarkerObservation(
                markerId = "marker-1",
                markerPoseCamera = markerPoseCamera,
                markerSizeMeters = 0.2f,
                tMarkerZone = Pose3D.Identity,
            )
        )

        val result = refiner.refinePose(
            cameraPoseWorld = cameraPoseWorld,
            observations = observations,
        )

        assertThat(result).isNotNull()
        val refined = result!!.worldZonePose
        assertThat(refined.position.x).isWithin(1e-6).of(1.0)
        assertThat(refined.position.y).isWithin(1e-6).of(2.0)
        assertThat(refined.position.z).isWithin(1e-6).of(3.0)
        assertThat(refined.rotation).isEqualTo(Quaternion.Identity)
        assertThat(result.residualErrorMm).isWithin(1e-6).of(0.0)
        assertThat(result.usedMarkers).isEqualTo(1)
    }
}
