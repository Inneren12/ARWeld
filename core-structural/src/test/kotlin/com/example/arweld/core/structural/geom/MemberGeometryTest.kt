package com.example.arweld.core.structural.geom

import com.example.arweld.core.structural.model.*
import com.example.arweld.core.structural.profiles.ProfileStandard
import com.example.arweld.core.structural.profiles.WShapeSpec
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

class MemberGeometryTest {

    /**
     * Test 1: W member length 3000mm -> world AABB length along member axis is ~3000mm
     */
    @Test
    fun `generateMembersGeometry - W310x39 member 3000mm length produces correct AABB size`() {
        // Create a simple model: two nodes 3000mm apart along Z axis, one W310x39 member
        val model = StructuralModel(
            id = "test-model",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0),
                Node(id = "n2", x = 0.0, y = 0.0, z = 3000.0)
            ),
            members = listOf(
                Member(
                    id = "m1",
                    kind = MemberKind.COLUMN,
                    profile = createW310x39(),
                    nodeStartId = "n1",
                    nodeEndId = "n2"
                )
            )
        )

        val meshes = model.generateMembersGeometry()

        assertThat(meshes).hasSize(1)
        val mesh = meshes[0]

        // Check that the AABB has correct length along the member axis (Z in this case)
        val aabbSize = mesh.aabb.size()
        val length = maxOf(aabbSize.x, aabbSize.y, aabbSize.z)

        // Allow 1mm tolerance for floating point precision
        assertThat(abs(length - 3000f)).isLessThan(1f)
    }

    /**
     * Test 2: Start and end nodes should be contained in world AABB (with tolerance)
     */
    @Test
    fun `generateMembersGeometry - start and end nodes are contained in AABB`() {
        val model = StructuralModel(
            id = "test-model",
            nodes = listOf(
                Node(id = "n1", x = 100.0, y = 200.0, z = 300.0),
                Node(id = "n2", x = 1100.0, y = 1200.0, z = 1300.0)
            ),
            members = listOf(
                Member(
                    id = "m1",
                    kind = MemberKind.BEAM,
                    profile = createW310x39(),
                    nodeStartId = "n1",
                    nodeEndId = "n2"
                )
            )
        )

        val meshes = model.generateMembersGeometry()

        assertThat(meshes).hasSize(1)
        val mesh = meshes[0]

        val start = Vec3(100f, 200f, 300f)
        val end = Vec3(1100f, 1200f, 1300f)

        // Expand AABB slightly for tolerance (profile dimensions)
        val expandedMin = mesh.aabb.min - Vec3(1f, 1f, 1f)
        val expandedMax = mesh.aabb.max + Vec3(1f, 1f, 1f)
        val expandedAabb = Aabb(expandedMin, expandedMax)

        assertThat(expandedAabb.contains(start)).isTrue()
        assertThat(expandedAabb.contains(end)).isTrue()
    }

    /**
     * Test 3: Roll 90° affects bbox orientation for non-square sections
     *
     * For a W-shape (non-square), rotating 90° around the member axis should swap the
     * cross-section dimensions. We verify this by comparing AABBs for roll=0 vs roll=90.
     */
    @Test
    fun `generateMembersGeometry - roll 90deg affects AABB orientation for W section`() {
        // Create two identical members, one with roll=0, one with roll=90
        val modelNoRoll = StructuralModel(
            id = "test-no-roll",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0),
                Node(id = "n2", x = 0.0, y = 0.0, z = 2000.0)
            ),
            members = listOf(
                Member(
                    id = "m1",
                    kind = MemberKind.BEAM,
                    profile = createW310x39(),
                    nodeStartId = "n1",
                    nodeEndId = "n2",
                    orientationMeta = OrientationMeta(rollAngleDeg = 0.0)
                )
            )
        )

        val modelRoll90 = StructuralModel(
            id = "test-roll-90",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0),
                Node(id = "n2", x = 0.0, y = 0.0, z = 2000.0)
            ),
            members = listOf(
                Member(
                    id = "m1",
                    kind = MemberKind.BEAM,
                    profile = createW310x39(),
                    nodeStartId = "n1",
                    nodeEndId = "n2",
                    orientationMeta = OrientationMeta(rollAngleDeg = 90.0)
                )
            )
        )

        val meshesNoRoll = modelNoRoll.generateMembersGeometry()
        val meshesRoll90 = modelRoll90.generateMembersGeometry()

        assertThat(meshesNoRoll).hasSize(1)
        assertThat(meshesRoll90).hasSize(1)

        val aabbNoRoll = meshesNoRoll[0].aabb
        val aabbRoll90 = meshesRoll90[0].aabb

        val sizeNoRoll = aabbNoRoll.size()
        val sizeRoll90 = aabbRoll90.size()

        // Member axis is Z, so Z dimension should be the same
        assertThat(abs(sizeNoRoll.z - sizeRoll90.z)).isLessThan(1f)

        // For W310x39: dMm=307.3 (depth), bfMm=165.1 (flange width)
        // With member along Z:
        // - roll=0: X and Y dimensions correspond to flange width and depth
        // - roll=90: X and Y dimensions should be swapped

        // Sort the transverse dimensions (X, Y) to compare regardless of which is which
        val transverseNoRoll = listOf(sizeNoRoll.x, sizeNoRoll.y).sorted()
        val transverseRoll90 = listOf(sizeRoll90.x, sizeRoll90.y).sorted()

        // The sorted dimensions should be approximately the same (both should span the profile)
        // but the individual X and Y should be different (swapped)
        assertThat(abs(transverseNoRoll[0] - transverseRoll90[0])).isLessThan(10f)
        assertThat(abs(transverseNoRoll[1] - transverseRoll90[1])).isLessThan(10f)

        // More importantly: the unsorted dimensions should be different (indicating rotation)
        val xDiff = abs(sizeNoRoll.x - sizeRoll90.x)
        val yDiff = abs(sizeNoRoll.y - sizeRoll90.y)

        // At least one dimension should be significantly different (indicating rotation happened)
        assertThat(xDiff > 50f || yDiff > 50f).isTrue()
    }

    /**
     * Test 4: Verify mesh has valid indices and vertices
     */
    @Test
    fun `generateMembersGeometry - produces valid mesh with proper indices`() {
        val model = StructuralModel(
            id = "test-model",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0),
                Node(id = "n2", x = 0.0, y = 0.0, z = 1000.0)
            ),
            members = listOf(
                Member(
                    id = "m1",
                    kind = MemberKind.BEAM,
                    profile = createW310x39(),
                    nodeStartId = "n1",
                    nodeEndId = "n2"
                )
            )
        )

        val meshes = model.generateMembersGeometry()

        assertThat(meshes).hasSize(1)
        val mesh = meshes[0]

        // Verify vertices are in groups of 3 (x,y,z)
        assertThat(mesh.vertices.size % 3).isEqualTo(0)

        // Verify we have some vertices
        assertThat(mesh.vertices.size).isGreaterThan(0)

        // Verify all indices are valid (refer to existing vertices)
        val maxIndex = mesh.vertices.size / 3 - 1
        for (idx in mesh.indices) {
            assertThat(idx).isAtLeast(0)
            assertThat(idx).isAtMost(maxIndex)
        }

        // Verify no NaN values in vertices
        for (v in mesh.vertices) {
            assertThat(v.isNaN()).isFalse()
            assertThat(v.isInfinite()).isFalse()
        }
    }

    /**
     * Test 5: Near-zero length members are skipped
     */
    @Test
    fun `generateMembersGeometry - skips members with near-zero length`() {
        val model = StructuralModel(
            id = "test-model",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0),
                Node(id = "n2", x = 0.0, y = 0.0, z = 0.0001)  // Only 0.1mm apart
            ),
            members = listOf(
                Member(
                    id = "m1",
                    kind = MemberKind.BEAM,
                    profile = createW310x39(),
                    nodeStartId = "n1",
                    nodeEndId = "n2"
                )
            )
        )

        val meshes = model.generateMembersGeometry()

        // Should skip this member due to insufficient length
        assertThat(meshes).isEmpty()
    }

    /**
     * Test 6: Multiple members generate multiple meshes
     */
    @Test
    fun `generateMembersGeometry - generates mesh for each valid member`() {
        val model = StructuralModel(
            id = "test-model",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0),
                Node(id = "n2", x = 0.0, y = 0.0, z = 1000.0),
                Node(id = "n3", x = 1000.0, y = 0.0, z = 0.0)
            ),
            members = listOf(
                Member(
                    id = "m1",
                    kind = MemberKind.COLUMN,
                    profile = createW310x39(),
                    nodeStartId = "n1",
                    nodeEndId = "n2"
                ),
                Member(
                    id = "m2",
                    kind = MemberKind.BEAM,
                    profile = createW310x39(),
                    nodeStartId = "n1",
                    nodeEndId = "n3"
                )
            )
        )

        val meshes = model.generateMembersGeometry()

        assertThat(meshes).hasSize(2)
        assertThat(meshes.map { it.memberId }).containsExactly("m1", "m2")
    }

    // Helper: Create W310x39 profile spec
    private fun createW310x39(): WShapeSpec {
        return WShapeSpec(
            designation = "W310x39",
            standard = ProfileStandard.CSA,
            dMm = 307.3,
            bfMm = 165.1,
            twMm = 6.4,
            tfMm = 9.4,
            areaMm2 = 5050.0,
            massKgPerM = 38.9
        )
    }
}
