package com.example.arweld.core.drawing2d.editor.v1

import com.example.arweld.core.drawing2d.v1.MetaEntryV1
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Drawing2DEditorSerializationTest {

    @Test
    fun `drawing2d editor round trips through json`() {
        val drawing = Drawing2D(
            nodes = listOf(
                Node2D(id = "n1", x = 10.0, y = 20.0),
                Node2D(id = "n2", x = 40.0, y = 60.0, meta = listOf(MetaEntryV1("tag", "anchor")))
            ),
            members = listOf(
                Member2D(id = "m1", aNodeId = "n1", bNodeId = "n2", profileRef = "W310x39")
            ),
            scale = ScaleInfo(
                pointA = Point2D(x = 10.0, y = 20.0),
                pointB = Point2D(x = 40.0, y = 60.0),
                realLengthMm = 1500.0
            ),
            meta = Drawing2DEditorMetaV1(entries = listOf(MetaEntryV1("source", "manual")))
        )

        val json = Drawing2DEditorJson.encodeToString(drawing)
        val decoded = Drawing2DEditorJson.decodeFromString(json)

        assertThat(decoded).isEqualTo(drawing)
    }

    @Test
    fun `schemaVersion preserved and optional fields omitted when null`() {
        val drawing = Drawing2D(
            nodes = listOf(
                Node2D(id = "n1", x = 0.0, y = 0.0)
            ),
            members = emptyList()
        )

        val json = Drawing2DEditorJson.encodeToString(drawing)
        val decoded = Drawing2DEditorJson.decodeFromString(json)

        assertThat(json).contains("\"schemaVersion\":1")
        assertThat(json).doesNotContain("\"scale\"")
        assertThat(json).doesNotContain("\"meta\"")
        assertThat(decoded.schemaVersion).isEqualTo(1)
        assertThat(decoded.scale).isNull()
        assertThat(decoded.meta).isNull()
    }
}
