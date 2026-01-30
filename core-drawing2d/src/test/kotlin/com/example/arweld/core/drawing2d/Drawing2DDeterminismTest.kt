package com.example.arweld.core.drawing2d

import com.example.arweld.core.drawing2d.v1.AttachmentKindV1
import com.example.arweld.core.drawing2d.v1.AttachmentRefV1
import com.example.arweld.core.drawing2d.v1.Drawing2D
import com.example.arweld.core.drawing2d.v1.LayerV1
import com.example.arweld.core.drawing2d.v1.PageV1
import com.example.arweld.core.drawing2d.v1.PointV1
import com.example.arweld.core.drawing2d.v1.entities.CircleV1
import com.example.arweld.core.drawing2d.v1.entities.EntityV1
import com.example.arweld.core.drawing2d.v1.entities.LineV1
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Drawing2DDeterminismTest {

    @Test
    fun `canonicalize sorts lists deterministically`() {
        val layersA = listOf(
            LayerV1(id = "layer-b", name = "Layer B", order = 2),
            LayerV1(id = "layer-a", name = "Layer A", order = 1)
        )
        val layersB = listOf(
            LayerV1(id = "layer-a", name = "Layer A", order = 1),
            LayerV1(id = "layer-b", name = "Layer B", order = 2)
        )

        val entitiesA: List<EntityV1> = listOf(
            LineV1(
                id = "entity-2",
                layerId = "layer-b",
                a = PointV1(0.0, 0.0),
                b = PointV1(10.0, 10.0)
            ),
            CircleV1(
                id = "entity-1",
                layerId = "layer-a",
                c = PointV1(4.0, 5.0),
                r = 3.5
            )
        )
        val entitiesB: List<EntityV1> = listOf(
            CircleV1(
                id = "entity-1",
                layerId = "layer-a",
                c = PointV1(4.0, 5.0),
                r = 3.5
            ),
            LineV1(
                id = "entity-2",
                layerId = "layer-b",
                a = PointV1(0.0, 0.0),
                b = PointV1(10.0, 10.0)
            )
        )

        val attachmentsA = listOf(
            AttachmentRefV1(kind = AttachmentKindV1.OVERLAY, relPath = "overlay/z.png"),
            AttachmentRefV1(kind = AttachmentKindV1.RAW_IMAGE, relPath = "raw/a.png"),
            AttachmentRefV1(kind = AttachmentKindV1.RAW_IMAGE, relPath = "raw/b.png")
        )
        val attachmentsB = listOf(
            AttachmentRefV1(kind = AttachmentKindV1.RAW_IMAGE, relPath = "raw/b.png"),
            AttachmentRefV1(kind = AttachmentKindV1.OVERLAY, relPath = "overlay/z.png"),
            AttachmentRefV1(kind = AttachmentKindV1.RAW_IMAGE, relPath = "raw/a.png")
        )

        val drawingA = buildDrawing(layersA, entitiesA, attachmentsA)
        val drawingB = buildDrawing(layersB, entitiesB, attachmentsB)

        assertThat(drawingA.canonicalize()).isEqualTo(drawingB.canonicalize())
        assertThat(drawingA.toCanonicalJson()).isEqualTo(drawingB.toCanonicalJson())
    }

    @Test
    fun `canonical json roundtrip matches canonical object`() {
        val drawing = buildDrawing(
            layers = listOf(
                LayerV1(id = "layer-a", name = "Layer A", order = 1)
            ),
            entities = listOf(
                LineV1(
                    id = "entity-1",
                    layerId = "layer-a",
                    a = PointV1(1.0, 2.0),
                    b = PointV1(3.0, 4.0)
                )
            ),
            attachments = listOf(
                AttachmentRefV1(kind = AttachmentKindV1.DRAWING2D_JSON, relPath = "drawing2d.json")
            )
        )

        val canonical = drawing.canonicalize()
        val json = Drawing2DJson.encodeToString(canonical)
        val decoded = Drawing2DJson.decodeFromString<Drawing2D>(json)

        assertThat(decoded).isEqualTo(canonical)
    }

    private fun buildDrawing(
        layers: List<LayerV1>,
        entities: List<EntityV1>,
        attachments: List<AttachmentRefV1>
    ): Drawing2D {
        return Drawing2D(
            drawingId = "drawing-001",
            rev = 42,
            page = PageV1(widthPx = 1920, heightPx = 1080),
            layers = layers,
            entities = entities,
            attachments = attachments
        )
    }
}
