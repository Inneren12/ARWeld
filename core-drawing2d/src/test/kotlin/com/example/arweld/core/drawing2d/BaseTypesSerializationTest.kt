package com.example.arweld.core.drawing2d

import com.example.arweld.core.drawing2d.v1.AxisDirectionV1
import com.example.arweld.core.drawing2d.v1.AxisV1
import com.example.arweld.core.drawing2d.v1.CoordSpaceTypeV1
import com.example.arweld.core.drawing2d.v1.CoordSpaceV1
import com.example.arweld.core.drawing2d.v1.LayerV1
import com.example.arweld.core.drawing2d.v1.OriginV1
import com.example.arweld.core.drawing2d.v1.PageV1
import com.example.arweld.core.drawing2d.v1.PointV1
import com.example.arweld.core.drawing2d.v1.UnitsV1
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Drawing2D v1 base types serialization.
 *
 * Tests verify:
 * - Roundtrip: encode -> decode produces equal instances
 * - Determinism: encoding the same instance twice produces identical JSON
 * - Schema version constant
 */
class BaseTypesSerializationTest {

    // ==========================================================================
    // Schema Version
    // ==========================================================================

    @Test
    fun `schema version is 1`() {
        assertThat(Drawing2DContract.DRAWING2D_SCHEMA_VERSION).isEqualTo(1)
    }

    // ==========================================================================
    // PointV1 Tests
    // ==========================================================================

    @Test
    fun `PointV1 roundtrip serialization`() {
        val original = PointV1(x = 100.5, y = 200.75)
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<PointV1>(json)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `PointV1 deterministic serialization`() {
        val point = PointV1(x = 42.0, y = 84.0)

        val json1 = Drawing2DJson.encodeToString(point)
        val json2 = Drawing2DJson.encodeToString(point)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `PointV1 JSON format`() {
        val point = PointV1(x = 10.0, y = 20.0)
        val json = Drawing2DJson.encodeToString(point)

        assertThat(json).isEqualTo("""{"x":10.0,"y":20.0}""")
    }

    @Test
    fun `PointV1 with negative coordinates`() {
        val original = PointV1(x = -50.5, y = -100.25)
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<PointV1>(json)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `PointV1 with zero coordinates`() {
        val original = PointV1(x = 0.0, y = 0.0)
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<PointV1>(json)

        assertThat(decoded).isEqualTo(original)
    }

    // ==========================================================================
    // UnitsV1 Tests
    // ==========================================================================

    @Test
    fun `UnitsV1 roundtrip serialization`() {
        val original = UnitsV1.PX
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<UnitsV1>(json)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `UnitsV1 deterministic serialization`() {
        val units = UnitsV1.PX

        val json1 = Drawing2DJson.encodeToString(units)
        val json2 = Drawing2DJson.encodeToString(units)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `UnitsV1 JSON format`() {
        val units = UnitsV1.PX
        val json = Drawing2DJson.encodeToString(units)

        assertThat(json).isEqualTo(""""PX"""")
    }

    // ==========================================================================
    // CoordSpaceV1 Tests
    // ==========================================================================

    @Test
    fun `CoordSpaceV1 roundtrip serialization`() {
        val original = CoordSpaceV1(
            type = CoordSpaceTypeV1.RECTIFIED_PX,
            origin = OriginV1.TOP_LEFT,
            axis = AxisV1(x = AxisDirectionV1.RIGHT, y = AxisDirectionV1.DOWN)
        )
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<CoordSpaceV1>(json)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `CoordSpaceV1 deterministic serialization`() {
        val coordSpace = CoordSpaceV1(
            type = CoordSpaceTypeV1.RECTIFIED_PX,
            origin = OriginV1.TOP_LEFT,
            axis = AxisV1(x = AxisDirectionV1.RIGHT, y = AxisDirectionV1.DOWN)
        )

        val json1 = Drawing2DJson.encodeToString(coordSpace)
        val json2 = Drawing2DJson.encodeToString(coordSpace)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `CoordSpaceTypeV1 roundtrip serialization`() {
        val original = CoordSpaceTypeV1.RECTIFIED_PX
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<CoordSpaceTypeV1>(json)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `OriginV1 roundtrip serialization`() {
        val original = OriginV1.TOP_LEFT
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<OriginV1>(json)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `AxisV1 roundtrip serialization`() {
        val original = AxisV1(x = AxisDirectionV1.RIGHT, y = AxisDirectionV1.DOWN)
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<AxisV1>(json)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `AxisDirectionV1 all values roundtrip`() {
        for (direction in AxisDirectionV1.entries) {
            val json = Drawing2DJson.encodeToString(direction)
            val decoded = Drawing2DJson.decodeFromString<AxisDirectionV1>(json)
            assertThat(decoded).isEqualTo(direction)
        }
    }

    // ==========================================================================
    // PageV1 Tests
    // ==========================================================================

    @Test
    fun `PageV1 roundtrip serialization`() {
        val original = PageV1(widthPx = 1920, heightPx = 1080)
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<PageV1>(json)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `PageV1 deterministic serialization`() {
        val page = PageV1(widthPx = 800, heightPx = 600)

        val json1 = Drawing2DJson.encodeToString(page)
        val json2 = Drawing2DJson.encodeToString(page)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `PageV1 JSON format`() {
        val page = PageV1(widthPx = 1024, heightPx = 768)
        val json = Drawing2DJson.encodeToString(page)

        assertThat(json).isEqualTo("""{"widthPx":1024,"heightPx":768}""")
    }

    // ==========================================================================
    // LayerV1 Tests
    // ==========================================================================

    @Test
    fun `LayerV1 roundtrip serialization`() {
        val original = LayerV1(id = "layer-001", name = "Background", order = 0)
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<LayerV1>(json)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `LayerV1 deterministic serialization`() {
        val layer = LayerV1(id = "layer-002", name = "Foreground", order = 10)

        val json1 = Drawing2DJson.encodeToString(layer)
        val json2 = Drawing2DJson.encodeToString(layer)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `LayerV1 JSON format`() {
        val layer = LayerV1(id = "lyr-1", name = "Main", order = 5)
        val json = Drawing2DJson.encodeToString(layer)

        assertThat(json).isEqualTo("""{"id":"lyr-1","name":"Main","order":5}""")
    }

    @Test
    fun `LayerV1 with negative order`() {
        val original = LayerV1(id = "bg", name = "Deep Background", order = -10)
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<LayerV1>(json)

        assertThat(decoded).isEqualTo(original)
    }

    // ==========================================================================
    // List Serialization Tests (for future entity collections)
    // ==========================================================================

    @Test
    fun `List of PointV1 roundtrip serialization`() {
        val original = listOf(
            PointV1(x = 0.0, y = 0.0),
            PointV1(x = 100.0, y = 0.0),
            PointV1(x = 100.0, y = 100.0),
            PointV1(x = 0.0, y = 100.0)
        )
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<List<PointV1>>(json)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `List of LayerV1 roundtrip serialization`() {
        val original = listOf(
            LayerV1(id = "bg", name = "Background", order = 0),
            LayerV1(id = "main", name = "Main", order = 1),
            LayerV1(id = "fg", name = "Foreground", order = 2)
        )
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<List<LayerV1>>(json)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `List serialization preserves order`() {
        val original = listOf(
            LayerV1(id = "a", name = "A", order = 3),
            LayerV1(id = "b", name = "B", order = 1),
            LayerV1(id = "c", name = "C", order = 2)
        )
        val json = Drawing2DJson.encodeToString(original)
        val decoded = Drawing2DJson.decodeFromString<List<LayerV1>>(json)

        // Verify order is preserved (not sorted by 'order' field)
        assertThat(decoded[0].id).isEqualTo("a")
        assertThat(decoded[1].id).isEqualTo("b")
        assertThat(decoded[2].id).isEqualTo("c")
    }

    // ==========================================================================
    // Drawing2DJson Configuration Tests
    // ==========================================================================

    @Test
    fun `Drawing2DJson instance is accessible`() {
        assertThat(Drawing2DJson.json).isNotNull()
    }

    @Test
    fun `Drawing2DJson produces compact output`() {
        val point = PointV1(x = 1.0, y = 2.0)
        val json = Drawing2DJson.encodeToString(point)

        // Verify no pretty-printing (no newlines or extra spaces)
        assertThat(json).doesNotContain("\n")
        assertThat(json).doesNotContain(" ")
    }
}
