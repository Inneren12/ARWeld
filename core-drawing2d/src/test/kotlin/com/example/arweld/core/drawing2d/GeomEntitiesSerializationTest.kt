package com.example.arweld.core.drawing2d

import com.example.arweld.core.drawing2d.v1.PointV1
import com.example.arweld.core.drawing2d.v1.entities.ArcV1
import com.example.arweld.core.drawing2d.v1.entities.CircleV1
import com.example.arweld.core.drawing2d.v1.entities.EntityV1
import com.example.arweld.core.drawing2d.v1.entities.LineV1
import com.example.arweld.core.drawing2d.v1.entities.PolylineV1
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Drawing2D v1 geometric entities serialization.
 */
class GeomEntitiesSerializationTest {

    @Test
    fun `LineV1 roundtrip serialization`() {
        val original = LineV1(
            id = "line-1",
            layerId = "layer-1",
            styleId = "style-1",
            a = PointV1(x = 0.0, y = 0.0),
            b = PointV1(x = 100.0, y = 200.0)
        )

        val json = Drawing2DJson.encodeToString<EntityV1>(original)
        val decoded = Drawing2DJson.decodeFromString<EntityV1>(json)

        assertThat(decoded).isEqualTo(original)
        assertThat(json).contains("\"type\":\"line\"")
    }

    @Test
    fun `LineV1 deterministic serialization`() {
        val line = LineV1(
            id = "line-2",
            layerId = "layer-2",
            a = PointV1(x = 5.0, y = 10.0),
            b = PointV1(x = 15.0, y = 20.0)
        )

        val json1 = Drawing2DJson.encodeToString<EntityV1>(line)
        val json2 = Drawing2DJson.encodeToString<EntityV1>(line)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `PolylineV1 roundtrip serialization`() {
        val original = PolylineV1(
            id = "poly-1",
            layerId = "layer-main",
            points = listOf(
                PointV1(x = 0.0, y = 0.0),
                PointV1(x = 10.0, y = 0.0),
                PointV1(x = 10.0, y = 10.0)
            ),
            closed = false
        )

        val json = Drawing2DJson.encodeToString<EntityV1>(original)
        val decoded = Drawing2DJson.decodeFromString<EntityV1>(json)

        assertThat(decoded).isEqualTo(original)
        assertThat(json).contains("\"type\":\"polyline\"")
    }

    @Test
    fun `PolylineV1 deterministic serialization`() {
        val polyline = PolylineV1(
            id = "poly-2",
            layerId = "layer-main",
            points = listOf(
                PointV1(x = -5.0, y = -5.0),
                PointV1(x = 0.0, y = 0.0)
            ),
            closed = true
        )

        val json1 = Drawing2DJson.encodeToString<EntityV1>(polyline)
        val json2 = Drawing2DJson.encodeToString<EntityV1>(polyline)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `CircleV1 roundtrip serialization`() {
        val original = CircleV1(
            id = "circle-1",
            layerId = "layer-geom",
            c = PointV1(x = 25.0, y = 30.0),
            r = 12.5
        )

        val json = Drawing2DJson.encodeToString<EntityV1>(original)
        val decoded = Drawing2DJson.decodeFromString<EntityV1>(json)

        assertThat(decoded).isEqualTo(original)
        assertThat(json).contains("\"type\":\"circle\"")
    }

    @Test
    fun `CircleV1 deterministic serialization`() {
        val circle = CircleV1(
            id = "circle-2",
            layerId = "layer-geom",
            c = PointV1(x = -10.0, y = 8.0),
            r = 4.0
        )

        val json1 = Drawing2DJson.encodeToString<EntityV1>(circle)
        val json2 = Drawing2DJson.encodeToString<EntityV1>(circle)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `ArcV1 roundtrip serialization`() {
        val original = ArcV1(
            id = "arc-1",
            layerId = "layer-geom",
            c = PointV1(x = 5.0, y = 5.0),
            r = 10.0,
            startAngleDeg = 45.0,
            endAngleDeg = 135.0,
            cw = true
        )

        val json = Drawing2DJson.encodeToString<EntityV1>(original)
        val decoded = Drawing2DJson.decodeFromString<EntityV1>(json)

        assertThat(decoded).isEqualTo(original)
        assertThat(json).contains("\"type\":\"arc\"")
    }

    @Test
    fun `ArcV1 deterministic serialization`() {
        val arc = ArcV1(
            id = "arc-2",
            layerId = "layer-geom",
            c = PointV1(x = 1.0, y = 2.0),
            r = 3.0,
            startAngleDeg = 0.0,
            endAngleDeg = 270.0,
            cw = false
        )

        val json1 = Drawing2DJson.encodeToString<EntityV1>(arc)
        val json2 = Drawing2DJson.encodeToString<EntityV1>(arc)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `polymorphic decoding preserves subtype`() {
        val lineJson = """
            {"type":"line","id":"line-3","layerId":"layer-1","a":{"x":1.0,"y":2.0},"b":{"x":3.0,"y":4.0}}
        """.trimIndent()

        val decoded = Drawing2DJson.decodeFromString<EntityV1>(lineJson)

        assertThat(decoded).isInstanceOf(LineV1::class.java)
        assertThat(decoded).isEqualTo(
            LineV1(
                id = "line-3",
                layerId = "layer-1",
                a = PointV1(x = 1.0, y = 2.0),
                b = PointV1(x = 3.0, y = 4.0)
            )
        )
    }
}
