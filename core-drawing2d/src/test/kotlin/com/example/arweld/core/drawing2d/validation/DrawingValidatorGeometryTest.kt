package com.example.arweld.core.drawing2d.validation

import com.example.arweld.core.drawing2d.v1.Drawing2D
import com.example.arweld.core.drawing2d.v1.LayerV1
import com.example.arweld.core.drawing2d.v1.PageV1
import com.example.arweld.core.drawing2d.v1.PointV1
import com.example.arweld.core.drawing2d.v1.entities.ArcV1
import com.example.arweld.core.drawing2d.v1.entities.CircleV1
import com.example.arweld.core.drawing2d.v1.entities.EntityV1
import com.example.arweld.core.drawing2d.v1.entities.LineV1
import com.example.arweld.core.drawing2d.v1.entities.PolylineV1
import com.example.arweld.core.drawing2d.v1.entities.TextV1
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DrawingValidatorGeometryTest {

    private val validator = DrawingValidatorV1()

    @Test
    fun `non-finite point reports violation path`() {
        val drawing = minimalDrawing(
            entities = listOf(
                LineV1(
                    id = "L1",
                    layerId = "LAYER",
                    a = PointV1(Double.NaN, 0.0),
                    b = PointV1(1.0, 2.0)
                )
            )
        )

        val violations = validator.validate(drawing)

        val violation = violations.first { it.code == CodesV1.NUM_NOT_FINITE }
        assertThat(violation.path).isEqualTo("$.entities[id=L1].a.x")
    }

    @Test
    fun `infinite radius reports non-finite`() {
        val drawing = minimalDrawing(
            entities = listOf(
                CircleV1(
                    id = "C1",
                    layerId = "LAYER",
                    c = PointV1(0.0, 0.0),
                    r = Double.POSITIVE_INFINITY
                )
            )
        )

        val violations = validator.validate(drawing)

        assertThat(violations).contains(
            ViolationV1(
                code = CodesV1.NUM_NOT_FINITE,
                severity = SeverityV1.ERROR,
                path = "$.entities[id=C1].r",
                message = "numeric value must be finite"
            )
        )
    }

    @Test
    fun `zero radius reports non-positive`() {
        val drawing = minimalDrawing(
            entities = listOf(
                CircleV1(
                    id = "C1",
                    layerId = "LAYER",
                    c = PointV1(0.0, 0.0),
                    r = 0.0
                )
            )
        )

        val violations = validator.validate(drawing)

        assertThat(violations.map { it.code }).contains(CodesV1.RADIUS_NON_POSITIVE)
    }

    @Test
    fun `polyline with one point reports error`() {
        val drawing = minimalDrawing(
            entities = listOf(
                PolylineV1(
                    id = "P1",
                    layerId = "LAYER",
                    points = listOf(PointV1(0.0, 0.0)),
                    closed = false
                )
            )
        )

        val violations = validator.validate(drawing)

        assertThat(violations).contains(
            ViolationV1(
                code = CodesV1.POLYLINE_TOO_FEW_POINTS,
                severity = SeverityV1.ERROR,
                path = "$.entities[id=P1].points",
                message = "polyline must have at least 2 points",
                refs = listOf("P1")
            )
        )
    }

    @Test
    fun `closed polyline with two points reports error`() {
        val drawing = minimalDrawing(
            entities = listOf(
                PolylineV1(
                    id = "P1",
                    layerId = "LAYER",
                    points = listOf(PointV1(0.0, 0.0), PointV1(1.0, 1.0)),
                    closed = true
                )
            )
        )

        val violations = validator.validate(drawing)

        assertThat(violations.map { it.code }).contains(CodesV1.CLOSED_POLYLINE_TOO_FEW_POINTS)
    }

    @Test
    fun `arc with non-finite angle reports path`() {
        val drawing = minimalDrawing(
            entities = listOf(
                ArcV1(
                    id = "A1",
                    layerId = "LAYER",
                    c = PointV1(0.0, 0.0),
                    r = 10.0,
                    startAngleDeg = Double.NaN,
                    endAngleDeg = 90.0,
                    cw = true
                )
            )
        )

        val violations = validator.validate(drawing)

        val violation = violations.first { it.code == CodesV1.NUM_NOT_FINITE }
        assertThat(violation.path).isEqualTo("$.entities[id=A1].startAngleDeg")
    }

    @Test
    fun `violations are returned in canonical order`() {
        val drawing = minimalDrawing(
            entities = listOf(
                TextV1(
                    id = "T1",
                    layerId = "LAYER",
                    anchor = PointV1(Double.POSITIVE_INFINITY, 0.0),
                    value = "note",
                    rotationDeg = Double.NaN
                ),
                CircleV1(
                    id = "C1",
                    layerId = "LAYER",
                    c = PointV1(0.0, 0.0),
                    r = 0.0
                )
            )
        )

        val violations = validator.validate(drawing)

        assertThat(violations).containsExactlyElementsIn(violations.canonicalSorted()).inOrder()
    }

    private fun minimalDrawing(
        entities: List<EntityV1>
    ): Drawing2D {
        return Drawing2D(
            schemaVersion = 1,
            drawingId = "D1",
            rev = 0,
            page = PageV1(widthPx = 100, heightPx = 100),
            layers = listOf(LayerV1("LAYER", "Layer", 0)),
            entities = entities,
            attachments = emptyList()
        )
    }
}
