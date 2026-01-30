package com.example.arweld.core.drawing2d

import com.example.arweld.core.drawing2d.v1.LayerV1
import com.example.arweld.core.drawing2d.v1.MetaEntryV1
import com.example.arweld.core.drawing2d.v1.PointV1
import com.example.arweld.core.drawing2d.v1.entities.CircleV1
import com.example.arweld.core.drawing2d.v1.entities.LineV1
import com.example.arweld.core.drawing2d.v1.patch.AddEntityOpV1
import com.example.arweld.core.drawing2d.v1.patch.AddLayerOpV1
import com.example.arweld.core.drawing2d.v1.patch.DrawingPatchEvent
import com.example.arweld.core.drawing2d.v1.patch.RemoveEntityOpV1
import com.example.arweld.core.drawing2d.v1.patch.RemoveLayerOpV1
import com.example.arweld.core.drawing2d.v1.patch.ReplaceEntityOpV1
import com.example.arweld.core.drawing2d.v1.patch.ReplaceLayerOpV1
import com.example.arweld.core.drawing2d.v1.patch.canonicalize
import com.example.arweld.core.drawing2d.v1.patch.toCanonicalJson
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PatchEventDeterminismTest {

    @Test
    fun `canonical json stable across meta ordering`() {
        val eventA = buildEvent(
            meta = listOf(
                MetaEntryV1(key = "zeta", value = "last"),
                MetaEntryV1(key = "alpha", value = "first")
            )
        )
        val eventB = buildEvent(
            meta = listOf(
                MetaEntryV1(key = "alpha", value = "first"),
                MetaEntryV1(key = "zeta", value = "last")
            )
        )

        assertThat(eventA.canonicalize().meta.map { it.key })
            .isEqualTo(listOf("alpha", "zeta"))
        assertThat(eventA.toCanonicalJson()).isEqualTo(eventB.toCanonicalJson())
    }

    @Test
    fun `canonical encoding is deterministic and decodes polymorphic ops`() {
        val event = buildEvent(
            meta = listOf(
                MetaEntryV1(key = "beta", value = "value-b"),
                MetaEntryV1(key = "alpha", value = "value-a")
            )
        )

        val jsonFirst = event.toCanonicalJson()
        val jsonSecond = event.toCanonicalJson()

        assertThat(jsonFirst).isEqualTo(jsonSecond)

        val decoded = Drawing2DJson.decodeFromString<DrawingPatchEvent>(jsonFirst)
        assertThat(decoded.ops[0]).isInstanceOf(AddEntityOpV1::class.java)
        assertThat(decoded.ops[1]).isInstanceOf(ReplaceEntityOpV1::class.java)
        assertThat(decoded.ops[2]).isInstanceOf(RemoveEntityOpV1::class.java)
        assertThat(decoded.ops[3]).isInstanceOf(AddLayerOpV1::class.java)
        assertThat(decoded.ops[4]).isInstanceOf(ReplaceLayerOpV1::class.java)
        assertThat(decoded.ops[5]).isInstanceOf(RemoveLayerOpV1::class.java)
    }

    private fun buildEvent(meta: List<MetaEntryV1>): DrawingPatchEvent {
        return DrawingPatchEvent(
            eventId = "evt-001",
            drawingId = "drawing-001",
            baseRev = 7,
            ops = listOf(
                AddEntityOpV1(
                    entity = LineV1(
                        id = "entity-1",
                        layerId = "layer-a",
                        a = PointV1(0.0, 0.0),
                        b = PointV1(10.0, 10.0)
                    )
                ),
                ReplaceEntityOpV1(
                    entity = CircleV1(
                        id = "entity-2",
                        layerId = "layer-a",
                        c = PointV1(4.0, 5.0),
                        r = 3.5
                    )
                ),
                RemoveEntityOpV1(entityId = "entity-3"),
                AddLayerOpV1(
                    layer = LayerV1(id = "layer-a", name = "Layer A", order = 1)
                ),
                ReplaceLayerOpV1(
                    layer = LayerV1(id = "layer-b", name = "Layer B", order = 2)
                ),
                RemoveLayerOpV1(layerId = "layer-c")
            ),
            author = "designer@example.com",
            meta = meta
        )
    }
}
