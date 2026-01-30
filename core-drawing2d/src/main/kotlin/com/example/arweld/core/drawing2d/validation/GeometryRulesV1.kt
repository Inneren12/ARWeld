package com.example.arweld.core.drawing2d.validation

import com.example.arweld.core.drawing2d.v1.Drawing2D
import com.example.arweld.core.drawing2d.v1.PointV1
import com.example.arweld.core.drawing2d.v1.entities.ArcV1
import com.example.arweld.core.drawing2d.v1.entities.CircleV1
import com.example.arweld.core.drawing2d.v1.entities.DimensionV1
import com.example.arweld.core.drawing2d.v1.entities.EntityV1
import com.example.arweld.core.drawing2d.v1.entities.LineV1
import com.example.arweld.core.drawing2d.v1.entities.PolylineV1
import com.example.arweld.core.drawing2d.v1.entities.TextV1

internal object GeometryRulesV1 {
    private const val MIN_POLYLINE_POINTS = 2
    private const val MIN_CLOSED_POLYLINE_POINTS = 3

    fun validate(drawing: Drawing2D, violations: MutableList<ViolationV1>) {
        drawing.entities.forEach { entity ->
            when (entity) {
                is LineV1 -> validateLine(entity, violations)
                is PolylineV1 -> validatePolyline(entity, violations)
                is CircleV1 -> validateCircle(entity, violations)
                is ArcV1 -> validateArc(entity, violations)
                is DimensionV1 -> validateDimension(entity, violations)
                is TextV1 -> validateText(entity, violations)
                else -> Unit
            }
        }
    }

    private fun validateLine(line: LineV1, violations: MutableList<ViolationV1>) {
        val basePath = entityPath(line)
        val aPath = PathV1.field(basePath, "a")
        val bPath = PathV1.field(basePath, "b")
        validatePointFinite(line.a, aPath, violations)
        validatePointFinite(line.b, bPath, violations)
        if (line.a == line.b) {
            violations.addWarn(
                code = CodesV1.DEGENERATE_LINE,
                path = basePath,
                message = "line endpoints should not be identical",
                refs = listOf(line.id)
            )
        }
    }

    private fun validatePolyline(polyline: PolylineV1, violations: MutableList<ViolationV1>) {
        val basePath = entityPath(polyline)
        val pointsPath = PathV1.field(basePath, "points")
        if (polyline.points.size < MIN_POLYLINE_POINTS) {
            violations.addError(
                code = CodesV1.POLYLINE_TOO_FEW_POINTS,
                path = pointsPath,
                message = "polyline must have at least $MIN_POLYLINE_POINTS points",
                refs = listOf(polyline.id)
            )
        }
        if (polyline.closed && polyline.points.size < MIN_CLOSED_POLYLINE_POINTS) {
            violations.addError(
                code = CodesV1.CLOSED_POLYLINE_TOO_FEW_POINTS,
                path = pointsPath,
                message = "closed polyline must have at least $MIN_CLOSED_POLYLINE_POINTS points",
                refs = listOf(polyline.id)
            )
        }
        polyline.points.forEachIndexed { index, point ->
            val pointPath = PathV1.index(basePath, "points", index)
            validatePointFinite(point, pointPath, violations)
        }
    }

    private fun validateCircle(circle: CircleV1, violations: MutableList<ViolationV1>) {
        val basePath = entityPath(circle)
        validatePointFinite(circle.c, PathV1.field(basePath, "c"), violations)
        validateRadius(circle.r, PathV1.field(basePath, "r"), violations)
    }

    private fun validateArc(arc: ArcV1, violations: MutableList<ViolationV1>) {
        val basePath = entityPath(arc)
        validatePointFinite(arc.c, PathV1.field(basePath, "c"), violations)
        validateRadius(arc.r, PathV1.field(basePath, "r"), violations)
        validateFinite(arc.startAngleDeg, PathV1.field(basePath, "startAngleDeg"), violations)
        validateFinite(arc.endAngleDeg, PathV1.field(basePath, "endAngleDeg"), violations)
        // v1 intentionally does not clamp angle ranges; only finite values are enforced.
    }

    private fun validateDimension(dimension: DimensionV1, violations: MutableList<ViolationV1>) {
        val basePath = entityPath(dimension)
        validatePointFinite(dimension.p1, PathV1.field(basePath, "p1"), violations)
        validatePointFinite(dimension.p2, PathV1.field(basePath, "p2"), violations)
        dimension.offsetPx?.let { offset ->
            validateFinite(offset, PathV1.field(basePath, "offsetPx"), violations)
        }
    }

    private fun validateText(text: TextV1, violations: MutableList<ViolationV1>) {
        val basePath = entityPath(text)
        validatePointFinite(text.anchor, PathV1.field(basePath, "anchor"), violations)
        validateFinite(text.rotationDeg, PathV1.field(basePath, "rotationDeg"), violations)
    }

    private fun validateRadius(value: Double, path: String, violations: MutableList<ViolationV1>) {
        validateFinite(value, path, violations)
        if (value.isFiniteStrict() && value <= 0.0) {
            violations.addError(
                code = CodesV1.RADIUS_NON_POSITIVE,
                path = path,
                message = "radius must be > 0"
            )
        }
    }

    private fun validateFinite(value: Double, path: String, violations: MutableList<ViolationV1>) {
        if (!value.isFiniteStrict()) {
            violations.addError(
                code = CodesV1.NUM_NOT_FINITE,
                path = path,
                message = "numeric value must be finite"
            )
        }
    }

    private fun validatePointFinite(point: PointV1, path: String, violations: MutableList<ViolationV1>) {
        if (!point.x.isFiniteStrict()) {
            violations.addError(
                code = CodesV1.NUM_NOT_FINITE,
                path = PathV1.field(path, "x"),
                message = "numeric value must be finite"
            )
        }
        if (!point.y.isFiniteStrict()) {
            violations.addError(
                code = CodesV1.NUM_NOT_FINITE,
                path = PathV1.field(path, "y"),
                message = "numeric value must be finite"
            )
        }
    }

    private fun entityPath(entity: EntityV1): String =
        PathV1.idSelector(PathV1.root, "entities", entity.id)
}
