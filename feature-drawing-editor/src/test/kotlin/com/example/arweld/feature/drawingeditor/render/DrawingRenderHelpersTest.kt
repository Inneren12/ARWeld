package com.example.arweld.feature.drawingeditor.render

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Member2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawingRenderHelpersTest {

    // ======= buildNodeLookup tests =======

    @Test
    fun `buildNodeLookup returns empty map for empty nodes list`() {
        val drawing = Drawing2D(nodes = emptyList(), members = emptyList())
        val lookup = buildNodeLookup(drawing)
        assertTrue(lookup.isEmpty())
    }

    @Test
    fun `buildNodeLookup correctly maps nodes by id`() {
        val node1 = Node2D(id = "n1", x = 10.0, y = 20.0)
        val node2 = Node2D(id = "n2", x = 30.0, y = 40.0)
        val drawing = Drawing2D(nodes = listOf(node1, node2), members = emptyList())

        val lookup = buildNodeLookup(drawing)

        assertEquals(2, lookup.size)
        assertEquals(node1, lookup["n1"])
        assertEquals(node2, lookup["n2"])
        assertNull(lookup["nonexistent"])
    }

    // ======= resolveMemberEndpoints tests =======

    @Test
    fun `resolveMemberEndpoints returns Resolved when both nodes exist`() {
        val node1 = Node2D(id = "n1", x = 10.0, y = 20.0)
        val node2 = Node2D(id = "n2", x = 30.0, y = 40.0)
        val member = Member2D(id = "m1", aNodeId = "n1", bNodeId = "n2")
        val lookup = mapOf("n1" to node1, "n2" to node2)

        val result = resolveMemberEndpoints(member, lookup, logMissing = false)

        assertTrue(result is MemberEndpointResult.Resolved)
        val resolved = result as MemberEndpointResult.Resolved
        assertEquals("m1", resolved.memberId)
        assertEquals(node1, resolved.aNode)
        assertEquals(node2, resolved.bNode)
    }

    @Test
    fun `resolveMemberEndpoints returns MissingNodes when aNodeId not found`() {
        val node2 = Node2D(id = "n2", x = 30.0, y = 40.0)
        val member = Member2D(id = "m1", aNodeId = "missing", bNodeId = "n2")
        val lookup = mapOf("n2" to node2)

        val result = resolveMemberEndpoints(member, lookup, logMissing = false)

        assertTrue(result is MemberEndpointResult.MissingNodes)
        val missing = result as MemberEndpointResult.MissingNodes
        assertEquals("m1", missing.memberId)
        assertEquals(listOf("missing"), missing.missingNodeIds)
    }

    @Test
    fun `resolveMemberEndpoints returns MissingNodes when bNodeId not found`() {
        val node1 = Node2D(id = "n1", x = 10.0, y = 20.0)
        val member = Member2D(id = "m1", aNodeId = "n1", bNodeId = "missing")
        val lookup = mapOf("n1" to node1)

        val result = resolveMemberEndpoints(member, lookup, logMissing = false)

        assertTrue(result is MemberEndpointResult.MissingNodes)
        val missing = result as MemberEndpointResult.MissingNodes
        assertEquals("m1", missing.memberId)
        assertEquals(listOf("missing"), missing.missingNodeIds)
    }

    @Test
    fun `resolveMemberEndpoints returns MissingNodes with both ids when neither found`() {
        val member = Member2D(id = "m1", aNodeId = "missing1", bNodeId = "missing2")
        val lookup = emptyMap<String, Node2D>()

        val result = resolveMemberEndpoints(member, lookup, logMissing = false)

        assertTrue(result is MemberEndpointResult.MissingNodes)
        val missing = result as MemberEndpointResult.MissingNodes
        assertEquals("m1", missing.memberId)
        assertEquals(2, missing.missingNodeIds.size)
        assertTrue(missing.missingNodeIds.contains("missing1"))
        assertTrue(missing.missingNodeIds.contains("missing2"))
    }

    // ======= resolveAllMemberEndpoints tests =======

    @Test
    fun `resolveAllMemberEndpoints returns empty list for empty drawing`() {
        val drawing = Drawing2D(nodes = emptyList(), members = emptyList())
        val result = resolveAllMemberEndpoints(drawing)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `resolveAllMemberEndpoints resolves valid members`() {
        val node1 = Node2D(id = "n1", x = 10.0, y = 20.0)
        val node2 = Node2D(id = "n2", x = 30.0, y = 40.0)
        val member = Member2D(id = "m1", aNodeId = "n1", bNodeId = "n2", profileRef = "W310x39")
        val drawing = Drawing2D(nodes = listOf(node1, node2), members = listOf(member))

        val result = resolveAllMemberEndpoints(drawing)

        assertEquals(1, result.size)
        val resolved = result[0]
        assertEquals("m1", resolved.memberId)
        assertEquals(10f, resolved.startPoint.x, 0.001f)
        assertEquals(20f, resolved.startPoint.y, 0.001f)
        assertEquals(30f, resolved.endPoint.x, 0.001f)
        assertEquals(40f, resolved.endPoint.y, 0.001f)
        assertEquals("W310x39", resolved.profileRef)
    }

    @Test
    fun `resolveAllMemberEndpoints skips members with missing nodes`() {
        val node1 = Node2D(id = "n1", x = 10.0, y = 20.0)
        val node2 = Node2D(id = "n2", x = 30.0, y = 40.0)
        val validMember = Member2D(id = "m1", aNodeId = "n1", bNodeId = "n2")
        val invalidMember = Member2D(id = "m2", aNodeId = "n1", bNodeId = "missing")
        val drawing = Drawing2D(
            nodes = listOf(node1, node2),
            members = listOf(validMember, invalidMember)
        )

        val result = resolveAllMemberEndpoints(drawing)

        assertEquals(1, result.size)
        assertEquals("m1", result[0].memberId)
    }

    @Test
    fun `resolveAllMemberEndpoints handles all members with missing references`() {
        val member1 = Member2D(id = "m1", aNodeId = "missing1", bNodeId = "missing2")
        val member2 = Member2D(id = "m2", aNodeId = "missing3", bNodeId = "missing4")
        val drawing = Drawing2D(nodes = emptyList(), members = listOf(member1, member2))

        val result = resolveAllMemberEndpoints(drawing)

        assertTrue(result.isEmpty())
    }

    // ======= resolveAllMemberEndpointsWithSummary tests =======

    @Test
    fun `resolveAllMemberEndpointsWithSummary reports correct summary`() {
        val node1 = Node2D(id = "n1", x = 10.0, y = 20.0)
        val node2 = Node2D(id = "n2", x = 30.0, y = 40.0)
        val validMember = Member2D(id = "m1", aNodeId = "n1", bNodeId = "n2")
        val invalidMember1 = Member2D(id = "m2", aNodeId = "n1", bNodeId = "missing")
        val invalidMember2 = Member2D(id = "m3", aNodeId = "missing", bNodeId = "missing2")
        val drawing = Drawing2D(
            nodes = listOf(node1, node2),
            members = listOf(validMember, invalidMember1, invalidMember2)
        )

        val summary = resolveAllMemberEndpointsWithSummary(drawing)

        assertEquals(1, summary.resolvedMembers.size)
        assertEquals(2, summary.skippedCount)
        assertEquals("m1", summary.resolvedMembers[0].memberId)
    }

    @Test
    fun `resolveAllMemberEndpointsWithSummary with all valid members`() {
        val node1 = Node2D(id = "n1", x = 10.0, y = 20.0)
        val node2 = Node2D(id = "n2", x = 30.0, y = 40.0)
        val node3 = Node2D(id = "n3", x = 50.0, y = 60.0)
        val member1 = Member2D(id = "m1", aNodeId = "n1", bNodeId = "n2")
        val member2 = Member2D(id = "m2", aNodeId = "n2", bNodeId = "n3")
        val drawing = Drawing2D(
            nodes = listOf(node1, node2, node3),
            members = listOf(member1, member2)
        )

        val summary = resolveAllMemberEndpointsWithSummary(drawing)

        assertEquals(2, summary.resolvedMembers.size)
        assertEquals(0, summary.skippedCount)
    }

    // ======= toPoint2 extension tests =======

    @Test
    fun `toPoint2 converts Node2D coordinates correctly`() {
        val node = Node2D(id = "n1", x = 123.456, y = 789.012)
        val point = node.toPoint2()

        assertEquals(123.456f, point.x, 0.001f)
        assertEquals(789.012f, point.y, 0.001f)
    }

    @Test
    fun `toPoint2 handles negative coordinates`() {
        val node = Node2D(id = "n1", x = -50.5, y = -100.25)
        val point = node.toPoint2()

        assertEquals(-50.5f, point.x, 0.001f)
        assertEquals(-100.25f, point.y, 0.001f)
    }

    @Test
    fun `toPoint2 handles zero coordinates`() {
        val node = Node2D(id = "n1", x = 0.0, y = 0.0)
        val point = node.toPoint2()

        assertEquals(0f, point.x, 0.001f)
        assertEquals(0f, point.y, 0.001f)
    }
}
