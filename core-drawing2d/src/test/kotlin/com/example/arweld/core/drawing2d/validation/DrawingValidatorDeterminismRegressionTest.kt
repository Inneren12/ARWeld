package com.example.arweld.core.drawing2d.validation

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.v1.Drawing2D
import com.example.arweld.core.drawing2d.v1.LayerV1
import com.example.arweld.core.drawing2d.v1.PageV1
import com.example.arweld.core.drawing2d.v1.PointV1
import com.example.arweld.core.drawing2d.v1.entities.CircleV1
import com.example.arweld.core.drawing2d.v1.entities.EntityV1
import com.example.arweld.core.drawing2d.v1.entities.PolylineV1
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.Serializable
import org.junit.Test

class DrawingValidatorDeterminismRegressionTest {

    private val validator = DrawingValidatorV1()

    @Test
    fun `different insertion order yields identical canonical violations`() {
        val layersA = listOf(
            LayerV1("layer-b", "Layer B", 1),
            LayerV1("layer-a", "Layer A", 0)
        )
        val layersB = listOf(
            LayerV1("layer-a", "Layer A", 0),
            LayerV1("layer-b", "Layer B", 1)
        )

        val entitiesA: List<EntityV1> = listOf(
            PolylineV1(
                id = "poly-1",
                layerId = "layer-a",
                points = listOf(PointV1(0.0, 0.0)),
                closed = false
            ),
            CircleV1(
                id = "circle-1",
                layerId = "missing-layer",
                c = PointV1(1.0, 1.0),
                r = 0.0
            )
        )
        val entitiesB: List<EntityV1> = listOf(
            CircleV1(
                id = "circle-1",
                layerId = "missing-layer",
                c = PointV1(1.0, 1.0),
                r = 0.0
            ),
            PolylineV1(
                id = "poly-1",
                layerId = "layer-a",
                points = listOf(PointV1(0.0, 0.0)),
                closed = false
            )
        )

        val drawingA = invalidDrawing(layersA, entitiesA)
        val drawingB = invalidDrawing(layersB, entitiesB)

        val violationsA = validator.validate(drawingA)
        val violationsB = validator.validate(drawingB)

        assertThat(violationsA).isEqualTo(violationsB)
        assertThat(violationsA.toSnapshots()).isEqualTo(violationsB.toSnapshots())
    }

    @Test
    fun `repeated validation yields identical outputs`() {
        val drawing = invalidDrawing(
            layers = listOf(LayerV1("layer-a", "Layer A", 0)),
            entities = listOf(
                PolylineV1(
                    id = "poly-1",
                    layerId = "layer-a",
                    points = listOf(PointV1(0.0, 0.0)),
                    closed = false
                ),
                CircleV1(
                    id = "circle-1",
                    layerId = "missing-layer",
                    c = PointV1(1.0, 1.0),
                    r = 0.0
                )
            )
        )

        val first = validator.validate(drawing)
        val second = validator.validate(drawing)

        assertThat(first).isEqualTo(second)
        assertThat(encodeSnapshots(first)).isEqualTo(encodeSnapshots(second))
    }

    private fun invalidDrawing(layers: List<LayerV1>, entities: List<EntityV1>): Drawing2D {
        return Drawing2D(
            schemaVersion = 1,
            drawingId = "determinism-drawing",
            rev = 0,
            page = PageV1(widthPx = 200, heightPx = 100),
            layers = layers,
            entities = entities,
            attachments = emptyList()
        )
    }

    private fun List<ViolationV1>.toSnapshots(): List<ViolationSnapshot> {
        return map { ViolationSnapshot(it.code, it.path, it.severity.name) }
    }

    private fun encodeSnapshots(violations: List<ViolationV1>): String {
        return Drawing2DJson.encodeToString(violations.toSnapshots())
    }

    @Serializable
    data class ViolationSnapshot(
        val code: String,
        val path: String,
        val severity: String
    )
}
