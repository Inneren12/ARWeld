package com.example.arweld.core.drawing2d.validation

import com.example.arweld.core.drawing2d.v1.AttachmentKindV1
import com.example.arweld.core.drawing2d.v1.AttachmentRefV1
import com.example.arweld.core.drawing2d.v1.Drawing2D
import com.example.arweld.core.drawing2d.v1.LayerV1
import com.example.arweld.core.drawing2d.v1.PageV1
import com.example.arweld.core.drawing2d.v1.PointV1
import com.example.arweld.core.drawing2d.v1.entities.EntityV1
import com.example.arweld.core.drawing2d.v1.entities.GroupV1
import com.example.arweld.core.drawing2d.v1.entities.LineV1
import com.example.arweld.core.drawing2d.v1.entities.TagV1
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DrawingValidatorCoreTest {

    private val validator = DrawingValidatorV1()

    @Test
    fun `valid minimal drawing yields no violations`() {
        val drawing = minimalDrawing()

        val violations = validator.validate(drawing)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `schema version mismatch reports error`() {
        val drawing = minimalDrawing(schemaVersion = 2)

        val violations = validator.validate(drawing)

        assertThat(violations.map { it.code }).contains(CodesV1.SCHEMA_VERSION_MISMATCH)
    }

    @Test
    fun `blank drawing id reports error`() {
        val drawing = minimalDrawing(drawingId = " ")

        val violations = validator.validate(drawing)

        assertThat(violations.map { it.code }).contains(CodesV1.DRAWING_ID_BLANK)
    }

    @Test
    fun `missing layers reports error`() {
        val drawing = minimalDrawing(layers = emptyList())

        val violations = validator.validate(drawing)

        assertThat(violations.map { it.code }).contains(CodesV1.LAYERS_EMPTY)
    }

    @Test
    fun `entity references unknown layer reports error`() {
        val drawing = minimalDrawing(
            entities = listOf(
                LineV1(
                    id = "E1",
                    layerId = "missing",
                    a = PointV1(0.0, 0.0),
                    b = PointV1(1.0, 1.0)
                )
            )
        )

        val violations = validator.validate(drawing)

        assertThat(violations.map { it.code }).contains(CodesV1.ENTITY_LAYER_UNKNOWN)
    }

    @Test
    fun `group member missing entity reports error`() {
        val drawing = minimalDrawing(
            entities = listOf(
                LineV1(
                    id = "E1",
                    layerId = "L1",
                    a = PointV1(0.0, 0.0),
                    b = PointV1(1.0, 1.0)
                ),
                GroupV1(
                    id = "G1",
                    layerId = "L1",
                    members = listOf("MISSING")
                )
            )
        )

        val violations = validator.validate(drawing)

        assertThat(violations.map { it.code }).contains(CodesV1.GROUP_MEMBER_UNKNOWN)
    }

    @Test
    fun `tag target missing reports error`() {
        val drawing = minimalDrawing(
            entities = listOf(
                TagV1(
                    id = "T1",
                    layerId = "L1",
                    targetId = "MISSING",
                    key = "k",
                    value = "v"
                )
            )
        )

        val violations = validator.validate(drawing)

        assertThat(violations.map { it.code }).contains(CodesV1.TAG_TARGET_UNKNOWN)
    }

    @Test
    fun `attachment relPath with dotdot reports error`() {
        val drawing = minimalDrawing(
            attachments = listOf(
                AttachmentRefV1(
                    kind = AttachmentKindV1.RAW_IMAGE,
                    relPath = "../bad.png"
                )
            )
        )

        val violations = validator.validate(drawing)

        assertThat(violations.map { it.code }).contains(CodesV1.ATTACHMENT_PATH_INVALID)
    }

    @Test
    fun `violations are returned in canonical order`() {
        val drawing = minimalDrawing(
            schemaVersion = 2,
            drawingId = "",
            layers = emptyList(),
            attachments = listOf(
                AttachmentRefV1(
                    kind = AttachmentKindV1.RAW_IMAGE,
                    relPath = "ok.png",
                    sha256 = "not-hex"
                )
            )
        )

        val violations = validator.validate(drawing)

        assertThat(violations).containsExactlyElementsIn(violations.canonicalSorted()).inOrder()
    }

    private fun minimalDrawing(
        schemaVersion: Int = 1,
        drawingId: String = "D1",
        rev: Long = 0,
        layers: List<LayerV1> = listOf(LayerV1("L1", "Layer 1", 0)),
        entities: List<EntityV1> = listOf(
            LineV1(
                id = "E1",
                layerId = "L1",
                a = PointV1(0.0, 0.0),
                b = PointV1(10.0, 10.0)
            )
        ),
        attachments: List<AttachmentRefV1> = emptyList()
    ): Drawing2D {
        return Drawing2D(
            schemaVersion = schemaVersion,
            drawingId = drawingId,
            rev = rev,
            page = PageV1(widthPx = 100, heightPx = 100),
            layers = layers,
            entities = entities,
            attachments = attachments
        )
    }
}
