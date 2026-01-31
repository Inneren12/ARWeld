package com.example.arweld.core.drawing2d.editor.v1

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Drawing2DEditorDeterminismTest {

    @Test
    fun `allocator generates deterministic ids for same creation order`() {
        val baseDrawing = Drawing2D(nodes = emptyList(), members = emptyList())

        val firstNode = Drawing2DIdAllocator.allocateNodeId(baseDrawing)
        val firstMember = Drawing2DIdAllocator.allocateMemberId(firstNode.drawing)

        val secondRunNode = Drawing2DIdAllocator.allocateNodeId(baseDrawing)
        val secondRunMember = Drawing2DIdAllocator.allocateMemberId(secondRunNode.drawing)

        assertThat(firstNode.id).isEqualTo("N000001")
        assertThat(firstMember.id).isEqualTo("M000001")
        assertThat(firstNode.id).isEqualTo(secondRunNode.id)
        assertThat(firstMember.id).isEqualTo(secondRunMember.id)
        assertThat(firstMember.drawing.meta?.nextNodeId).isEqualTo(2)
        assertThat(firstMember.drawing.meta?.nextMemberId).isEqualTo(2)
    }

    @Test
    fun `canonical json saves are stable between consecutive calls`() {
        val drawing = Drawing2D(
            nodes = listOf(
                Node2D(id = "N000002", x = 2.0, y = 2.0),
                Node2D(id = "N000001", x = 1.0, y = 1.0)
            ),
            members = listOf(
                Member2D(id = "M000002", aNodeId = "N000001", bNodeId = "N000002"),
                Member2D(id = "M000001", aNodeId = "N000002", bNodeId = "N000001")
            )
        )

        val firstSave = drawing.toCanonicalJson()
        val secondSave = drawing.toCanonicalJson()

        assertThat(firstSave).isEqualTo(secondSave)
        assertThat(drawing.canonicalize().nodes.map { it.id })
            .containsExactly("N000001", "N000002")
            .inOrder()
        assertThat(drawing.canonicalize().members.map { it.id })
            .containsExactly("M000001", "M000002")
            .inOrder()
    }
}
