package com.example.arweld.feature.drawingeditor.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.core.drawing2d.editor.v1.missingNodeReferences
import com.example.arweld.feature.drawingeditor.viewmodel.EditorState
import com.example.arweld.feature.drawingeditor.viewmodel.EditorTool
import com.example.arweld.feature.drawingeditor.viewmodel.Point2
import com.example.arweld.feature.drawingeditor.viewmodel.ScaleDraft
import com.example.arweld.feature.drawingeditor.viewmodel.ScaleStatus
import com.example.arweld.feature.drawingeditor.viewmodel.ScaleStatusDisplay
import com.example.arweld.feature.drawingeditor.viewmodel.ViewTransform
import com.example.arweld.feature.drawingeditor.viewmodel.deriveScaleStatus
import com.example.arweld.feature.drawingeditor.viewmodel.formatScaleLengthMm
import com.example.arweld.feature.drawingeditor.viewmodel.formatScaleMmPerPx
import com.example.arweld.feature.drawingeditor.viewmodel.formatScaleValue
import com.example.arweld.feature.drawingeditor.viewmodel.worldToScreen

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ManualEditorScreen(
    uiState: EditorState,
    onToolSelected: (EditorTool) -> Unit,
    onScalePointSelected: (Point2D) -> Unit,
    onScaleLengthChanged: (String) -> Unit,
    onScaleApply: () -> Unit,
    onScaleReset: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onTransformGesture: (panX: Float, panY: Float, zoomFactor: Float, focalX: Float, focalY: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text(text = "Manual Editor") })
                ToolSelectorRow(
                    selectedTool = uiState.tool,
                    onToolSelected = onToolSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                val scaleStatus = deriveScaleStatus(uiState.drawing.scale)
                ScaleStatusIndicator(
                    scaleStatus = scaleStatus,
                    onSetScale = { onToolSelected(EditorTool.SCALE) },
                    onResetScale = onScaleReset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        },
        bottomBar = {
            BottomSheetContent(
                selectedTool = uiState.tool,
                summaryText = buildSummaryText(uiState.drawing),
                scaleDraft = uiState.scaleDraft,
                undoEnabled = uiState.undoStack.isNotEmpty(),
                redoEnabled = uiState.redoStack.isNotEmpty(),
                onScaleLengthChanged = onScaleLengthChanged,
                onScaleApply = onScaleApply,
                onUndo = onUndo,
                onRedo = onRedo,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Loading Drawing2D…")
                    }
                }

                uiState.lastError != null -> {
                    Text(
                        text = uiState.lastError ?: "Unknown error",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    EditorCanvas(
                        uiState = uiState,
                        onScalePointSelected = onScalePointSelected,
                        onTransformGesture = onTransformGesture,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorCanvas(
    uiState: EditorState,
    onScalePointSelected: (Point2D) -> Unit,
    onTransformGesture: (panX: Float, panY: Float, zoomFactor: Float, focalX: Float, focalY: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = uiState.scaleDraft
    val scale = uiState.viewTransform.scale
    val offsetX = uiState.viewTransform.offsetX
    val offsetY = uiState.viewTransform.offsetY
    val pointColor = MaterialTheme.colorScheme.primary
    val lineColor = MaterialTheme.colorScheme.secondary
    val labelPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = 32f
        }
    }
    labelPaint.color = MaterialTheme.colorScheme.onSurface.toArgb()

    Box(
        modifier = modifier.pointerInput(uiState.tool, scale, offsetX, offsetY) {
            detectTapGestures { offset ->
                if (uiState.tool == EditorTool.SCALE) {
                    onScalePointSelected(screenToWorld(offset, uiState.viewTransform))
                }
            }
        }
    ) {
        DrawingCanvas(
            drawing = uiState.drawing,
            viewTransform = uiState.viewTransform,
            onTransformGesture = onTransformGesture,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pointA = draft.pointA
            val pointB = draft.pointB
            if (pointA != null) {
                val screenA = worldToScreen(pointA, uiState.viewTransform)
                drawCircle(color = pointColor, radius = 8f, center = screenA)
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText("A", screenA.x + 12f, screenA.y - 12f, labelPaint)
                }
            }
            if (pointB != null) {
                val screenB = worldToScreen(pointB, uiState.viewTransform)
                drawCircle(color = pointColor, radius = 8f, center = screenB)
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText("B", screenB.x + 12f, screenB.y - 12f, labelPaint)
                }
            }
            if (pointA != null && pointB != null) {
                val screenA = worldToScreen(pointA, uiState.viewTransform)
                val screenB = worldToScreen(pointB, uiState.viewTransform)
                drawLine(color = lineColor, start = screenA, end = screenB, strokeWidth = 3f)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Text(
                text = "Canvas",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildSummaryText(uiState.drawing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.tool == EditorTool.SCALE) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap two points to define scale.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ToolSelectorRow(
    selectedTool: EditorTool,
    onToolSelected: (EditorTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EditorTool.values().forEach { tool ->
            val isSelected = tool == selectedTool
            OutlinedButton(
                onClick = { onToolSelected(tool) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
            ) {
                Text(text = toolLabel(tool))
            }
        }
    }
}

@Composable
private fun ScaleStatusIndicator(
    scaleStatus: ScaleStatusDisplay,
    onSetScale: () -> Unit,
    onResetScale: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (background, content) = when (scaleStatus.status) {
        ScaleStatus.Missing -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        ScaleStatus.Invalid -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        ScaleStatus.Set -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(
        color = background,
        contentColor = content,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val statusText = when (scaleStatus.status) {
                ScaleStatus.Missing -> "Scale: not set"
                ScaleStatus.Invalid -> "Scale: invalid"
                ScaleStatus.Set -> {
                    val mmPerPx = scaleStatus.mmPerPx?.let { formatScaleMmPerPx(it) } ?: "?"
                    val refText = scaleStatus.referenceLengthMm?.let { "Ref ${formatScaleLengthMm(it)} mm" }
                    if (refText != null) {
                        "Scale: $mmPerPx mm/px • $refText"
                    } else {
                        "Scale: $mmPerPx mm/px"
                    }
                }
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            when (scaleStatus.status) {
                ScaleStatus.Missing -> {
                    TextButton(onClick = onSetScale) {
                        Text(text = "Set")
                    }
                }
                ScaleStatus.Invalid -> {
                    TextButton(onClick = onResetScale) {
                        Text(text = "Reset")
                    }
                }
                ScaleStatus.Set -> Unit
            }
        }
    }
}

@Composable
private fun BottomSheetContent(
    selectedTool: EditorTool,
    summaryText: String,
    scaleDraft: ScaleDraft,
    undoEnabled: Boolean,
    redoEnabled: Boolean,
    onScaleLengthChanged: (String) -> Unit,
    onScaleApply: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Bottom sheet",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tool: ${toolLabel(selectedTool)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onUndo,
                    enabled = undoEnabled,
                ) {
                    Text(text = "Undo")
                }
                OutlinedButton(
                    onClick = onRedo,
                    enabled = redoEnabled,
                ) {
                    Text(text = "Redo")
                }
            }

            if (selectedTool == EditorTool.SCALE) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Enter real length (mm)",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = scaleDraft.inputText,
                    onValueChange = onScaleLengthChanged,
                    label = { Text("Real length (mm)") },
                    singleLine = true,
                    isError = scaleDraft.inputError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (scaleDraft.inputError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scaleDraft.inputError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (scaleDraft.pendingDistancePx != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val distanceText = formatScaleValue(scaleDraft.pendingDistancePx, 3)
                    val mmPerPxText = scaleDraft.pendingMmPerPx?.let { formatScaleMmPerPx(it) }
                    Text(
                        text = if (mmPerPxText != null) {
                            "Distance: $distanceText units • mm/px: $mmPerPxText"
                        } else {
                            "Distance: $distanceText units"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (scaleDraft.applyError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scaleDraft.applyError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onScaleApply,
                    enabled = scaleDraft.pointA != null &&
                        scaleDraft.pointB != null &&
                        scaleDraft.pendingMmPerPx != null &&
                        scaleDraft.inputError == null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Apply scale")
                }
            }
        }
    }
}

private fun toolLabel(tool: EditorTool): String = when (tool) {
    EditorTool.SELECT -> "Select"
    EditorTool.SCALE -> "Scale"
    EditorTool.NODE -> "Node"
    EditorTool.MEMBER -> "Member"
}

private fun buildSummaryText(drawing: Drawing2D): String {
    val missingRefs = drawing.missingNodeReferences()
    val scaleStatus = when (deriveScaleStatus(drawing.scale).status) {
        ScaleStatus.Missing -> "Scale not set"
        ScaleStatus.Invalid -> "Scale invalid"
        ScaleStatus.Set -> "Scale calibrated"
    }
    return "Nodes: ${drawing.nodes.size} • Members: ${drawing.members.size} • " +
        "Missing refs: ${missingRefs.size} • $scaleStatus"
}

private fun screenToWorld(offset: Offset, transform: ViewTransform): Point2D {
    val scale = transform.scale.toDouble()
    return Point2D(
        x = (offset.x - transform.offsetX) / scale,
        y = (offset.y - transform.offsetY) / scale,
    )
}

private fun worldToScreen(point: Point2D, transform: ViewTransform): Offset {
    val scale = transform.scale
    return Offset(
        x = (point.x * scale + transform.offsetX).toFloat(),
        y = (point.y * scale + transform.offsetY).toFloat(),
    )
}

@Composable
private fun DrawingCanvas(
    drawing: Drawing2D,
    viewTransform: ViewTransform,
    onTransformGesture: (panX: Float, panY: Float, zoomFactor: Float, focalX: Float, focalY: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        if (pan != Offset.Zero || zoom != 1f) {
                            onTransformGesture(
                                panX = pan.x,
                                panY = pan.y,
                                zoomFactor = zoom,
                                focalX = centroid.x,
                                focalY = centroid.y,
                            )
                        }
                    }
                }
        ) {
            val originScreen = worldToScreen(viewTransform, Point2(0f, 0f))
            withTransform({
                translate(viewTransform.offsetX, viewTransform.offsetY)
                scale(viewTransform.scale, viewTransform.scale)
            }) {
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(-500f, 0f),
                    end = Offset(500f, 0f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF2196F3),
                    start = Offset(0f, -500f),
                    end = Offset(0f, 500f),
                    strokeWidth = 2f
                )
                drawing.nodes.forEach { node ->
                    drawCircle(
                        color = Color(0xFFFFC107),
                        radius = 6f,
                        center = Offset(node.x.toFloat(), node.y.toFloat())
                    )
                }
            }
            drawCircle(
                color = Color(0xFFE91E63),
                radius = 6f,
                center = Offset(originScreen.x, originScreen.y)
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                .padding(12.dp)
        ) {
            Text(
                text = "Scale: ${formatScaleValue(viewTransform.scale.toDouble(), 2)}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = buildSummaryText(drawing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
