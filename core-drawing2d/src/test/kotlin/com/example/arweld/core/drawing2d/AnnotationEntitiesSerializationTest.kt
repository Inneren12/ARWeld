package com.example.arweld.core.drawing2d

import com.example.arweld.core.drawing2d.v1.PointV1
import com.example.arweld.core.drawing2d.v1.entities.DimensionKindV1
import com.example.arweld.core.drawing2d.v1.entities.DimensionV1
import com.example.arweld.core.drawing2d.v1.entities.EntityV1
import com.example.arweld.core.drawing2d.v1.entities.GroupV1
import com.example.arweld.core.drawing2d.v1.entities.TagV1
import com.example.arweld.core.drawing2d.v1.entities.TextV1
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Drawing2D v1 annotation entity serialization.
 */
class AnnotationEntitiesSerializationTest {

    @Test
    fun `TextV1 roundtrip serialization`() {
        val original = TextV1(
            id = "text-1",
            layerId = "layer-annotations",
            anchor = PointV1(x = 12.5, y = -4.0),
            value = "NOTE: VERIFY",
            rotationDeg = 15.0
        )

        val json = Drawing2DJson.encodeToString<EntityV1>(original)
        val decoded = Drawing2DJson.decodeFromString<EntityV1>(json)

        assertThat(decoded).isEqualTo(original)
        assertThat(json).contains("\"type\":\"text\"")
    }

    @Test
    fun `TextV1 deterministic serialization`() {
        val text = TextV1(
            id = "text-2",
            layerId = "layer-annotations",
            anchor = PointV1(x = 0.0, y = 0.0),
            value = "",
            rotationDeg = 0.0
        )

        val json1 = Drawing2DJson.encodeToString<EntityV1>(text)
        val json2 = Drawing2DJson.encodeToString<EntityV1>(text)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `DimensionV1 roundtrip serialization`() {
        val original = DimensionV1(
            id = "dim-1",
            layerId = "layer-annotations",
            kind = DimensionKindV1.LINEAR,
            p1 = PointV1(x = 0.0, y = 0.0),
            p2 = PointV1(x = 100.0, y = 0.0),
            text = "100",
            offsetPx = 8.0
        )

        val json = Drawing2DJson.encodeToString<EntityV1>(original)
        val decoded = Drawing2DJson.decodeFromString<EntityV1>(json)

        assertThat(decoded).isEqualTo(original)
        assertThat(json).contains("\"type\":\"dimension\"")
    }

    @Test
    fun `DimensionV1 deterministic serialization`() {
        val dimension = DimensionV1(
            id = "dim-2",
            layerId = "layer-annotations",
            kind = DimensionKindV1.LINEAR,
            p1 = PointV1(x = 5.0, y = 10.0),
            p2 = PointV1(x = 25.0, y = 10.0)
        )

        val json1 = Drawing2DJson.encodeToString<EntityV1>(dimension)
        val json2 = Drawing2DJson.encodeToString<EntityV1>(dimension)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `TagV1 roundtrip serialization`() {
        val original = TagV1(
            id = "tag-1",
            layerId = "layer-annotations",
            targetId = "line-1",
            key = "inspection",
            value = "required"
        )

        val json = Drawing2DJson.encodeToString<EntityV1>(original)
        val decoded = Drawing2DJson.decodeFromString<EntityV1>(json)

        assertThat(decoded).isEqualTo(original)
        assertThat(json).contains("\"type\":\"tag\"")
    }

    @Test
    fun `TagV1 deterministic serialization`() {
        val tag = TagV1(
            id = "tag-2",
            layerId = "layer-annotations",
            targetId = "circle-9",
            key = "priority",
            value = "high"
        )

        val json1 = Drawing2DJson.encodeToString<EntityV1>(tag)
        val json2 = Drawing2DJson.encodeToString<EntityV1>(tag)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `GroupV1 roundtrip serialization`() {
        val original = GroupV1(
            id = "group-1",
            layerId = "layer-annotations",
            members = listOf("line-1", "circle-2", "text-3")
        )

        val json = Drawing2DJson.encodeToString<EntityV1>(original)
        val decoded = Drawing2DJson.decodeFromString<EntityV1>(json)

        assertThat(decoded).isEqualTo(original)
        assertThat(json).contains("\"type\":\"group\"")
    }

    @Test
    fun `GroupV1 deterministic serialization`() {
        val group = GroupV1(
            id = "group-2",
            layerId = "layer-annotations",
            members = listOf("a", "b", "c")
        )

        val json1 = Drawing2DJson.encodeToString<EntityV1>(group)
        val json2 = Drawing2DJson.encodeToString<EntityV1>(group)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `polymorphic decoding preserves tag and group subtypes`() {
        val tagJson = """
            {"type":"tag","id":"tag-3","layerId":"layer-1","targetId":"line-4","key":"status","value":"todo"}
        """.trimIndent()
        val groupJson = """
            {"type":"group","id":"group-3","layerId":"layer-1","members":["line-1","line-2"]}
        """.trimIndent()

        val tagDecoded = Drawing2DJson.decodeFromString<EntityV1>(tagJson)
        val groupDecoded = Drawing2DJson.decodeFromString<EntityV1>(groupJson)

        assertThat(tagDecoded).isInstanceOf(TagV1::class.java)
        assertThat(tagDecoded).isEqualTo(
            TagV1(
                id = "tag-3",
                layerId = "layer-1",
                targetId = "line-4",
                key = "status",
                value = "todo"
            )
        )
        assertThat(groupDecoded).isInstanceOf(GroupV1::class.java)
        assertThat(groupDecoded).isEqualTo(
            GroupV1(
                id = "group-3",
                layerId = "layer-1",
                members = listOf("line-1", "line-2")
            )
        )
    }
}
