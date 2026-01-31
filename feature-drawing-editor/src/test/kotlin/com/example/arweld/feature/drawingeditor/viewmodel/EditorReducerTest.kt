package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Member2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.core.drawing2d.editor.v1.missingNodeReferences
import org.junit.Assert.assertEquals
import org.junit.Test

class EditorReducerTest {

    @Test
    fun `tool change updates tool and clears selection`() {
        val initial = EditorState(
            tool = EditorTool.SELECT,
            selection = EditorSelection.Node("N1"),
            isLoading = false,
        )

        val result = reduceEditorState(initial, EditorIntent.ToolChanged(EditorTool.SCALE))

        assertEquals(EditorTool.SCALE, result.tool)
        assertEquals(EditorSelection.None, result.selection)
    }

    @Test
    fun `loaded updates drawing and clears loading and error`() {
        val drawing = Drawing2D(
            nodes = listOf(Node2D(id = "N1", x = 0.0, y = 0.0)),
            members = listOf(Member2D(id = "M1", aNodeId = "N1", bNodeId = "N1")),
        )
        val initial = EditorState(
            isLoading = true,
            lastError = "Previous error",
        )

        val result = reduceEditorState(initial, EditorIntent.Loaded(drawing))

        assertEquals(false, result.isLoading)
        assertEquals(null, result.lastError)
        assertEquals(drawing, result.drawing)
    }

    @Test
    fun `select entity updates selection`() {
        val initial = EditorState(selection = EditorSelection.None, isLoading = false)

        val result = reduceEditorState(initial, EditorIntent.SelectEntity(EditorSelection.Member("M1")))

        assertEquals(EditorSelection.Member("M1"), result.selection)
    }

    @Test
    fun `error sets error and clears loading`() {
        val initial = EditorState(isLoading = true)

        val result = reduceEditorState(initial, EditorIntent.Error("Load failed"))

        assertEquals(false, result.isLoading)
        assertEquals("Load failed", result.lastError)
    }

    @Test
    fun `node tap on empty adds node with deterministic id and selects it`() {
        val initial = EditorState(
            tool = EditorTool.NODE,
            drawing = Drawing2D(nodes = emptyList(), members = emptyList()),
            isLoading = false,
        )
        val tap = Point2D(x = 12.5, y = -4.0)

        val result = reduceEditorState(initial, EditorIntent.NodeTap(tap, tolerancePx = 16f))

        assertEquals(1, result.drawing.nodes.size)
        assertEquals(EditorSelection.Node("N000001"), result.selection)
        assertEquals(2, result.drawing.meta?.nextNodeId)
        assertEquals(tap.x, result.drawing.nodes.first().x, 0.0)
        assertEquals(tap.y, result.drawing.nodes.first().y, 0.0)
    }

    @Test
    fun `node tap near existing node selects it without creating new node`() {
        val existing = Node2D(id = "N000005", x = 0.0, y = 0.0)
        val initial = EditorState(
            tool = EditorTool.NODE,
            drawing = Drawing2D(nodes = listOf(existing), members = emptyList()),
            isLoading = false,
        )

        val result = reduceEditorState(initial, EditorIntent.NodeTap(Point2D(x = 1.0, y = 1.0), tolerancePx = 16f))

        assertEquals(1, result.drawing.nodes.size)
        assertEquals(EditorSelection.Node("N000005"), result.selection)
    }

    @Test
    fun `node delete removes node and connected members`() {
        val drawing = Drawing2D(
            nodes = listOf(
                Node2D(id = "N1", x = 0.0, y = 0.0),
                Node2D(id = "N2", x = 10.0, y = 0.0),
                Node2D(id = "N3", x = 20.0, y = 0.0),
            ),
            members = listOf(
                Member2D(id = "M1", aNodeId = "N1", bNodeId = "N2"),
                Member2D(id = "M2", aNodeId = "N2", bNodeId = "N3"),
                Member2D(id = "M3", aNodeId = "N1", bNodeId = "N3"),
            ),
        )
        val initial = EditorState(
            drawing = drawing,
            selection = EditorSelection.Node("N2"),
        )

        val result = reduceEditorState(initial, EditorIntent.NodeDeleteRequested("N2"))

        assertEquals(listOf("N1", "N3"), result.drawing.nodes.map { it.id })
        assertEquals(listOf("M3"), result.drawing.members.map { it.id })
        assertEquals(EditorSelection.None, result.selection)
        assertEquals(emptyList<String>(), result.drawing.missingNodeReferences())
    }

    @Test
    fun `member tool first tap stores draft node`() {
        val initial = EditorState(
            tool = EditorTool.MEMBER,
            drawing = Drawing2D(
                nodes = listOf(Node2D(id = "N1", x = 0.0, y = 0.0)),
                members = emptyList(),
            ),
            isLoading = false,
        )

        val result = reduceEditorState(initial, EditorIntent.MemberNodeTapped("N1"))

        assertEquals("N1", result.memberDraft.nodeAId)
        assertEquals(0, result.drawing.members.size)
        assertEquals(EditorSelection.None, result.selection)
    }

    @Test
    fun `member tool second tap creates member with canonical endpoints`() {
        val initial = EditorState(
            tool = EditorTool.MEMBER,
            drawing = Drawing2D(
                nodes = listOf(
                    Node2D(id = "N1", x = 0.0, y = 0.0),
                    Node2D(id = "N2", x = 10.0, y = 0.0),
                ),
                members = emptyList(),
            ),
            memberDraft = MemberDraft(nodeAId = "N2"),
            isLoading = false,
        )

        val result = reduceEditorState(initial, EditorIntent.MemberNodeTapped("N1"))

        assertEquals(1, result.drawing.members.size)
        val member = result.drawing.members.first()
        assertEquals("M000001", member.id)
        assertEquals("N1", member.aNodeId)
        assertEquals("N2", member.bNodeId)
        assertEquals(2, result.drawing.meta?.nextMemberId)
        assertEquals(EditorSelection.Member("M000001"), result.selection)
        assertEquals(null, result.memberDraft.nodeAId)
    }
}
