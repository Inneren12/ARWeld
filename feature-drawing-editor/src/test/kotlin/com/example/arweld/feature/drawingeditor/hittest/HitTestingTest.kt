package com.example.arweld.feature.drawingeditor.hittest

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Member2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.feature.drawingeditor.viewmodel.EditorSelection
import com.example.arweld.feature.drawingeditor.viewmodel.ViewTransform
import org.junit.Assert.assertEquals
import org.junit.Test

class HitTestingTest {
    private val defaultTransform = ViewTransform(scale = 1f, offsetX = 0f, offsetY = 0f)

    @Test
    fun `distanceSquaredPointToSegment returns zero when on segment`() {
        val distanceSq = distanceSquaredPointToSegment(
            point = Point2D(0.0, 0.0),
            start = Point2D(-2.0, 0.0),
            end = Point2D(2.0, 0.0),
        )

        assertEquals(0.0, distanceSq, 1e-9)
    }

    @Test
    fun `distanceSquaredPointToSegment handles degenerate segment`() {
        val distanceSq = distanceSquaredPointToSegment(
            point = Point2D(3.0, 4.0),
            start = Point2D(0.0, 0.0),
            end = Point2D(0.0, 0.0),
        )

        assertEquals(25.0, distanceSq, 1e-9)
    }

    @Test
    fun `hitTestNode chooses nearest and breaks ties by lowest id`() {
        val nodes = listOf(
            Node2D(id = "N2", x = 1.0, y = 0.0),
            Node2D(id = "N1", x = -1.0, y = 0.0),
        )
        val nearTap = Point2D(0.8, 0.0)
        val tieTap = Point2D(0.0, 0.0)

        val nearest = hitTestNode(
            worldTap = nearTap,
            nodes = nodes,
            tolerancePx = 4f,
            viewTransform = defaultTransform,
        )
        val tieBroken = hitTestNode(
            worldTap = tieTap,
            nodes = nodes,
            tolerancePx = 4f,
            viewTransform = defaultTransform,
        )

        assertEquals("N2", nearest)
        assertEquals("N1", tieBroken)
    }

    @Test
    fun `hitTestMember chooses nearest and breaks ties by lowest id`() {
        val nodes = listOf(
            Node2D(id = "A", x = 0.0, y = 0.0),
            Node2D(id = "B", x = 10.0, y = 0.0),
            Node2D(id = "C", x = 0.0, y = 1.0),
            Node2D(id = "D", x = 10.0, y = 1.0),
        )
        val members = listOf(
            Member2D(id = "M2", aNodeId = "C", bNodeId = "D"),
            Member2D(id = "M1", aNodeId = "A", bNodeId = "B"),
        )
        val nearTap = Point2D(5.0, 0.2)
        val tieTap = Point2D(5.0, 0.5)

        val nearest = hitTestMember(
            worldTap = nearTap,
            members = members,
            nodes = nodes,
            tolerancePx = 2f,
            viewTransform = defaultTransform,
        )
        val tieBroken = hitTestMember(
            worldTap = tieTap,
            members = members,
            nodes = nodes,
            tolerancePx = 2f,
            viewTransform = defaultTransform,
        )

        assertEquals("M1", nearest)
        assertEquals("M1", tieBroken)
    }

    @Test
    fun `selectEntityAtTap prioritizes nodes over members`() {
        val nodes = listOf(
            Node2D(id = "N1", x = 0.0, y = 0.0),
            Node2D(id = "N2", x = 10.0, y = 0.0),
        )
        val members = listOf(
            Member2D(id = "M1", aNodeId = "N1", bNodeId = "N2"),
        )
        val drawing = Drawing2D(nodes = nodes, members = members)

        val selection = selectEntityAtTap(
            worldTap = Point2D(0.0, 0.0),
            drawing = drawing,
            tolerancePx = 4f,
            viewTransform = defaultTransform,
        )

        assertEquals(EditorSelection.Node("N1"), selection)
    }

    @Test
    fun `hitTestNode tolerance stays consistent across zoom`() {
        val nodes = listOf(Node2D(id = "N1", x = 0.0, y = 0.0))
        val tolerancePx = 10f
        val tapScreenPx = 8f
        val zoomedIn = ViewTransform(scale = 2f, offsetX = 0f, offsetY = 0f)

        val tapWorldAtScale1 = Point2D(tapScreenPx.toDouble(), 0.0)
        val tapWorldAtScale2 = Point2D((tapScreenPx / 2f).toDouble(), 0.0)

        val hitAtScale1 = hitTestNode(
            worldTap = tapWorldAtScale1,
            nodes = nodes,
            tolerancePx = tolerancePx,
            viewTransform = defaultTransform,
        )
        val hitAtScale2 = hitTestNode(
            worldTap = tapWorldAtScale2,
            nodes = nodes,
            tolerancePx = tolerancePx,
            viewTransform = zoomedIn,
        )

        assertEquals("N1", hitAtScale1)
        assertEquals("N1", hitAtScale2)
    }
}
