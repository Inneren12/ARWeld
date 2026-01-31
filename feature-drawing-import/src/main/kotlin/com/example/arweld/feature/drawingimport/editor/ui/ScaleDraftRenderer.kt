package com.example.arweld.feature.drawingimport.editor.ui

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arweld.feature.drawingimport.editor.ScaleDraft
import com.example.arweld.feature.drawingimport.editor.Vec2
import com.example.arweld.feature.drawingimport.editor.ViewTransform

fun DrawScope.drawScaleDraft(
    draft: ScaleDraft,
    viewTransform: ViewTransform,
) {
    val pointA = draft.pointA
    val pointB = draft.pointB
    val lineColor = Color(0xFF1C6DD0)

    if (pointA != null && pointB != null) {
        val screenA = viewTransform.worldToScreen(pointA).toOffset()
        val screenB = viewTransform.worldToScreen(pointB).toOffset()
        drawLine(
            color = lineColor,
            start = screenA,
            end = screenB,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }

    if (pointA != null) {
        drawMarker("A", pointA, lineColor, viewTransform)
    }
    if (pointB != null) {
        drawMarker("B", pointB, lineColor, viewTransform)
    }
}

private fun DrawScope.drawMarker(
    label: String,
    point: Vec2,
    color: Color,
    viewTransform: ViewTransform,
) {
    val center = viewTransform.worldToScreen(point).toOffset()
    val outerRadius = 7.dp.toPx()
    val innerRadius = 5.dp.toPx()
    drawCircle(
        color = Color.White,
        radius = outerRadius,
        center = center,
    )
    drawCircle(
        color = color,
        radius = innerRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx()),
    )

    val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 12.sp.toPx()
        this.color = color.toArgb()
    }
    val labelOffset = Offset(center.x + 10.dp.toPx(), center.y - 8.dp.toPx())
    drawContext.canvas.nativeCanvas.drawText(label, labelOffset.x, labelOffset.y, textPaint)
}

private fun Vec2.toOffset(): Offset = Offset(x, y)
