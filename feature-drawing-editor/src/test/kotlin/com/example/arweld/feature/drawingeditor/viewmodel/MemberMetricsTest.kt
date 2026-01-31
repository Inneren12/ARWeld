package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Member2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.core.drawing2d.editor.v1.ScaleInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class MemberMetricsTest {

    @Test
    fun `computeLengthPx returns Euclidean distance`() {
        val nodeA = Node2D(id = "N1", x = 0.0, y = 0.0)
        val nodeB = Node2D(id = "N2", x = 3.0, y = 4.0)

        val length = computeLengthPx(nodeA, nodeB)

        assertEquals(5.0, length)
    }

    @Test
    fun `computeMmPerPx and computeLengthMm follow scale formula`() {
        val scale = ScaleInfo(
            pointA = Point2D(0.0, 0.0),
            pointB = Point2D(0.0, 10.0),
            realLengthMm = 200.0,
        )

        val mmPerPx = computeMmPerPx(scale)
        val lengthMm = computeLengthMm(lengthPx = 5.0, mmPerPx = mmPerPx)

        assertEquals(20.0, mmPerPx)
        assertEquals(100.0, lengthMm)
    }

    @Test
    fun `computeLengthMmOrNull returns null when scale missing`() {
        val lengthMm = computeLengthMmOrNull(lengthPx = 12.0, scaleInfo = null)

        assertNull(lengthMm)
    }

    @Test
    fun `resolveMemberEndpoints returns missing nodes when member references invalid ids`() {
        val drawing = Drawing2D(
            nodes = listOf(Node2D(id = "N1", x = 0.0, y = 0.0)),
            members = listOf(Member2D(id = "M1", aNodeId = "N1", bNodeId = "N2")),
        )

        val result = resolveMemberEndpoints(drawing, memberId = "M1")

        val missing = assertIs<MemberEndpointResolution.MissingNodes>(result)
        assertEquals(listOf("N2"), missing.missingNodeIds)
    }
}
