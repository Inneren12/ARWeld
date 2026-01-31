package com.example.arweld.feature.drawingeditor.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.awaitUpOrCancellation
import androidx.compose.foundation.gestures.drag
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Node2D
import com.example.arweld.core.drawing2d.editor.v1.Point2D
import com.example.arweld.core.drawing2d.editor.v1.missingNodeReferences
import com.example.arweld.feature.drawingeditor.hittest.hitTestNode
import com.example.arweld.feature.drawingeditor.hittest.selectEntityAtTap
import com.example.arweld.feature.drawingeditor.render.ResolvedMember
import com.example.arweld.feature.drawingeditor.render.resolveAllMemberEndpoints
import com.example.arweld.feature.drawingeditor.viewmodel.EditorSelection
import com.example.arweld.feature.drawingeditor.viewmodel.EditorState
import com.example.arweld.feature.drawingeditor.viewmodel.EditorTool
import com.example.arweld.feature.drawingeditor.viewmodel.Point2
import com.example.arweld.feature.drawingeditor.viewmodel.NodeEditDraft
import com.example.arweld.feature.drawingeditor.viewmodel.ScaleDraft
import com.example.arweld.feature.drawingeditor.viewmodel.ScaleStatus
import com.example.arweld.feature.drawingeditor.viewmodel.ScaleStatusDisplay
import com.example.arweld.feature.drawingeditor.viewmodel.UnderlayState
import com.example.arweld.feature.drawingeditor.viewmodel.ViewTransform
import com.example.arweld.feature.drawingeditor.viewmodel.deriveScaleStatus
import com.example.arweld.feature.drawingeditor.viewmodel.formatNodeCoordinate
import com.example.arweld.feature.drawingeditor.viewmodel.formatScaleLengthMm
import com.example.arweld.feature.drawingeditor.viewmodel.formatScaleMmPerPx
import com.example.arweld.feature.drawingeditor.viewmodel.formatScaleValue
import com.example.arweld.feature.drawingeditor.viewmodel.worldToScreen
import java.util.Locale

/**
 * Render configuration constants for drawing primitives.
 * These are kept as constants to avoid per-frame allocations.
 */
private object RenderConfig {
    // Node rendering
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
    onScaleReset: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onNodeDelete: (String) -> Unit,
    onNodeTap: (Point2D, Float) -> Unit,
    onNodeDragStart: (String, Point2D) -> Unit,
    onNodeDragMove: (Point2D) -> Unit,
    onNodeDragEnd: (Point2D) -> Unit,
    onNodeDragCancel: () -> Unit,
    onSelectEntity: (EditorSelection) -> Unit,
    onClearSelection: () -> Unit,
    onNodeEditXChanged: (String) -> Unit,
    onNodeEditYChanged: (String) -> Unit,
    onNodeEditApply: (String) -> Unit,
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
            val selectedNodeId = (uiState.selection as? EditorSelection.Node)?.id
            val selectedNode = selectedNodeId?.let { id ->
                uiState.drawing.nodes.firstOrNull { it.id == id }
            }
            BottomSheetContent(
                selectedTool = uiState.tool,
                summaryText = buildSummaryText(uiState.drawing),
                scaleDraft = uiState.scaleDraft,
                selectedNode = selectedNode,
                nodeEditDraft = uiState.nodeEditDraft,
                undoEnabled = uiState.undoStack.isNotEmpty(),
                redoEnabled = uiState.redoStack.isNotEmpty(),
                onScaleLengthChanged = onScaleLengthChanged,
                onScaleApply = onScaleApply,
                onUndo = onUndo,
                onRedo = onRedo,
                onDeleteNode = onNodeDelete,
                onNodeEditXChanged = onNodeEditXChanged,
                onNodeEditYChanged = onNodeEditYChanged,
                onNodeEditApply = onNodeEditApply,
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
                    EditorCanvas(
                        uiState = uiState,
                        onScalePointSelected = onScalePointSelected,
                        onNodeTap = onNodeTap,
                        onNodeDragStart = onNodeDragStart,
                        onNodeDragMove = onNodeDragMove,
                        onNodeDragEnd = onNodeDragEnd,
                        onNodeDragCancel = onNodeDragCancel,
                        onSelectEntity = onSelectEntity,
                        onClearSelection = onClearSelection,
                        onTransformGesture = onTransformGesture,
                        modifier = Modifier.fillMaxSize(),
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
    onNodeTap: (Point2D, Float) -> Unit,
    onNodeDragStart: (String, Point2D) -> Unit,
    onNodeDragMove: (Point2D) -> Unit,
    onNodeDragEnd: (Point2D) -> Unit,
    onNodeDragCancel: () -> Unit,
    onSelectEntity: (EditorSelection) -> Unit,
    onClearSelection: () -> Unit,
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

    val density = LocalDensity.current
    val tolerancePx = with(density) { EditorUiConstants.NODE_HIT_RADIUS_DP.toPx() }
    val nodeRadiusPx = with(density) { EditorUiConstants.NODE_RENDER_RADIUS_DP.toPx() }
    val nodeRadiusWorld = nodeRadiusPx / scale.coerceAtLeast(0.001f)

    Box(
        modifier = modifier.pointerInput(uiState.tool, uiState.drawing, scale, offsetX, offsetY, tolerancePx) {
            awaitEachGesture {
                val down = awaitFirstDown()
                val worldDown = screenToWorld(down.position, uiState.viewTransform)
                val hitNodeId = if (uiState.tool == EditorTool.NODE) {
                    hitTestNode(
                        worldTap = worldDown,
                        nodes = uiState.drawing.nodes,
                        tolerancePx = tolerancePx,
                        viewTransform = uiState.viewTransform,
                    )
                } else {
                    null
                }
                val dragPointer = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                    if (hitNodeId != null) {
                        change.consume()
                    }
                }
                if (dragPointer != null && hitNodeId != null && uiState.tool == EditorTool.NODE) {
                    onNodeDragStart(hitNodeId, worldDown)
                    var lastWorld = screenToWorld(dragPointer.position, uiState.viewTransform)
                    onNodeDragMove(lastWorld)
                    val finished = drag(down.id) { change ->
                        lastWorld = screenToWorld(change.position, uiState.viewTransform)
                        onNodeDragMove(lastWorld)
                        change.consume()
                    }
                    if (finished) {
                        onNodeDragEnd(lastWorld)
                    } else {
                        onNodeDragCancel()
                    }
                } else {
                    val up = awaitUpOrCancellation() ?: return@awaitEachGesture
                    val worldTap = screenToWorld(up.position, uiState.viewTransform)
                    when (uiState.tool) {
                        EditorTool.SCALE -> onScalePointSelected(worldTap)
                        EditorTool.SELECT -> {
                            val selection = selectEntityAtTap(
                                worldTap = worldTap,
                                drawing = uiState.drawing,
                                tolerancePx = tolerancePx,
                                viewTransform = uiState.viewTransform,
                            )
                            if (selection == EditorSelection.None) {
                                onClearSelection()
                            } else {
                                onSelectEntity(selection)
                            }
                        }
                        EditorTool.NODE -> onNodeTap(worldTap, tolerancePx)
                        EditorTool.MEMBER -> Unit
                    }
                }
            }
        }
    ) {
        DrawingCanvasWithUnderlay(
            drawing = uiState.drawing,
            viewTransform = uiState.viewTransform,
            underlayState = uiState.underlayState,
            selection = uiState.selection,
            allowTransformGestures = uiState.nodeDragState == null,
            onTransformGesture = onTransformGesture,
            nodeRadiusWorld = nodeRadiusWorld,
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
    selectedNode: Node2D?,
    nodeEditDraft: NodeEditDraft,
    undoEnabled: Boolean,
    redoEnabled: Boolean,
    onScaleLengthChanged: (String) -> Unit,
    onScaleApply: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onDeleteNode: (String) -> Unit,
    onNodeEditXChanged: (String) -> Unit,
    onNodeEditYChanged: (String) -> Unit,
    onNodeEditApply: (String) -> Unit,
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

            if (selectedNode != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selected node: ${selectedNode.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Current (world): X ${formatNodeCoordinate(selectedNode.x)} • " +
                        "Y ${formatNodeCoordinate(selectedNode.y)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Edit coordinates (world)",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nodeEditDraft.xText,
                    onValueChange = onNodeEditXChanged,
                    label = { Text("X") },
                    singleLine = true,
                    isError = nodeEditDraft.xError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (nodeEditDraft.xError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nodeEditDraft.xError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nodeEditDraft.yText,
                    onValueChange = onNodeEditYChanged,
                    label = { Text("Y") },
                    singleLine = true,
                    isError = nodeEditDraft.yError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (nodeEditDraft.yError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nodeEditDraft.yError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (nodeEditDraft.applyError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nodeEditDraft.applyError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onNodeEditApply(selectedNode.id) },
                    enabled = nodeEditDraft.xText.isNotBlank() &&
                        nodeEditDraft.yText.isNotBlank() &&
                        nodeEditDraft.xError == null &&
                        nodeEditDraft.yError == null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Apply coordinates")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onDeleteNode(selectedNode.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(text = "Delete node")
                }
            }

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
    allowTransformGestures: Boolean,
    onTransformGesture: (panX: Float, panY: Float, zoomFactor: Float, focalX: Float, focalY: Float) -> Unit,
    nodeRadiusWorld: Float,
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
                        if (!allowTransformGestures) {
                            return@detectTransformGestures
                        }
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
                drawNodes(drawing, selection, nodeRadiusWorld)
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
                text = "Scale: ${formatScaleValue(viewTransform.scale.toDouble(), 2)}",
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
    nodeRadiusWorld: Float,
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
            nodeRadiusWorld * 1.25f
        } else {
            nodeRadiusWorld
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
