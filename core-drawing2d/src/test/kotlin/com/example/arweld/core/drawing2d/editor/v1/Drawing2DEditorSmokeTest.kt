package com.example.arweld.core.drawing2d.editor.v1

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Smoke tests for the 2D3D editor foundations.
 *
 * These tests verify that the core wiring for drawing serialization and
 * basic validation works correctly. They catch regressions in boot/open
 * of the 2D3D editor without requiring a full UI.
 *
 * For workspace path tests, see core-domain/Project2D3DWorkspaceTest.
 *
 * @see docs/2d3d/testing.md for details on running these tests
 */
class Drawing2DEditorSmokeTest {

    @Test
    fun `empty drawing serializes and deserializes correctly`() {
        // Given: an empty drawing (no nodes, no members, no scale)
        val emptyDrawing = Drawing2D(
            nodes = emptyList(),
            members = emptyList(),
            scale = null,
            meta = null
        )

        // When: serialized to JSON and deserialized back
        val json = Drawing2DEditorJson.encodeToString(emptyDrawing)
        val restored = Drawing2DEditorJson.decodeFromString(json)

        // Then: the restored drawing equals the original
        assertThat(restored).isEqualTo(emptyDrawing)
        assertThat(restored.schemaVersion).isEqualTo(1)
        assertThat(restored.nodes).isEmpty()
        assertThat(restored.members).isEmpty()
        assertThat(restored.scale).isNull()
    }

    @Test
    fun `empty drawing has no missing node references`() {
        // Given: an empty drawing
        val emptyDrawing = Drawing2D(
            nodes = emptyList(),
            members = emptyList()
        )

        // When: checking for missing node references
        val missing = emptyDrawing.missingNodeReferences()

        // Then: no references are missing
        assertThat(missing).isEmpty()
    }

    @Test
    fun `empty drawing json contains schema version`() {
        // Given: an empty drawing
        val emptyDrawing = Drawing2D(
            nodes = emptyList(),
            members = emptyList()
        )

        // When: serialized to JSON
        val json = Drawing2DEditorJson.encodeToString(emptyDrawing)

        // Then: schema version is present for forward compatibility
        assertThat(json).contains("\"schemaVersion\":1")
    }

    @Test
    fun `drawing with initialized meta roundtrips correctly`() {
        // Given: a drawing with initialized meta (simulates first save)
        val drawing = Drawing2D(
            nodes = emptyList(),
            members = emptyList(),
            meta = Drawing2DEditorMetaV1(
                nextNodeId = 1,
                nextMemberId = 1,
                entries = null
            )
        )

        // When: serialized and deserialized
        val json = Drawing2DEditorJson.encodeToString(drawing)
        val restored = Drawing2DEditorJson.decodeFromString(json)

        // Then: meta state is preserved
        assertThat(restored.meta).isNotNull()
        assertThat(restored.meta?.nextNodeId).isEqualTo(1)
        assertThat(restored.meta?.nextMemberId).isEqualTo(1)
    }

    @Test
    fun `empty drawing roundtrips through save and load cycle`() {
        // Given: an empty drawing
        val emptyDrawing = Drawing2D(
            nodes = emptyList(),
            members = emptyList()
        )

        // When: simulating save (serialize) and load (deserialize) cycle
        val savedJson = Drawing2DEditorJson.encodeToString(emptyDrawing)
        val loadedDrawing = Drawing2DEditorJson.decodeFromString(savedJson)

        // Then: the drawing roundtrips and has no invalid references
        assertThat(loadedDrawing).isEqualTo(emptyDrawing)
        assertThat(loadedDrawing.missingNodeReferences()).isEmpty()
    }

    @Test
    fun `json for empty drawing has minimal structure`() {
        // Given: an empty drawing
        val emptyDrawing = Drawing2D(
            nodes = emptyList(),
            members = emptyList()
        )

        // When: serialized to JSON
        val json = Drawing2DEditorJson.encodeToString(emptyDrawing)

        // Then: json contains only required fields
        assertThat(json).contains("\"schemaVersion\":1")
        assertThat(json).contains("\"nodes\":[]")
        assertThat(json).contains("\"members\":[]")
        // Optional fields should be omitted when null
        assertThat(json).doesNotContain("\"scale\"")
        assertThat(json).doesNotContain("\"meta\"")
    }
}
