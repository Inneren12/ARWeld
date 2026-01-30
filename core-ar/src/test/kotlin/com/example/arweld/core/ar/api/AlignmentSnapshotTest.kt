package com.example.arweld.core.ar.api

import com.example.arweld.core.domain.spatial.Vector3
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AlignmentSnapshotTest {

    private val validQuality = AlignmentQuality(meanPx = 1.5, maxPx = 3.0, samples = 10)
    private val validGravity = Vector3(0.0, 0.0, -9.81)

    @Test
    fun `valid construction with all required fields`() {
        val snapshot = AlignmentSnapshot(
            intrinsicsHash = "sha256:abc123",
            reprojection = validQuality,
            gravity = validGravity
        )

        assertThat(snapshot.intrinsicsHash).isEqualTo("sha256:abc123")
        assertThat(snapshot.reprojection).isEqualTo(validQuality)
        assertThat(snapshot.gravity).isEqualTo(validGravity)
    }

    @Test
    fun `schemaVersion defaults to 1`() {
        val snapshot = AlignmentSnapshot(
            intrinsicsHash = "test-hash",
            reprojection = validQuality,
            gravity = validGravity
        )

        assertThat(snapshot.schemaVersion).isEqualTo(1)
        assertThat(snapshot.schemaVersion).isEqualTo(AlignmentSnapshot.SCHEMA_VERSION)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank intrinsicsHash throws IllegalArgumentException`() {
        AlignmentSnapshot(
            intrinsicsHash = "",
            reprojection = validQuality,
            gravity = validGravity
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `whitespace-only intrinsicsHash throws IllegalArgumentException`() {
        AlignmentSnapshot(
            intrinsicsHash = "   ",
            reprojection = validQuality,
            gravity = validGravity
        )
    }

    @Test
    fun `non-blank intrinsicsHash with whitespace is accepted`() {
        val snapshot = AlignmentSnapshot(
            intrinsicsHash = " abc ",
            reprojection = validQuality,
            gravity = validGravity
        )

        assertThat(snapshot.intrinsicsHash).isEqualTo(" abc ")
    }

    @Test
    fun `gravity vector with various orientations is accepted`() {
        // Device lying flat, screen up
        val flat = AlignmentSnapshot(
            intrinsicsHash = "hash1",
            reprojection = validQuality,
            gravity = Vector3(0.0, 0.0, -9.81)
        )
        assertThat(flat.gravity.z).isWithin(0.01).of(-9.81)

        // Device tilted
        val tilted = AlignmentSnapshot(
            intrinsicsHash = "hash2",
            reprojection = validQuality,
            gravity = Vector3(5.0, -3.0, -8.0)
        )
        assertThat(tilted.gravity.x).isEqualTo(5.0)
        assertThat(tilted.gravity.y).isEqualTo(-3.0)
        assertThat(tilted.gravity.z).isEqualTo(-8.0)
    }

    @Test
    fun `data class equality works correctly`() {
        val snapshot1 = AlignmentSnapshot(
            intrinsicsHash = "hash",
            reprojection = validQuality,
            gravity = validGravity
        )
        val snapshot2 = AlignmentSnapshot(
            intrinsicsHash = "hash",
            reprojection = validQuality,
            gravity = validGravity
        )
        val snapshot3 = AlignmentSnapshot(
            intrinsicsHash = "different-hash",
            reprojection = validQuality,
            gravity = validGravity
        )

        assertThat(snapshot1).isEqualTo(snapshot2)
        assertThat(snapshot1).isNotEqualTo(snapshot3)
    }

    @Test
    fun `data class copy preserves schemaVersion`() {
        val original = AlignmentSnapshot(
            intrinsicsHash = "hash",
            reprojection = validQuality,
            gravity = validGravity
        )
        val copied = original.copy(intrinsicsHash = "new-hash")

        assertThat(copied.intrinsicsHash).isEqualTo("new-hash")
        assertThat(copied.schemaVersion).isEqualTo(1)
        assertThat(copied.reprojection).isEqualTo(validQuality)
    }

    @Test
    fun `SCHEMA_VERSION constant is accessible`() {
        assertThat(AlignmentSnapshot.SCHEMA_VERSION).isEqualTo(1)
    }

    @Test
    fun `snapshot requires all three audit fields - compile time guarantee via non-null types`() {
        // This test documents that AlignmentSnapshot REQUIRES all fields at construction.
        // The following would NOT compile if any parameter were made optional:
        // AlignmentSnapshot(intrinsicsHash = "hash") // Compile error - missing reprojection and gravity
        // AlignmentSnapshot(intrinsicsHash = "hash", reprojection = validQuality) // Compile error - missing gravity

        // Valid construction requires all three:
        val snapshot = AlignmentSnapshot(
            intrinsicsHash = "required-hash",
            reprojection = AlignmentQuality(meanPx = 0.5, maxPx = 1.0, samples = 4),
            gravity = Vector3(0.1, -0.2, -9.8)
        )

        // All fields are present and non-null
        assertThat(snapshot.intrinsicsHash).isNotEmpty()
        assertThat(snapshot.reprojection).isNotNull()
        assertThat(snapshot.gravity).isNotNull()
    }
}
