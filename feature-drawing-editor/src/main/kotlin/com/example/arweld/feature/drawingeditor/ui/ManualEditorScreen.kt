package com.example.arweld.feature.drawingeditor.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.core.drawing2d.editor.v1.missingNodeReferences
import com.example.arweld.feature.drawingeditor.render.ResolvedMember
import com.example.arweld.feature.drawingeditor.render.resolveAllMemberEndpoints
import com.example.arweld.feature.drawingeditor.viewmodel.EditorSelection
import com.example.arweld.feature.drawingeditor.viewmodel.EditorState
import com.example.arweld.feature.drawingeditor.viewmodel.EditorTool
import com.example.arweld.feature.drawingeditor.viewmodel.ScaleDraft
import com.example.arweld.feature.drawingeditor.viewmodel.ViewTransform
import java.util.Locale
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.example.arweld.feature.drawingeditor.viewmodel.Point2
import com.example.arweld.feature.drawingeditor.viewmodel.UnderlayState
import com.example.arweld.feature.drawingeditor.viewmodel.ViewTransform
import com.example.arweld.feature.drawingeditor.viewmodel.worldToScreen

/**
 * Render configuration constants for drawing primitives.
 * These are kept as constants to avoid per-frame allocations.
 */
private object RenderConfig {
    // Node rendering
    const val NODE_RADIUS = 8f
    const val NODE_STROKE_WIDTH = 2f
    val NODE_FILL_COLOR = Color(0xFFFFC107)          // Amber
    val NODE_STROKE_COLOR = Color(0xFFFF8F00)        // Dark amber
    val NODE_SELECTED_FILL_COLOR = Color(0xFF2196F3) // Blue (selection highlight)
    val NODE_SELECTED_STROKE_COLOR = Color(0xFF1565C0)

    // Member rendering
    const val MEMBER_STROKE_WIDTH = 3f
    val MEMBER_COLOR = Color(0xFF607D8B)             // Blue grey
    val MEMBER_SELECTED_COLOR = Color(0xFF2196F3)    // Blue (selection highlight)

    // Axes rendering
    const val AXIS_LENGTH = 500f
    const val AXIS_STROKE_WIDTH = 1.5f
    val AXIS_X_COLOR = Color(0xFF4CAF50)             // Green (X)
    val AXIS_Y_COLOR = Color(0xFF2196F3)             // Blue (Y)

    // Origin marker
    const val ORIGIN_MARKER_RADIUS = 4f
    val ORIGIN_MARKER_COLOR = Color(0xFFE91E63)      // Pink
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ManualEditorScreen(
    uiState: EditorState,
    onToolSelected: (EditorTool) -> Unit,
    onScalePointSelected: (Point2D) -> Unit,
    onScaleLengthChanged: (String) -> Unit,
    onScaleApply: () -> Unit,
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
                        Text(text = "Loading Drawing2D...")
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
                    DrawingCanvasWithUnderlay(
                    EditorCanvas(
                        uiState = uiState,
                        onScalePointSelected = onScalePointSelected,
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
        modifier = modifier
            .pointerInput(uiState.tool, scale, offsetX, offsetY) {
                detectTapGestures { offset ->
                    if (uiState.tool == EditorTool.SCALE) {
                        onScalePointSelected(screenToWorld(offset, uiState.viewTransform))
                    }
                    DrawingCanvas(
                        drawing = uiState.drawing,
                        viewTransform = uiState.viewTransform,
                        underlayState = uiState.underlayState,
                        selection = uiState.selection,
                        onTransformGesture = onTransformGesture,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    )
                }
            }
    ) {
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
                    val distanceText = formatNumber(scaleDraft.pendingDistancePx)
                    val mmPerPxText = scaleDraft.pendingMmPerPx?.let { formatNumber(it) }
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
    val scaleStatus = if (drawing.scale != null) "Scale calibrated" else "Scale not set"
    return "Nodes: ${drawing.nodes.size} \u2022 Members: ${drawing.members.size} \u2022 " +
        "Missing refs: ${missingRefs.size} \u2022 $scaleStatus"
}

/**
 * Main canvas composable that renders underlay image (if present) + overlay primitives.
 *
 * Render order (back to front):
 * 1. Underlay image (fit-center in world space)
 * 2. Coordinate axes
 * 3. Members (lines between nodes)
 * 4. Nodes (circles with optional selection highlight)
 * 5. Origin marker
 */
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

private fun formatNumber(value: Double): String = String.format(Locale.US, "%.4f", value)
@Composable
private fun DrawingCanvasWithUnderlay(
    drawing: Drawing2D,
    viewTransform: ViewTransform,
    underlayState: UnderlayState,
    selection: EditorSelection,
    onTransformGesture: (panX: Float, panY: Float, zoomFactor: Float, focalX: Float, focalY: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Pre-compute resolved members to avoid per-frame allocations
    val resolvedMembers = remember(drawing) {
        resolveAllMemberEndpoints(drawing)
    }

    Box(modifier = modifier) {
        // Layer 1: Underlay image (rendered via Coil SubcomposeAsyncImage for fit-center)
        when (underlayState) {
            is UnderlayState.Loaded -> {
                UnderlayImage(
                    file = underlayState.file,
                    viewTransform = viewTransform,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is UnderlayState.Loading -> {
                // Could show a loading indicator for underlay specifically
            }
            is UnderlayState.Missing, is UnderlayState.None -> {
                // No underlay to render
            }
        }

        // Layer 2+: Overlay primitives via Canvas
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
            // Render all overlay primitives within the world transform
            withTransform({
                translate(viewTransform.offsetX, viewTransform.offsetY)
                scale(viewTransform.scale, viewTransform.scale)
            }) {
                // Draw coordinate axes
                drawAxes()

                // Draw members as lines
                drawMembers(resolvedMembers, selection)

                // Draw nodes as circles
                drawNodes(drawing, selection)
            }

            // Draw origin marker in screen space (always visible reference)
            val originScreen = worldToScreen(viewTransform, Point2(0f, 0f))
            drawCircle(
                color = RenderConfig.ORIGIN_MARKER_COLOR,
                radius = RenderConfig.ORIGIN_MARKER_RADIUS,
                center = Offset(originScreen.x, originScreen.y)
            )
        }

        // HUD overlay with scale and summary info
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                .padding(12.dp)
        ) {
            Text(
                text = "Scale: ${"%.2f".format(viewTransform.scale)}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = buildSummaryText(drawing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Show underlay status
            val underlayStatus = when (underlayState) {
                is UnderlayState.None -> null
                is UnderlayState.Loading -> "Underlay: loading..."
                is UnderlayState.Loaded -> "Underlay: loaded"
                is UnderlayState.Missing -> "Underlay: missing"
            }
            underlayStatus?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Renders the underlay image using Coil with proper transform handling.
 * The image is rendered fit-center at the world origin (0,0).
 */
@Composable
private fun UnderlayImage(
    file: java.io.File,
    viewTransform: ViewTransform,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(file)
            .crossfade(true)
            .build(),
        contentDescription = "Underlay image",
        modifier = modifier,
    ) {
        val state = painter.state
        when (state) {
            is AsyncImagePainter.State.Loading -> {
                // Optional: show loading indicator
            }
            is AsyncImagePainter.State.Success -> {
                // Get image dimensions for proper positioning
                val painter = state.painter
                val intrinsicSize = painter.intrinsicSize

                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Calculate the position in screen space where the image origin should be
                        val originScreen = worldToScreen(viewTransform, Point2(0f, 0f))

                        // Apply transform and draw the image
                        withTransform({
                            translate(originScreen.x, originScreen.y)
                            scale(viewTransform.scale, viewTransform.scale)
                        }) {
                            // Draw the image with the painter
                            with(painter) {
                                draw(
                                    size = intrinsicSize,
                                )
                            }
                        }
                    }
                }
            }
            is AsyncImagePainter.State.Error -> {
                // Error state is handled by UnderlayState.Missing
            }
            is AsyncImagePainter.State.Empty -> {
                // No image
            }
        }
    }
}

/**
 * Draw coordinate axes in world space.
 */
private fun DrawScope.drawAxes() {
    // X axis (horizontal, green)
    drawLine(
        color = RenderConfig.AXIS_X_COLOR,
        start = Offset(-RenderConfig.AXIS_LENGTH, 0f),
        end = Offset(RenderConfig.AXIS_LENGTH, 0f),
        strokeWidth = RenderConfig.AXIS_STROKE_WIDTH
    )
    // Y axis (vertical, blue)
    drawLine(
        color = RenderConfig.AXIS_Y_COLOR,
        start = Offset(0f, -RenderConfig.AXIS_LENGTH),
        end = Offset(0f, RenderConfig.AXIS_LENGTH),
        strokeWidth = RenderConfig.AXIS_STROKE_WIDTH
    )
}

/**
 * Draw members as lines between resolved endpoints.
 */
private fun DrawScope.drawMembers(
    resolvedMembers: List<ResolvedMember>,
    selection: EditorSelection,
) {
    resolvedMembers.forEach { member ->
        val isSelected = selection is EditorSelection.Member && selection.id == member.memberId
        val color = if (isSelected) {
            RenderConfig.MEMBER_SELECTED_COLOR
        } else {
            RenderConfig.MEMBER_COLOR
        }
        val strokeWidth = if (isSelected) {
            RenderConfig.MEMBER_STROKE_WIDTH * 1.5f
        } else {
            RenderConfig.MEMBER_STROKE_WIDTH
        }

        drawLine(
            color = color,
            start = Offset(member.startPoint.x, member.startPoint.y),
            end = Offset(member.endPoint.x, member.endPoint.y),
            strokeWidth = strokeWidth
        )
    }
}

/**
 * Draw nodes as circles with optional selection highlight.
 */
private fun DrawScope.drawNodes(
    drawing: Drawing2D,
    selection: EditorSelection,
) {
    drawing.nodes.forEach { node ->
        val isSelected = selection is EditorSelection.Node && selection.id == node.id
        val fillColor = if (isSelected) {
            RenderConfig.NODE_SELECTED_FILL_COLOR
        } else {
            RenderConfig.NODE_FILL_COLOR
        }
        val strokeColor = if (isSelected) {
            RenderConfig.NODE_SELECTED_STROKE_COLOR
        } else {
            RenderConfig.NODE_STROKE_COLOR
        }
        val radius = if (isSelected) {
            RenderConfig.NODE_RADIUS * 1.25f
        } else {
            RenderConfig.NODE_RADIUS
        }

        val center = Offset(node.x.toFloat(), node.y.toFloat())

        // Draw filled circle
        drawCircle(
            color = fillColor,
            radius = radius,
            center = center
        )
        // Draw stroke
        drawCircle(
            color = strokeColor,
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = RenderConfig.NODE_STROKE_WIDTH
            )
        )
    }
}
