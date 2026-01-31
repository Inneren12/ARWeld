package com.example.arweld.feature.drawingimport.overlay

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.example.arweld.feature.drawingimport.preprocess.OrderedCornersV1
import kotlin.math.max
import kotlin.math.min

class CornerOverlayRendererV1 {
    fun render(
        baseBitmap: Bitmap,
        ordered: OrderedCornersV1,
        refined: OrderedCornersV1? = null,
    ): Bitmap {
        val bitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val width = bitmap.width
        val height = bitmap.height
        val stroke = max(2f, width * 0.0045f)
        val markerRadius = max(4f, width * 0.012f)
        val refinedRadius = markerRadius * 0.65f
        val labelOffset = markerRadius + stroke * 1.5f
        val labelTextSize = max(12f, width * 0.045f)

        val orderedColor = 0xFF00BCD4.toInt()
        val refinedColor = 0xFFFFC107.toInt()

        val orderedStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = orderedColor
            style = Paint.Style.STROKE
            strokeWidth = stroke
        }
        val orderedFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = orderedColor
            style = Paint.Style.FILL
        }
        val refinedStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = refinedColor
            style = Paint.Style.STROKE
            strokeWidth = stroke * 0.75f
        }
        val refinedFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = refinedColor
            style = Paint.Style.FILL
        }
        val connectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x80FFC107.toInt()
            style = Paint.Style.STROKE
            strokeWidth = stroke * 0.6f
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = labelTextSize
            style = Paint.Style.FILL
            setShadowLayer(2f, 1f, 1f, 0xFF000000.toInt())
        }

        val orderedPoints = ordered.toRenderPoints(width, height)
        val quadPath = Path().apply {
            moveTo(orderedPoints[0].x, orderedPoints[0].y)
            lineTo(orderedPoints[1].x, orderedPoints[1].y)
            lineTo(orderedPoints[2].x, orderedPoints[2].y)
            lineTo(orderedPoints[3].x, orderedPoints[3].y)
            close()
        }
        canvas.drawPath(quadPath, orderedStroke)
        drawLabeledCorners(
            canvas = canvas,
            points = orderedPoints,
            labels = listOf("TL", "TR", "BR", "BL"),
            radius = markerRadius,
            fill = orderedFill,
            labelPaint = labelPaint,
            labelOffset = labelOffset,
        )

        refined?.let {
            val refinedPoints = it.toRenderPoints(width, height)
            for (i in refinedPoints.indices) {
                canvas.drawLine(
                    orderedPoints[i].x,
                    orderedPoints[i].y,
                    refinedPoints[i].x,
                    refinedPoints[i].y,
                    connectorPaint,
                )
            }
            drawLabeledCorners(
                canvas = canvas,
                points = refinedPoints,
                labels = listOf("TL", "TR", "BR", "BL"),
                radius = refinedRadius,
                fill = refinedFill,
                labelPaint = labelPaint,
                labelOffset = labelOffset * 0.85f,
            )
            val refinedPath = Path().apply {
                moveTo(refinedPoints[0].x, refinedPoints[0].y)
                lineTo(refinedPoints[1].x, refinedPoints[1].y)
                lineTo(refinedPoints[2].x, refinedPoints[2].y)
                lineTo(refinedPoints[3].x, refinedPoints[3].y)
                close()
            }
            canvas.drawPath(refinedPath, refinedStroke)
        }

        return bitmap
    }

    private data class RenderPoint(
        val x: Float,
        val y: Float,
    )

    private fun OrderedCornersV1.toRenderPoints(width: Int, height: Int): List<RenderPoint> {
        return listOf(topLeft, topRight, bottomRight, bottomLeft).map { point ->
            RenderPoint(
                x = clamp(point.x, width),
                y = clamp(point.y, height),
            )
        }
    }

    private fun clamp(value: Double, maxValue: Int): Float {
        if (maxValue <= 1) return 0f
        val clamped = min(max(value, 0.0), (maxValue - 1).toDouble())
        return clamped.toFloat()
    }

    private fun drawLabeledCorners(
        canvas: Canvas,
        points: List<RenderPoint>,
        labels: List<String>,
        radius: Float,
        fill: Paint,
        labelPaint: Paint,
        labelOffset: Float,
    ) {
        points.forEachIndexed { index, point ->
            canvas.drawCircle(point.x, point.y, radius, fill)
            canvas.drawText(
                labels[index],
                point.x + labelOffset,
                point.y - labelOffset,
                labelPaint,
            )
        }
    }
}
