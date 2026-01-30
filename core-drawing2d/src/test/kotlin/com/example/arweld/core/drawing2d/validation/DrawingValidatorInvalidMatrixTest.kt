package com.example.arweld.core.drawing2d.validation

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.v1.AttachmentKindV1
import com.example.arweld.core.drawing2d.v1.AttachmentRefV1
import com.example.arweld.core.drawing2d.v1.Drawing2D
import com.example.arweld.core.drawing2d.v1.LayerV1
import com.example.arweld.core.drawing2d.v1.PageV1
import com.example.arweld.core.drawing2d.v1.PointV1
import com.example.arweld.core.drawing2d.v1.entities.ArcV1
import com.example.arweld.core.drawing2d.v1.entities.CircleV1
import com.example.arweld.core.drawing2d.v1.entities.EntityV1
import com.example.arweld.core.drawing2d.v1.entities.GroupV1
import com.example.arweld.core.drawing2d.v1.entities.LineV1
import com.example.arweld.core.drawing2d.v1.entities.PolylineV1
import com.example.arweld.core.drawing2d.v1.entities.TagV1
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DrawingValidatorInvalidMatrixTest(
    @Suppress("UNUSED_PARAMETER") private val caseName: String,
    private val testCase: InvalidCase
) {

    private val validator = DrawingValidatorV1()

    @Test
    fun `invalid case yields expected canonical violations`() {
        val drawing = testCase.build()

        val violations = validator.validate(drawing)

        val expected = testCase.expected
        val actual = violations.map { ExpectedViolation(it.code, it.path, it.severity) }
        assertThat(actual).containsExactlyElementsIn(expected).inOrder()
    }

    data class ExpectedViolation(
        val code: String,
        val path: String,
        val severity: SeverityV1
    )

    data class InvalidCase(
        val name: String,
        val build: () -> Drawing2D,
        val expected: List<ExpectedViolation>
    )

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): List<Array<Any>> {
            val cases = listOf(
                InvalidCase(
                    name = "schema version mismatch (json)",
                    build = { decodeDrawing(schemaVersionMismatchJson()) },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.SCHEMA_VERSION_MISMATCH,
                            path = PathV1.field(PathV1.root, "schemaVersion"),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "blank drawing id",
                    build = { minimalDrawing(drawingId = "") },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.DRAWING_ID_BLANK,
                            path = PathV1.field(PathV1.root, "drawingId"),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "negative page width and height (json)",
                    build = { decodeDrawing(negativePageJson()) },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.PAGE_HEIGHT_INVALID,
                            path = PathV1.field(PathV1.field(PathV1.root, "page"), "heightPx"),
                            severity = SeverityV1.ERROR
                        ),
                        ExpectedViolation(
                            code = CodesV1.PAGE_WIDTH_INVALID,
                            path = PathV1.field(PathV1.field(PathV1.root, "page"), "widthPx"),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "duplicate layer ids",
                    build = {
                        minimalDrawing(
                            layers = listOf(
                                LayerV1("L1", "Layer 1", 0),
                                LayerV1("L1", "Layer 1 dup", 1)
                            )
                        )
                    },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.LAYER_ID_DUPLICATE,
                            path = PathV1.idSelector(PathV1.root, "layers", "L1"),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "duplicate entity ids",
                    build = {
                        minimalDrawing(
                            entities = listOf(
                                LineV1(
                                    id = "E1",
                                    layerId = "L1",
                                    a = PointV1(0.0, 0.0),
                                    b = PointV1(1.0, 1.0)
                                ),
                                LineV1(
                                    id = "E1",
                                    layerId = "L1",
                                    a = PointV1(2.0, 2.0),
                                    b = PointV1(3.0, 3.0)
                                )
                            )
                        )
                    },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.ENTITY_ID_DUPLICATE,
                            path = PathV1.idSelector(PathV1.root, "entities", "E1"),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "entity refers missing layer",
                    build = {
                        minimalDrawing(
                            entities = listOf(
                                LineV1(
                                    id = "E1",
                                    layerId = "MISSING",
                                    a = PointV1(0.0, 0.0),
                                    b = PointV1(1.0, 1.0)
                                )
                            )
                        )
                    },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.ENTITY_LAYER_UNKNOWN,
                            path = PathV1.field(
                                PathV1.idSelector(PathV1.root, "entities", "E1"),
                                "layerId"
                            ),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "group member missing",
                    build = {
                        minimalDrawing(
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
                    },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.GROUP_MEMBER_UNKNOWN,
                            path = PathV1.field(
                                PathV1.idSelector(PathV1.root, "entities", "G1"),
                                "members"
                            ),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "tag target missing",
                    build = {
                        minimalDrawing(
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
                    },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.TAG_TARGET_UNKNOWN,
                            path = PathV1.field(
                                PathV1.idSelector(PathV1.root, "entities", "T1"),
                                "targetId"
                            ),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "attachment relPath has dotdot",
                    build = {
                        minimalDrawing(
                            attachments = listOf(
                                AttachmentRefV1(
                                    kind = AttachmentKindV1.RAW_IMAGE,
                                    relPath = "../bad.png"
                                )
                            )
                        )
                    },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.ATTACHMENT_PATH_INVALID,
                            path = PathV1.field(PathV1.index(PathV1.root, "attachments", 0), "relPath"),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "non-finite point",
                    build = {
                        minimalDrawing(
                            entities = listOf(
                                LineV1(
                                    id = "E1",
                                    layerId = "L1",
                                    a = PointV1(Double.NaN, 0.0),
                                    b = PointV1(1.0, 1.0)
                                )
                            )
                        )
                    },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.NUM_NOT_FINITE,
                            path = PathV1.field(
                                PathV1.field(PathV1.idSelector(PathV1.root, "entities", "E1"), "a"),
                                "x"
                            ),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "non-finite radius",
                    build = {
                        minimalDrawing(
                            entities = listOf(
                                CircleV1(
                                    id = "C1",
                                    layerId = "L1",
                                    c = PointV1(0.0, 0.0),
                                    r = Double.POSITIVE_INFINITY
                                )
                            )
                        )
                    },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.NUM_NOT_FINITE,
                            path = PathV1.field(
                                PathV1.idSelector(PathV1.root, "entities", "C1"),
                                "r"
                            ),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "polyline too few points",
                    build = {
                        minimalDrawing(
                            entities = listOf(
                                PolylineV1(
                                    id = "P1",
                                    layerId = "L1",
                                    points = listOf(PointV1(0.0, 0.0)),
                                    closed = false
                                )
                            )
                        )
                    },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.POLYLINE_TOO_FEW_POINTS,
                            path = PathV1.field(
                                PathV1.idSelector(PathV1.root, "entities", "P1"),
                                "points"
                            ),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "closed polyline too few points",
                    build = {
                        minimalDrawing(
                            entities = listOf(
                                PolylineV1(
                                    id = "P2",
                                    layerId = "L1",
                                    points = listOf(PointV1(0.0, 0.0), PointV1(1.0, 1.0)),
                                    closed = true
                                )
                            )
                        )
                    },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.CLOSED_POLYLINE_TOO_FEW_POINTS,
                            path = PathV1.field(
                                PathV1.idSelector(PathV1.root, "entities", "P2"),
                                "points"
                            ),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "circle radius non-positive",
                    build = {
                        minimalDrawing(
                            entities = listOf(
                                CircleV1(
                                    id = "C2",
                                    layerId = "L1",
                                    c = PointV1(0.0, 0.0),
                                    r = 0.0
                                )
                            )
                        )
                    },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.RADIUS_NON_POSITIVE,
                            path = PathV1.field(
                                PathV1.idSelector(PathV1.root, "entities", "C2"),
                                "r"
                            ),
                            severity = SeverityV1.ERROR
                        )
                    )
                ),
                InvalidCase(
                    name = "arc angle non-finite",
                    build = {
                        minimalDrawing(
                            entities = listOf(
                                ArcV1(
                                    id = "A1",
                                    layerId = "L1",
                                    c = PointV1(0.0, 0.0),
                                    r = 10.0,
                                    startAngleDeg = Double.NaN,
                                    endAngleDeg = 90.0,
                                    cw = false
                                )
                            )
                        )
                    },
                    expected = listOf(
                        ExpectedViolation(
                            code = CodesV1.NUM_NOT_FINITE,
                            path = PathV1.field(
                                PathV1.idSelector(PathV1.root, "entities", "A1"),
                                "startAngleDeg"
                            ),
                            severity = SeverityV1.ERROR
                        )
                    )
                )
            )

            return cases.map { arrayOf(it.name, it) }
        }

        private fun minimalDrawing(
            schemaVersion: Int = 1,
            drawingId: String = "D1",
            rev: Long = 0,
            page: PageV1 = PageV1(widthPx = 100, heightPx = 100),
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
                page = page,
                layers = layers,
                entities = entities,
                attachments = attachments
            )
        }

        private fun decodeDrawing(json: String): Drawing2D {
            return Drawing2DJson.decodeFromString(json)
        }

        private fun schemaVersionMismatchJson(): String {
            return """
                {
                  "schemaVersion": 2,
                  "drawingId": "drawing-json-1",
                  "rev": 0,
                  "page": { "widthPx": 100, "heightPx": 100 },
                  "layers": [
                    { "id": "L1", "name": "Layer 1", "order": 0 }
                  ],
                  "entities": [
                    {
                      "type": "line",
                      "id": "E1",
                      "layerId": "L1",
                      "a": { "x": 0.0, "y": 0.0 },
                      "b": { "x": 1.0, "y": 1.0 }
                    }
                  ]
                }
            """.trimIndent()
        }

        private fun negativePageJson(): String {
            return """
                {
                  "schemaVersion": 1,
                  "drawingId": "drawing-json-2",
                  "rev": 0,
                  "page": { "widthPx": -10, "heightPx": -5 },
                  "layers": [
                    { "id": "L1", "name": "Layer 1", "order": 0 }
                  ],
                  "entities": [
                    {
                      "type": "line",
                      "id": "E1",
                      "layerId": "L1",
                      "a": { "x": 0.0, "y": 0.0 },
                      "b": { "x": 1.0, "y": 1.0 }
                    }
                  ]
                }
            """.trimIndent()
        }
    }
}
