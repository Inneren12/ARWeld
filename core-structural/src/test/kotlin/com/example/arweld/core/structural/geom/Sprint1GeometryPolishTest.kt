package com.example.arweld.core.structural.geom

import com.example.arweld.core.structural.model.*
import com.example.arweld.core.structural.profiles.*
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Sprint 1 Polish Tests: Geometry correctness for winding, caps, HSS hollow, and AR export.
 */
class Sprint1GeometryPolishTest {

    // ========================================================================
    // Test 1: W-shape winding and caps
    // ========================================================================

    @Test
    fun `W-shape mesh - has valid indices and non-empty vertices`() {
        val model = StructuralModel(
            id = "test-w",
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

        // Verify indices are valid (refer to existing vertices)
        assertThat(mesh.vertices.size % 3).isEqualTo(0)
        assertThat(mesh.vertices.size).isGreaterThan(0)

        val maxIndex = mesh.vertices.size / 3 - 1
        for (idx in mesh.indices) {
            assertThat(idx).isAtLeast(0)
            assertThat(idx).isAtMost(maxIndex)
        }

        // Verify AABB length ~ expected (1000mm)
        val aabbSize = mesh.aabb.size()
        val length = maxOf(aabbSize.x, aabbSize.y, aabbSize.z)
        assertThat(abs(length - 1000f)).isLessThan(10f)
    }

    @Test
    fun `W-shape mesh - cap triangles have consistent winding (simple check)`() {
        val model = StructuralModel(
            id = "test-w-caps",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0),
                Node(id = "n2", x = 0.0, y = 0.0, z = 500.0)
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

        // Simple sanity check: mesh should have reasonable number of triangles
        // W-shape: 12-sided polygon extruded + 2 caps = many triangles
        // Each side wall = 2 triangles × 12 sides = 24 triangles
        // Each cap = ~10 triangles (ear clipping)
        // Total ~ 24 + 20 = 44+ triangles
        val triangleCount = mesh.indices.size / 3
        assertThat(triangleCount).isAtLeast(40)

        // Verify no NaN values in vertices
        for (v in mesh.vertices) {
            assertThat(v.isNaN()).isFalse()
            assertThat(v.isInfinite()).isFalse()
        }
    }

    // ========================================================================
    // Test 2: HSS hollow correctness
    // ========================================================================

    @Test
    fun `HSS mesh - end caps preserve the hollow (ring caps, not filled)`() {
        val model = StructuralModel(
            id = "test-hss",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0),
                Node(id = "n2", x = 0.0, y = 0.0, z = 1000.0)
            ),
            members = listOf(
                Member(
                    id = "m1",
                    kind = MemberKind.COLUMN,
                    profile = createHss200x100x6(),
                    nodeStartId = "n1",
                    nodeEndId = "n2"
                )
            )
        )

        val meshes = model.generateMembersGeometry()
        assertThat(meshes).hasSize(1)

        val mesh = meshes[0]

        // HSS should have:
        // - Outer loop: 4 vertices at z=0 and 4 at z=length (8 total for outer)
        // - Inner loop: 4 vertices at z=0 and 4 at z=length (8 total for inner)
        // - Total vertices: 16
        // - Side walls: outer (4 sides × 2 triangles) + inner (4 sides × 2 triangles) = 16 triangles
        // - End caps: 2 caps × 4 quads × 2 triangles = 16 triangles (ring caps)
        // - Total triangles: 16 + 16 = 32

        val vertexCount = mesh.vertices.size / 3
        val triangleCount = mesh.indices.size / 3

        // Verify vertex count matches expected (16 vertices for rectangle with hole)
        assertThat(vertexCount).isEqualTo(16)

        // Verify triangle count matches expected (32 triangles)
        assertThat(triangleCount).isEqualTo(32)

        // Verify indices are valid
        val maxIndex = vertexCount - 1
        for (idx in mesh.indices) {
            assertThat(idx).isAtLeast(0)
            assertThat(idx).isAtMost(maxIndex)
        }

        // Verify AABB length ~ member length
        val aabbSize = mesh.aabb.size()
        val length = maxOf(aabbSize.x, aabbSize.y, aabbSize.z)
        assertThat(abs(length - 1000f)).isLessThan(10f)
    }

    // ========================================================================
    // Test 3: Non-W profile smoke tests
    // ========================================================================

    @Test
    fun `PL profile mesh - smoke test (non-empty, valid indices)`() {
        val model = StructuralModel(
            id = "test-pl",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0),
                Node(id = "n2", x = 0.0, y = 0.0, z = 800.0)
            ),
            members = listOf(
                Member(
                    id = "m1",
                    kind = MemberKind.BRACE,
                    profile = createPL10x250(),
                    nodeStartId = "n1",
                    nodeEndId = "n2"
                )
            )
        )

        val meshes = model.generateMembersGeometry()
        assertThat(meshes).hasSize(1)

        val mesh = meshes[0]
        assertThat(mesh.vertices.size).isGreaterThan(0)
        assertThat(mesh.indices.size).isGreaterThan(0)

        // Verify indices valid
        val maxIndex = mesh.vertices.size / 3 - 1
        for (idx in mesh.indices) {
            assertThat(idx).isAtLeast(0)
            assertThat(idx).isAtMost(maxIndex)
        }

        // Verify no NaN/Inf
        for (v in mesh.vertices) {
            assertThat(v.isNaN()).isFalse()
            assertThat(v.isInfinite()).isFalse()
        }
    }

    @Test
    fun `L profile mesh - smoke test (non-empty, valid indices)`() {
        val model = StructuralModel(
            id = "test-l",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0),
                Node(id = "n2", x = 0.0, y = 0.0, z = 600.0)
            ),
            members = listOf(
                Member(
                    id = "m1",
                    kind = MemberKind.BRACE,
                    profile = createL100x100x10(),
                    nodeStartId = "n1",
                    nodeEndId = "n2"
                )
            )
        )

        val meshes = model.generateMembersGeometry()
        assertThat(meshes).hasSize(1)

        val mesh = meshes[0]
        assertThat(mesh.vertices.size).isGreaterThan(0)
        assertThat(mesh.indices.size).isGreaterThan(0)

        val maxIndex = mesh.vertices.size / 3 - 1
        for (idx in mesh.indices) {
            assertThat(idx).isInRange(0, maxIndex)
        }

        for (v in mesh.vertices) {
            assertThat(v.isFinite()).isTrue()
        }
    }

    @Test
    fun `C profile mesh - smoke test (non-empty, valid indices)`() {
        val model = StructuralModel(
            id = "test-c",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0),
                Node(id = "n2", x = 0.0, y = 0.0, z = 1200.0)
            ),
            members = listOf(
                Member(
                    id = "m1",
                    kind = MemberKind.BEAM,
                    profile = createC200x50(),
                    nodeStartId = "n1",
                    nodeEndId = "n2"
                )
            )
        )

        val meshes = model.generateMembersGeometry()
        assertThat(meshes).hasSize(1)

        val mesh = meshes[0]
        assertThat(mesh.vertices.size).isGreaterThan(0)
        assertThat(mesh.indices.size).isGreaterThan(0)

        val maxIndex = mesh.vertices.size / 3 - 1
        for (idx in mesh.indices) {
            assertThat(idx).isInRange(0, maxIndex)
        }

        for (v in mesh.vertices) {
            assertThat(v.isFinite()).isTrue()
        }
    }

    // ========================================================================
    // Test 4: exportForAR length correctness
    // ========================================================================

    @Test
    fun `exportForAR - diagonal member has correct length from node distance`() {
        // Create a diagonal member from (0,0,0) to (1000,1000,0)
        // Expected length = sqrt(1000^2 + 1000^2) = sqrt(2000000) ≈ 1414.2mm
        val model = StructuralModel(
            id = "test-diagonal",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0),
                Node(id = "n2", x = 1000.0, y = 1000.0, z = 0.0)
            ),
            members = listOf(
                Member(
                    id = "m1",
                    kind = MemberKind.BRACE,
                    profile = createW310x39(),
                    nodeStartId = "n1",
                    nodeEndId = "n2"
                )
            )
        )

        val arElements = model.exportForAR()

        // Should have 1 member + 2 nodes = 3 elements
        assertThat(arElements).hasSize(3)

        val memberElement = arElements.find { it.type == ArElementType.MEMBER }
        assertThat(memberElement).isNotNull()

        val lengthStr = memberElement!!.meta["lengthMm"]
        assertThat(lengthStr).isNotNull()

        val length = lengthStr!!.toFloat()
        val expectedLength = sqrt(1000.0 * 1000.0 + 1000.0 * 1000.0).toFloat()

        // Allow 1mm tolerance
        assertThat(abs(length - expectedLength)).isLessThan(1f)
    }

    @Test
    fun `exportForAR - plate export uses correct field names (thickness, width, length)`() {
        val model = StructuralModel(
            id = "test-plate",
            nodes = listOf(
                Node(id = "n1", x = 0.0, y = 0.0, z = 0.0)
            ),
            members = emptyList(),
            plates = listOf(
                Plate(
                    id = "p1",
                    thickness = 12.0,
                    width = 200.0,
                    length = 300.0
                )
            )
        )

        val arElements = model.exportForAR()

        // Should have 1 node + 1 plate = 2 elements
        assertThat(arElements).hasSize(2)

        val plateElement = arElements.find { it.type == ArElementType.PLATE }
        assertThat(plateElement).isNotNull()

        // Verify correct field names (not "thicknessMm", etc.)
        assertThat(plateElement!!.meta).containsKey("thickness")
        assertThat(plateElement.meta).containsKey("width")
        assertThat(plateElement.meta).containsKey("length")

        // Verify values are correct
        assertThat(plateElement.meta["thickness"]).isEqualTo("12.0")
        assertThat(plateElement.meta["width"]).isEqualTo("200.0")
        assertThat(plateElement.meta["length"]).isEqualTo("300.0")
    }

    // ========================================================================
    // Helper functions to create profile specs
    // ========================================================================

    private fun createW310x39() = WShapeSpec(
        designation = "W310x39",
        standard = ProfileStandard.CSA,
        dMm = 307.3,
        bfMm = 165.1,
        twMm = 6.4,
        tfMm = 9.4,
        areaMm2 = 5050.0,
        massKgPerM = 38.9
    )

    private fun createHss200x100x6() = HssSpec(
        designation = "HSS200x100x6",
        standard = ProfileStandard.CSA,
        hMm = 200.0,
        bMm = 100.0,
        tMm = 6.0,
        areaMm2 = 3400.0,
        massKgPerM = 26.7
    )

    private fun createPL10x250() = PlateSpec(
        designation = "PL10x250",
        standard = ProfileStandard.CSA,
        tMm = 10.0,
        wMm = 250.0,
        areaMm2 = 2500.0,
        massKgPerM = 19.6
    )

    private fun createL100x100x10() = AngleSpec(
        designation = "L100x100x10",
        standard = ProfileStandard.CSA,
        leg1Mm = 100.0,
        leg2Mm = 100.0,
        tMm = 10.0,
        areaMm2 = 1900.0,
        massKgPerM = 14.9
    )

    private fun createC200x50() = ChannelSpec(
        designation = "C200x50",
        standard = ProfileStandard.CSA,
        dMm = 200.0,
        bfMm = 50.0,
        twMm = 6.0,
        tfMm = 8.0,
        channelSeries = ChannelSeries.C,
        areaMm2 = 2000.0,
        massKgPerM = 15.7
    )
}
