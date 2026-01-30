package com.example.arweld.core.domain.spatial

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PoseTypesSmokeTest {
    @Test
    fun `Pose3D identity is available from shared spatial types`() {
        assertThat(Pose3D.Identity.position).isEqualTo(Vector3(0.0, 0.0, 0.0))
        assertThat(Pose3D.Identity.rotation).isEqualTo(Quaternion.Identity)
    }
}
