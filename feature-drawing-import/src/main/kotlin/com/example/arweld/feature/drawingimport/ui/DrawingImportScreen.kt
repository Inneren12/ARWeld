package com.example.arweld.feature.drawingimport.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.io.v1.FileArtifactStoreV1
import com.example.arweld.core.drawing2d.artifacts.io.v1.ManifestWriterV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.core.drawing2d.artifacts.v1.CaptureMetaV1
import com.example.arweld.feature.drawingimport.BuildConfig
import com.example.arweld.feature.drawingimport.artifacts.DrawingImportArtifacts
import com.example.arweld.feature.drawingimport.camera.CameraSession
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportErrorCode
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportErrorMapper
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportEvent
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportEventLogger
import com.example.arweld.feature.drawingimport.overlay.CornerOverlayRendererV1
import com.example.arweld.feature.drawingimport.pipeline.DrawingImportPipelineParamsV1
import com.example.arweld.feature.drawingimport.pipeline.DrawingImportPipelineV1
import com.example.arweld.feature.drawingimport.preprocess.ContourStats
import com.example.arweld.feature.drawingimport.preprocess.ContourV1
import com.example.arweld.feature.drawingimport.preprocess.CornerOrderingV1
import com.example.arweld.feature.drawingimport.preprocess.CornerRefinerV1
import com.example.arweld.feature.drawingimport.preprocess.OrderedCornersV1 as PreprocessOrderedCornersV1
import com.example.arweld.feature.drawingimport.quality.DrawingImportPipelineResultV1
import com.example.arweld.feature.drawingimport.quality.ExposureMetricsV1
import com.example.arweld.feature.drawingimport.quality.OrderedCornersV1 as QualityOrderedCornersV1
import com.example.arweld.feature.drawingimport.quality.QualityGateResultV1
import com.example.arweld.feature.drawingimport.quality.QualityGateV1
import com.example.arweld.feature.drawingimport.quality.QualityMetricsV1
import com.example.arweld.feature.drawingimport.quality.RectifiedQualityMetricsV1
import com.example.arweld.feature.drawingimport.quality.SkewMetricsV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFailureV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectStageV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFrame
import com.example.arweld.feature.drawingimport.preprocess.PageDetectInput
import com.example.arweld.feature.drawingimport.preprocess.PageDetectContourExtractor
import com.example.arweld.feature.drawingimport.preprocess.PageDetectEdgeDetector
import com.example.arweld.feature.drawingimport.preprocess.PageDetectPreprocessor
import com.example.arweld.feature.drawingimport.preprocess.PageQuadCandidate
import com.example.arweld.feature.drawingimport.preprocess.PageQuadSelector
import com.example.arweld.feature.drawingimport.preprocess.PageDetectOutcomeV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFailureCodeV1
import com.example.arweld.feature.drawingimport.preprocess.RectifiedSizeV1
import com.example.arweld.feature.drawingimport.preprocess.DrawingImportGuardrailsV1
import com.example.arweld.feature.drawingimport.preprocess.RectifySizeParamsV1
import com.example.arweld.feature.drawingimport.preprocess.RectifySizePolicyV1
import com.example.arweld.feature.drawingimport.preprocess.RefineParamsV1
import com.example.arweld.feature.drawingimport.preprocess.RefineResultV1
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DrawingImportScreen(
    modifier: Modifier = Modifier,
    diagnosticsRecorder: DiagnosticsRecorder,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraSession = remember { CameraSession(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var screenState by rememberSaveable {
        mutableStateOf<DrawingImportUiState>(DrawingImportUiState.Idle)
    }
    var activeProjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var imageError by remember { mutableStateOf(false) }
    var lastEventName by remember { mutableStateOf<String?>(null) }
    var lastErrorCode by remember { mutableStateOf<DrawingImportErrorCode?>(null) }
    var hasLoggedCameraReady by remember { mutableStateOf(false) }
    var pageDetectInfo by remember { mutableStateOf<PageDetectFrameInfo?>(null) }
    var pageDetectFrame by remember { mutableStateOf<PageDetectFrame?>(null) }
    var preprocessFailure by remember { mutableStateOf<PageDetectFailureV1?>(null) }
    var isPreparingFrame by remember { mutableStateOf(false) }
    var contourInfo by remember { mutableStateOf<ContourDebugInfo?>(null) }
    var contourFailure by remember { mutableStateOf<PageDetectFailureV1?>(null) }
    var isRunningContours by remember { mutableStateOf(false) }
    var contourCache by remember { mutableStateOf<List<ContourV1>>(emptyList()) }
    var quadSelectionOutcome by remember { mutableStateOf<PageDetectOutcomeV1<PageQuadCandidate>?>(null) }
    var quadSelectionFailure by remember { mutableStateOf<PageDetectFailureV1?>(null) }
    var orderFailure by remember { mutableStateOf<PageDetectFailureV1?>(null) }
    var isSelectingQuad by remember { mutableStateOf(false) }
    var isSavingOverlay by remember { mutableStateOf(false) }
    var rectifiedSizeOutcome by remember { mutableStateOf<PageDetectOutcomeV1<RectifiedSizeV1>?>(null) }
    var orderedCorners by remember { mutableStateOf<PreprocessOrderedCornersV1?>(null) }
    var refineOutcome by remember { mutableStateOf<PageDetectOutcomeV1<RefineResultV1>?>(null) }
    var refineFailure by remember { mutableStateOf<PageDetectFailureV1?>(null) }
    var pipelineResult by remember { mutableStateOf<DrawingImportPipelineResultV1?>(null) }
    var processState by remember { mutableStateOf<DrawingImportProcessState>(DrawingImportProcessState.Idle) }
    var pipelineJob by remember { mutableStateOf<Job?>(null) }
    var loadedCaptureMeta by remember { mutableStateOf<CaptureMetaV1?>(null) }
    val diagnosticsLogger = remember(diagnosticsRecorder) {
        DrawingImportEventLogger(diagnosticsRecorder)
    }
    val pageDetectPreprocessor = remember { PageDetectPreprocessor() }
    val edgeDetector = remember { PageDetectEdgeDetector() }
    val contourExtractor = remember { PageDetectContourExtractor() }
    val quadSelector = remember { PageQuadSelector() }
    val refineParams = remember {
        RefineParamsV1(
            windowRadiusPx = 6,
            maxIters = 6,
            epsilon = 0.25,
        )
    }
    var uiState by remember {
        mutableStateOf<CameraPermissionState>(
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                CameraPermissionState.Ready
            } else {
                CameraPermissionState.NeedsPermission
            }
        )
    }
    if (uiState is CameraPermissionState.Ready && screenState == DrawingImportUiState.Idle) {
        screenState = DrawingImportUiState.Ready
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        uiState = if (granted) {
            CameraPermissionState.Ready
        } else {
            CameraPermissionState.PermissionDenied
        }
        val permissionState = if (granted) "granted" else "denied"
        logDiagnosticsEvent(
            logger = diagnosticsLogger,
            event = DrawingImportEvent.PERMISSION_RESULT,
            state = permissionState,
            projectId = activeProjectId,
            onLogged = { lastEventName = DrawingImportEvent.PERMISSION_RESULT.eventName },
        )
        if (!granted) {
            logDiagnosticsError(
                logger = diagnosticsLogger,
                projectId = activeProjectId,
                code = DrawingImportErrorCode.PERMISSION_DENIED,
                onLogged = {
                    lastEventName = DrawingImportEvent.ERROR.eventName
                    lastErrorCode = DrawingImportErrorCode.PERMISSION_DENIED
                },
            )
        }
    }

    val shouldBindCamera = uiState is CameraPermissionState.Ready &&
        (screenState is DrawingImportUiState.Ready || screenState is DrawingImportUiState.Capturing)

    LaunchedEffect(activeProjectId) {
        val projectId = activeProjectId
        if (projectId == null) {
            loadedCaptureMeta = null
            return@LaunchedEffect
        }
        val projectDir = File(DrawingImportArtifacts.artifactsRoot(context), projectId)
        val metaFile = File(projectDir, ProjectLayoutV1.CAPTURE_META_JSON)
        val meta = withContext(Dispatchers.IO) {
            if (!metaFile.exists()) {
                null
            } else {
                runCatching {
                    Drawing2DJson.decodeFromString<CaptureMetaV1>(metaFile.readText())
                }.getOrNull()
            }
        }
        loadedCaptureMeta = meta
        val currentState = screenState
        if (meta != null && currentState is DrawingImportUiState.Saved) {
            val session = currentState.session
            val metaBlur = meta.metrics?.blurVariance
            val mergedMetrics = when {
                session.rectifiedQualityMetrics == null -> RectifiedQualityMetricsV1(blurVariance = metaBlur)
                session.rectifiedQualityMetrics.blurVariance == null && metaBlur != null -> {
                    RectifiedQualityMetricsV1(blurVariance = metaBlur)
                }
                else -> session.rectifiedQualityMetrics
            }
            val mergedRectified = session.rectifiedImageInfo
                ?: meta.rectified?.let { RectifiedImageInfo(it.widthPx, it.heightPx) }
            screenState = DrawingImportUiState.Saved(
                session.copy(
                    rectifiedQualityMetrics = mergedMetrics,
                    rectifiedImageInfo = mergedRectified,
                ),
            )
        }
    }

    DisposableEffect(shouldBindCamera) {
        if (!shouldBindCamera) {
            hasLoggedCameraReady = false
        }
        onDispose { }
    }

    DisposableEffect(Unit) {
        onDispose {
            pipelineJob?.cancel()
        }
    }

    DisposableEffect(shouldBindCamera, lifecycleOwner, previewView) {
        val currentPreviewView = previewView
        if (shouldBindCamera && currentPreviewView != null) {
            cameraSession.start(
                lifecycleOwner,
                currentPreviewView,
                onReady = {
                    if (!hasLoggedCameraReady) {
                        logDiagnosticsEvent(
                            logger = diagnosticsLogger,
                            event = DrawingImportEvent.CAMERA_BIND_SUCCESS,
                            state = "ready",
                            projectId = activeProjectId,
                            onLogged = {
                                lastEventName = DrawingImportEvent.CAMERA_BIND_SUCCESS.eventName
                            },
                        )
                        hasLoggedCameraReady = true
                    }
                },
                onError = { throwable ->
                    cameraSession.stop()
                    uiState = CameraPermissionState.CameraError(
                        throwable.message ?: "Camera failed to start. Please try again.",
                    )
                    screenState = DrawingImportUiState.Error("Camera failed to start. Please try again.")
                    logDiagnosticsEvent(
                        logger = diagnosticsLogger,
                        event = DrawingImportEvent.CAMERA_BIND_FAILED,
                        state = "failed",
                        projectId = activeProjectId,
                        onLogged = {
                            lastEventName = DrawingImportEvent.CAMERA_BIND_FAILED.eventName
                        },
                    )
                    logDiagnosticsError(
                        logger = diagnosticsLogger,
                        projectId = activeProjectId,
                        code = DrawingImportErrorCode.CAMERA_BIND_FAILED,
                        onLogged = {
                            lastEventName = DrawingImportEvent.ERROR.eventName
                            lastErrorCode = DrawingImportErrorCode.CAMERA_BIND_FAILED
                        },
                    )
                },
            )
        }
        onDispose {
            cameraSession.stop()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (shouldBindCamera) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { factoryContext ->
                        PreviewView(factoryContext).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            previewView = this
                        }
                    },
                    update = { updatedView ->
                        previewView = updatedView
                    },
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                ) {
                    Text(
                        text = "Drawing Import",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    when (val state = uiState) {
                        CameraPermissionState.NeedsPermission -> {
                            Text(
                                text = "To start capture, allow camera access.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text(text = "Grant Camera")
                            }
                        }

                        CameraPermissionState.PermissionDenied -> {
                            Text(
                                text = "Camera access was denied. Please grant permission to continue.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text(text = "Retry Permission")
                            }
                        }

                        is CameraPermissionState.CameraError -> {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    uiState = if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA,
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        CameraPermissionState.Ready
                                    } else {
                                        CameraPermissionState.NeedsPermission
                                    }
                                },
                            ) {
                                Text(text = "Retry Camera")
                            }
                        }

                        CameraPermissionState.Ready -> {
                            when (val captureState = screenState) {
                                DrawingImportUiState.Idle,
                                DrawingImportUiState.Ready,
                                DrawingImportUiState.Capturing,
                                -> {
                                    Text(
                                        text = if (captureState == DrawingImportUiState.Capturing) {
                                            "Capturing..."
                                        } else {
                                            "Camera ready"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                is DrawingImportUiState.Error -> {
                                    Text(
                                        text = captureState.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            screenState = DrawingImportUiState.Ready
                                        },
                                    ) {
                                        Text(text = "Retry Capture")
                                    }
                                }

                                is DrawingImportUiState.Saved -> {
                                    val rawEntry = captureState.session.artifacts.firstOrNull {
                                        it.kind == ArtifactKindV1.RAW_IMAGE
                                    }
                                    val manifestEntry = captureState.session.artifacts.firstOrNull {
                                        it.kind == ArtifactKindV1.MANIFEST_JSON
                                    }
                                    val overlayRelPath = ProjectLayoutV1.overlay("corners")
                                    val overlayEntry = captureState.session.artifacts.firstOrNull {
                                        it.kind == ArtifactKindV1.OVERLAY && it.relPath == overlayRelPath
                                    }
                                    val rectifiedEntry = captureState.session.artifacts.firstOrNull {
                                        it.kind == ArtifactKindV1.RECTIFIED_IMAGE &&
                                            it.relPath == ProjectLayoutV1.RECTIFIED_IMAGE_PNG
                                    }
                                    val rectifiedInfo = captureState.session.rectifiedImageInfo
                                    val rawFile = rawEntry?.let {
                                        File(captureState.session.projectDir, it.relPath)
                                    }
                                    val rectifiedFile = rectifiedEntry?.let {
                                        File(captureState.session.projectDir, it.relPath)
                                    }

                                    Text(
                                        text = "Capture saved",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Project ID",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                    Text(
                                        text = captureState.session.projectId,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Raw preview",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(min = 180.dp, max = 260.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (rawFile != null) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(context)
                                                            .data(rawFile)
                                                            .crossfade(true)
                                                            .build(),
                                                        contentDescription = "Captured raw drawing preview",
                                                        modifier = Modifier.fillMaxSize(),
                                                        onError = {
                                                            if (!imageError) {
                                                                logDiagnosticsError(
                                                                    logger = diagnosticsLogger,
                                                                    projectId = captureState.session.projectId,
                                                                    code = DrawingImportErrorCode.PREVIEW_LOAD_FAILED,
                                                                    onLogged = {
                                                                        lastEventName = DrawingImportEvent.ERROR.eventName
                                                                        lastErrorCode = DrawingImportErrorCode.PREVIEW_LOAD_FAILED
                                                                    },
                                                                )
                                                            }
                                                            imageError = true
                                                        },
                                                    )
                                                }
                                                if (rawFile == null || imageError) {
                                                    Text(
                                                        text = "Preview unavailable",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (rectifiedFile != null) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Rectified preview",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(min = 180.dp, max = 260.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(context)
                                                            .data(rectifiedFile)
                                                            .crossfade(true)
                                                            .build(),
                                                        contentDescription = "Rectified drawing preview",
                                                        modifier = Modifier.fillMaxSize(),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = "Artifacts",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        ArtifactSummaryRow(
                                            label = "Raw",
                                            relPath = rawEntry?.relPath ?: "Missing",
                                            sha256 = rawEntry?.sha256 ?: "--",
                                        )
                                        rectifiedEntry?.let { entry ->
                                            val sizeLabel = rectifiedInfo?.let { info ->
                                                "${info.width}x${info.height}"
                                            }
                                            ArtifactSummaryRow(
                                                label = "Rectified",
                                                relPath = entry.relPath,
                                                sha256 = entry.sha256,
                                                pixelSha256 = entry.pixelSha256,
                                                sizeLabel = sizeLabel,
                                            )
                                        }
                                        ArtifactSummaryRow(
                                            label = "Manifest",
                                            relPath = manifestEntry?.relPath ?: "Missing",
                                            sha256 = manifestEntry?.sha256 ?: "--",
                                        )
                                        overlayEntry?.let { entry ->
                                            ArtifactSummaryRow(
                                                label = "Corners overlay",
                                                relPath = entry.relPath,
                                                sha256 = entry.sha256,
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = "Process",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        when (val current = processState) {
                                            DrawingImportProcessState.Idle -> {
                                                Text(
                                                    text = "Run the deterministic pipeline to produce a rectified image.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                            is DrawingImportProcessState.Running -> {
                                                Text(
                                                    text = "Processing: ${formatStageLabel(current.stage)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                            is DrawingImportProcessState.Success -> {
                                                Text(
                                                    text = "Process complete. Rectified size: " +
                                                        "${current.result.rectifiedSize.width}x${current.result.rectifiedSize.height}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                            is DrawingImportProcessState.Failure -> {
                                                Text(
                                                    text = "Failed at ${current.failure.stage} • ${current.failure.code}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                )
                                                Text(
                                                    text = "Hint: try a steady capture with clear edges and even lighting.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            val isRunning = processState is DrawingImportProcessState.Running
                                            Button(
                                                onClick = {
                                                    if (isRunning) return@Button
                                                    val session = captureState.session
                                                    pipelineJob?.cancel()
                                                    processState = DrawingImportProcessState.Running(PageDetectStageV1.LOAD_UPRIGHT)
                                                    pipelineJob = coroutineScope.launch(Dispatchers.Default) {
                                                        val pipeline = DrawingImportPipelineV1(
                                                            params = DrawingImportPipelineParamsV1(),
                                                            eventLogger = diagnosticsLogger,
                                                            stageListener = { stage ->
                                                                coroutineScope.launch {
                                                                    processState = DrawingImportProcessState.Running(stage)
                                                                }
                                                            },
                                                            cancellationContext = coroutineContext,
                                                        )
                                                        val result = pipeline.run(session)
                                                        withContext(Dispatchers.Main) {
                                                            when (result) {
                                                                is PageDetectOutcomeV1.Success -> {
                                                                    val updatedSession = session.copy(
                                                                        artifacts = result.value.artifacts,
                                                                        rectifiedImageInfo = RectifiedImageInfo(
                                                                            width = result.value.rectifiedSize.width,
                                                                            height = result.value.rectifiedSize.height,
                                                                        ),
                                                                        rectifiedQualityMetrics = result.value.rectifiedQualityMetrics,
                                                                    )
                                                                    screenState = DrawingImportUiState.Saved(updatedSession)
                                                                    processState = DrawingImportProcessState.Success(result.value)
                                                                }
                                                                is PageDetectOutcomeV1.Failure -> {
                                                                    processState = DrawingImportProcessState.Failure(result.failure)
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                enabled = !isRunning,
                                            ) {
                                                Text(text = if (isRunning) "Processing..." else "Process")
                                            }
                                            if (isRunning) {
                                                Button(
                                                    onClick = {
                                                        pipelineJob?.cancel()
                                                        processState = DrawingImportProcessState.Idle
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.secondary,
                                                        contentColor = MaterialTheme.colorScheme.onSecondary,
                                                    ),
                                                ) {
                                                    Text(text = "Cancel")
                                                }
                                            }
                                        }
                                    }
                                    if (BuildConfig.DEBUG) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                text = "Overlay output",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Button(
                                                onClick = {
                                                    val frame = pageDetectFrame
                                                    val ordered = orderedCorners
                                                    if (frame == null || ordered == null) {
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                "Run quad selection before saving the overlay.",
                                                            )
                                                        }
                                                        return@Button
                                                    }
                                                    if (isSavingOverlay) return@Button
                                                    isSavingOverlay = true
                                                    coroutineScope.launch(Dispatchers.Default) {
                                                        runCatching {
                                                            val baseBitmap = frameToBitmap(frame)
                                                            val overlayBitmap = CornerOverlayRendererV1().render(
                                                                baseBitmap = baseBitmap,
                                                                ordered = ordered,
                                                                refined = (refineOutcome as? PageDetectOutcomeV1.Success)
                                                                    ?.value
                                                                    ?.corners,
                                                            )
                                                            val pngBytes = encodePng(overlayBitmap)
                                                            baseBitmap.recycle()
                                                            overlayBitmap.recycle()
                                                            val store = FileArtifactStoreV1(
                                                                captureState.session.projectDir,
                                                            )
                                                            val overlayResult = store.writeBytes(
                                                                kind = ArtifactKindV1.OVERLAY,
                                                                relPath = overlayRelPath,
                                                                bytes = pngBytes,
                                                                mime = "image/png",
                                                            )
                                                            val artifactsWithoutManifest = captureState.session.artifacts
                                                                .filter { it.kind != ArtifactKindV1.MANIFEST_JSON }
                                                                .filterNot {
                                                                    it.kind == ArtifactKindV1.OVERLAY &&
                                                                        it.relPath == overlayRelPath
                                                                }
                                                            val updatedArtifacts = artifactsWithoutManifest + overlayResult
                                                            val newManifest = ManifestWriterV1().write(
                                                                captureState.session.projectDir,
                                                                DrawingImportArtifacts.buildManifest(
                                                                    projectId = captureState.session.projectId,
                                                                    artifacts = updatedArtifacts,
                                                                ),
                                                            )
                                                            val finalArtifacts = updatedArtifacts + newManifest
                                                            withContext(Dispatchers.Main) {
                                                                screenState = DrawingImportUiState.Saved(
                                                                    captureState.session.copy(
                                                                        artifacts = finalArtifacts,
                                                                    ),
                                                                )
                                                                snackbarHostState.showSnackbar(
                                                                    "Corners overlay saved",
                                                                )
                                                            }
                                                        }.onFailure { error ->
                                                            withContext(Dispatchers.Main) {
                                                                snackbarHostState.showSnackbar(
                                                                    error.message
                                                                        ?: "Failed to save corners overlay.",
                                                                )
                                                            }
                                                        }
                                                        withContext(Dispatchers.Main) {
                                                            isSavingOverlay = false
                                                        }
                                                    }
                                                },
                                                enabled = !isSavingOverlay,
                                            ) {
                                                Text(
                                                    text = if (isSavingOverlay) {
                                                        "Saving overlay..."
                                                    } else {
                                                        "Save corners overlay"
                                                    },
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                text = "Page detection prep",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            ) {
                                                Button(
                                                    onClick = {
                                                        if (rawFile == null) {
                                                            coroutineScope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    "Raw image missing; recapture first.",
                                                                )
                                                            }
                                                            return@Button
                                                        }
                                                        pageDetectInfo = null
                                                        pageDetectFrame = null
                                                        preprocessFailure = null
                                                        isPreparingFrame = true
                                                        contourInfo = null
                                                        contourFailure = null
                                                        isRunningContours = false
                                                        contourCache = emptyList()
                                                        quadSelectionOutcome = null
                                                        quadSelectionFailure = null
                                                        orderFailure = null
                                                        isSelectingQuad = false
                                                        rectifiedSizeOutcome = null
                                                        orderedCorners = null
                                                        refineOutcome = null
                                                        refineFailure = null
                                                        pipelineResult = null
                                                        coroutineScope.launch(Dispatchers.Default) {
                                                            val outcome = pageDetectPreprocessor.preprocess(
                                                                PageDetectInput(rawImageFile = rawFile),
                                                            )
                                                            withContext(Dispatchers.Main) {
                                                                when (outcome) {
                                                                    is PageDetectOutcomeV1.Success -> {
                                                                        val frame = outcome.value
                                                                        pageDetectFrame = frame
                                                                        pageDetectInfo = PageDetectFrameInfo(
                                                                            width = frame.width,
                                                                            height = frame.height,
                                                                            downscaleFactor = frame.downscaleFactor,
                                                                            rotationAppliedDeg = frame.rotationAppliedDeg,
                                                                        )
                                                                        preprocessFailure = null
                                                                    }
                                                                    is PageDetectOutcomeV1.Failure -> {
                                                                        preprocessFailure = outcome.failure
                                                                        logPageDetectFailure(
                                                                            logger = diagnosticsLogger,
                                                                            failure = outcome.failure,
                                                                            projectId = activeProjectId,
                                                                            onLogged = {
                                                                                lastEventName = DrawingImportEvent.ERROR.eventName
                                                                            },
                                                                        )
                                                                    }
                                                                }
                                                                isPreparingFrame = false
                                                            }
                                                        }
                                                    },
                                                ) {
                                                    Text(text = "Prepare for detection")
                                                }
                                                Button(
                                                    onClick = {
                                                        if (rawFile == null) {
                                                            coroutineScope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    "Raw image missing; recapture first.",
                                                                )
                                                            }
                                                            return@Button
                                                        }
                                                        contourInfo = null
                                                        contourFailure = null
                                                        preprocessFailure = null
                                                        isPreparingFrame = true
                                                        isRunningContours = true
                                                        quadSelectionOutcome = null
                                                        quadSelectionFailure = null
                                                        orderFailure = null
                                                        isSelectingQuad = false
                                                        rectifiedSizeOutcome = null
                                                        coroutineScope.launch(Dispatchers.Default) {
                                                            val frameOutcome = pageDetectFrame?.let {
                                                                PageDetectOutcomeV1.Success(it)
                                                            } ?: pageDetectPreprocessor.preprocess(
                                                                PageDetectInput(rawImageFile = rawFile),
                                                            )
                                                            val frame = when (frameOutcome) {
                                                                is PageDetectOutcomeV1.Success -> frameOutcome.value
                                                                is PageDetectOutcomeV1.Failure -> {
                                                                    withContext(Dispatchers.Main) {
                                                                        preprocessFailure = frameOutcome.failure
                                                                        logPageDetectFailure(
                                                                            logger = diagnosticsLogger,
                                                                            failure = frameOutcome.failure,
                                                                            projectId = activeProjectId,
                                                                            onLogged = {
                                                                                lastEventName = DrawingImportEvent.ERROR.eventName
                                                                            },
                                                                        )
                                                                        isPreparingFrame = false
                                                                        isRunningContours = false
                                                                    }
                                                                    return@launch
                                                                }
                                                            }
                                                            val edgesOutcome = edgeDetector.detect(frame)
                                                            val edgeMap = when (edgesOutcome) {
                                                                is PageDetectOutcomeV1.Success -> edgesOutcome.value
                                                                is PageDetectOutcomeV1.Failure -> {
                                                                    withContext(Dispatchers.Main) {
                                                                        contourFailure = edgesOutcome.failure
                                                                        logPageDetectFailure(
                                                                            logger = diagnosticsLogger,
                                                                            failure = edgesOutcome.failure,
                                                                            projectId = activeProjectId,
                                                                            onLogged = {
                                                                                lastEventName = DrawingImportEvent.ERROR.eventName
                                                                            },
                                                                        )
                                                                        isPreparingFrame = false
                                                                        isRunningContours = false
                                                                    }
                                                                    return@launch
                                                                }
                                                            }
                                                            val contoursOutcome = contourExtractor.extract(edgeMap)
                                                            when (contoursOutcome) {
                                                                is PageDetectOutcomeV1.Success -> {
                                                                    val contours = contoursOutcome.value
                                                                withContext(Dispatchers.Main) {
                                                                    pageDetectFrame = frame
                                                                    pageDetectInfo = PageDetectFrameInfo(
                                                                        width = frame.width,
                                                                        height = frame.height,
                                                                        downscaleFactor = frame.downscaleFactor,
                                                                        rotationAppliedDeg = frame.rotationAppliedDeg,
                                                                    )
                                                                    preprocessFailure = null
                                                                    contourInfo = ContourDebugInfo(
                                                                        totalContours = contours.size,
                                                                        topContours = ContourStats.topByArea(contours, 3),
                                                                    )
                                                                    contourCache = contours
                                                                    contourFailure = null
                                                                    isPreparingFrame = false
                                                                    isRunningContours = false
                                                                }
                                                            }
                                                                is PageDetectOutcomeV1.Failure -> {
                                                                    withContext(Dispatchers.Main) {
                                                                        contourFailure = contoursOutcome.failure
                                                                        logPageDetectFailure(
                                                                            logger = diagnosticsLogger,
                                                                            failure = contoursOutcome.failure,
                                                                            projectId = activeProjectId,
                                                                            onLogged = {
                                                                                lastEventName = DrawingImportEvent.ERROR.eventName
                                                                            },
                                                                        )
                                                                        isPreparingFrame = false
                                                                        isRunningContours = false
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    },
                                                ) {
                                                    Text(text = "Run edges+contours")
                                                }
                                                Button(
                                                    onClick = {
                                                        if (rawFile == null) {
                                                            coroutineScope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    "Raw image missing; recapture first.",
                                                                )
                                                            }
                                                            return@Button
                                                        }
                                                        quadSelectionOutcome = null
                                                        quadSelectionFailure = null
                                                        orderFailure = null
                                                        isPreparingFrame = true
                                                        isSelectingQuad = true
                                                        rectifiedSizeOutcome = null
                                                        pipelineResult = null
                                                        coroutineScope.launch(Dispatchers.Default) {
                                                            val frameOutcome = pageDetectFrame?.let {
                                                                PageDetectOutcomeV1.Success(it)
                                                            } ?: pageDetectPreprocessor.preprocess(
                                                                PageDetectInput(rawImageFile = rawFile),
                                                            )
                                                            val frame = when (frameOutcome) {
                                                                is PageDetectOutcomeV1.Success -> frameOutcome.value
                                                                is PageDetectOutcomeV1.Failure -> {
                                                                    withContext(Dispatchers.Main) {
                                                                        preprocessFailure = frameOutcome.failure
                                                                        logPageDetectFailure(
                                                                            logger = diagnosticsLogger,
                                                                            failure = frameOutcome.failure,
                                                                            projectId = activeProjectId,
                                                                            onLogged = {
                                                                                lastEventName = DrawingImportEvent.ERROR.eventName
                                                                            },
                                                                        )
                                                                        isPreparingFrame = false
                                                                        isSelectingQuad = false
                                                                    }
                                                                    return@launch
                                                                }
                                                            }
                                                            val contoursOutcome = if (contourCache.isNotEmpty()) {
                                                                PageDetectOutcomeV1.Success(contourCache)
                                                            } else {
                                                                val edgesOutcome = edgeDetector.detect(frame)
                                                                val edgeMap = when (edgesOutcome) {
                                                                    is PageDetectOutcomeV1.Success -> edgesOutcome.value
                                                                    is PageDetectOutcomeV1.Failure -> {
                                                                        withContext(Dispatchers.Main) {
                                                                            quadSelectionFailure = edgesOutcome.failure
                                                                            logPageDetectFailure(
                                                                                logger = diagnosticsLogger,
                                                                                failure = edgesOutcome.failure,
                                                                                projectId = activeProjectId,
                                                                                onLogged = {
                                                                                    lastEventName = DrawingImportEvent.ERROR.eventName
                                                                                },
                                                                            )
                                                                            isPreparingFrame = false
                                                                            isSelectingQuad = false
                                                                        }
                                                                        return@launch
                                                                    }
                                                                }
                                                                contourExtractor.extract(edgeMap)
                                                            }
                                                            val contours = when (contoursOutcome) {
                                                                is PageDetectOutcomeV1.Success -> contoursOutcome.value
                                                                is PageDetectOutcomeV1.Failure -> {
                                                                    withContext(Dispatchers.Main) {
                                                                        quadSelectionFailure = contoursOutcome.failure
                                                                        logPageDetectFailure(
                                                                            logger = diagnosticsLogger,
                                                                            failure = contoursOutcome.failure,
                                                                            projectId = activeProjectId,
                                                                            onLogged = {
                                                                                lastEventName = DrawingImportEvent.ERROR.eventName
                                                                            },
                                                                        )
                                                                        isPreparingFrame = false
                                                                        isSelectingQuad = false
                                                                    }
                                                                    return@launch
                                                                }
                                                            }
                                                            val quadOutcome = quadSelector.select(contours, frame.width, frame.height)
                                                            val sizeOutcome = when (quadOutcome) {
                                                                is PageDetectOutcomeV1.Success -> {
                                                                    when (val orderedOutcome = CornerOrderingV1.order(quadOutcome.value.points)) {
                                                                        is PageDetectOutcomeV1.Success -> {
                                                                            RectifySizePolicyV1.compute(
                                                                                orderedOutcome.value,
                                                                                RectifySizeParamsV1(
                                                                                    maxSide = DrawingImportGuardrailsV1.MAX_RECTIFIED_SIDE,
                                                                                    minSide = 256,
                                                                                    enforceEven = true,
                                                                                    maxPixels = DrawingImportGuardrailsV1.MAX_RECTIFIED_PIXELS,
                                                                                ),
                                                                            )
                                                                        }
                                                                        is PageDetectOutcomeV1.Failure -> {
                                                                            PageDetectOutcomeV1.Failure(orderedOutcome.failure)
                                                                        }
                                                                    }
                                                                }
                                                                is PageDetectOutcomeV1.Failure -> null
                                                            }
                                                            val orderingOutcome = when (quadOutcome) {
                                                                is PageDetectOutcomeV1.Success -> {
                                                                    CornerOrderingV1.order(quadOutcome.value.points)
                                                                }
                                                                is PageDetectOutcomeV1.Failure -> null
                                                            }
                                                            val exposureMetrics = runCatching {
                                                                val bitmap = frameToBitmap(frame)
                                                                try {
                                                                    QualityMetricsV1.exposure(bitmap)
                                                                } finally {
                                                                    bitmap.recycle()
                                                                }
                                                            }.getOrNull()
                                                            withContext(Dispatchers.Main) {
                                                                pageDetectFrame = frame
                                                                pageDetectInfo = PageDetectFrameInfo(
                                                                    width = frame.width,
                                                                    height = frame.height,
                                                                    downscaleFactor = frame.downscaleFactor,
                                                                    rotationAppliedDeg = frame.rotationAppliedDeg,
                                                                )
                                                                preprocessFailure = null
                                                                contourCache = contours
                                                                contourInfo = contourInfo ?: ContourDebugInfo(
                                                                    totalContours = contours.size,
                                                                    topContours = ContourStats.topByArea(contours, 3),
                                                                )
                                                                quadSelectionOutcome = quadOutcome
                                                                contourFailure = null
                                                                rectifiedSizeOutcome = sizeOutcome
                                                                pipelineResult = when (quadOutcome) {
                                                                    is PageDetectOutcomeV1.Success -> {
                                                                    val orderedCorners = QualityOrderedCornersV1.fromPoints(
                                                                        quadOutcome.value.points,
                                                                    )
                                                                    orderedCorners?.let { corners ->
                                                                        val metrics = QualityMetricsV1.skewFromQuad(
                                                                            corners,
                                                                            frame.width,
                                                                            frame.height,
                                                                        )
                                                                        val blurVariance = resolvedBlurVariance(
                                                                            screenState = screenState,
                                                                            captureMeta = loadedCaptureMeta,
                                                                        )
                                                                        val exposure = exposureMetrics
                                                                            ?: ExposureMetricsV1(
                                                                                meanY = 0.0,
                                                                                clipLowPct = 0.0,
                                                                                clipHighPct = 0.0,
                                                                            )
                                                                        val qualityGate = QualityGateV1.evaluate(
                                                                            blurVariance = blurVariance,
                                                                            exposure = exposure,
                                                                            skew = metrics,
                                                                        )
                                                                        DrawingImportPipelineResultV1(
                                                                            orderedCorners = corners,
                                                                            refinedCorners = null,
                                                                            imageWidth = frame.width,
                                                                            imageHeight = frame.height,
                                                                            skewMetrics = metrics,
                                                                            blurVariance = blurVariance,
                                                                            exposureMetrics = exposure,
                                                                            qualityGate = qualityGate,
                                                                        )
                                                                    }
                                                                }
                                                                    is PageDetectOutcomeV1.Failure -> null
                                                                }
                                                                isPreparingFrame = false
                                                                isSelectingQuad = false
                                                                orderedCorners = when (orderingOutcome) {
                                                                    is PageDetectOutcomeV1.Success -> {
                                                                        orderFailure = null
                                                                        orderingOutcome.value
                                                                    }
                                                                    is PageDetectOutcomeV1.Failure -> {
                                                                        orderFailure = orderingOutcome.failure
                                                                        logPageDetectFailure(
                                                                            logger = diagnosticsLogger,
                                                                            failure = orderingOutcome.failure,
                                                                            projectId = activeProjectId,
                                                                            onLogged = {
                                                                                lastEventName = DrawingImportEvent.ERROR.eventName
                                                                            },
                                                                        )
                                                                        null
                                                                    }
                                                                    null -> null
                                                                }
                                                                refineOutcome = null
                                                                refineFailure = null
                                                                quadSelectionFailure = if (quadOutcome is PageDetectOutcomeV1.Failure) {
                                                                    quadOutcome.failure.also { failure ->
                                                                        logPageDetectFailure(
                                                                            logger = diagnosticsLogger,
                                                                            failure = failure,
                                                                            projectId = activeProjectId,
                                                                            onLogged = {
                                                                                lastEventName = DrawingImportEvent.ERROR.eventName
                                                                            },
                                                                        )
                                                                    }
                                                                } else {
                                                                    null
                                                                }
                                                            }
                                                        }
                                                    },
                                                ) {
                                                    Text(text = "Select page quad")
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            ) {
                                                Button(
                                                    onClick = {
                                                        val frame = pageDetectFrame
                                                        val ordered = orderedCorners
                                                        if (frame == null || ordered == null) {
                                                            coroutineScope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    "Run quad selection before refining corners.",
                                                                )
                                                            }
                                                            return@Button
                                                        }
                                                        refineFailure = null
                                                        coroutineScope.launch(Dispatchers.Default) {
                                                            val refineResult = CornerRefinerV1.refine(frame, ordered, refineParams)
                                                            withContext(Dispatchers.Main) {
                                                                refineOutcome = refineResult
                                                                when (refineResult) {
                                                                    is PageDetectOutcomeV1.Success -> {
                                                                        refineFailure = null
                                                                    }
                                                                    is PageDetectOutcomeV1.Failure -> {
                                                                        refineFailure = refineResult.failure
                                                                        logPageDetectFailure(
                                                                            logger = diagnosticsLogger,
                                                                            failure = refineResult.failure,
                                                                            projectId = activeProjectId,
                                                                            onLogged = {
                                                                                lastEventName = DrawingImportEvent.ERROR.eventName
                                                                            },
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    },
                                                ) {
                                                    Text(text = "Refine corners")
                                                }
                                            }
                                            if (isPreparingFrame) {
                                                Text(
                                                    text = if (isRunningContours) {
                                                        "Running edges+contours..."
                                                    } else if (isSelectingQuad) {
                                                        "Selecting page quad..."
                                                    } else {
                                                        "Preparing frame..."
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                            pageDetectInfo?.let { info ->
                                                Text(
                                                    text = "Frame: ${info.width}x${info.height} " +
                                                        "• downscale ${"%.2f".format(Locale.US, info.downscaleFactor)} " +
                                                        "• rotation ${info.rotationAppliedDeg}°",
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                            preprocessFailure?.let { failure ->
                                                Text(
                                                    text = formatFailureLabel("Preprocess", failure),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                )
                                            }
                                            contourInfo?.let { info ->
                                                Text(
                                                    text = "Contours: ${info.totalContours}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                                info.topContours.forEachIndexed { index, contour ->
                                                    Text(
                                                        text = formatContourLabel(index, contour),
                                                        style = MaterialTheme.typography.bodySmall,
                                                    )
                                                }
                                            }
                                            contourFailure?.let { failure ->
                                                Text(
                                                    text = formatFailureLabel("Edges/contours", failure),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                )
                                            }
                                            quadSelectionOutcome?.let { result ->
                                                when (result) {
                                                    is PageDetectOutcomeV1.Success -> {
                                                        Text(
                                                            text = formatQuadLabel(result.value),
                                                            style = MaterialTheme.typography.bodySmall,
                                                        )
                                                    }
                                                    is PageDetectOutcomeV1.Failure -> {
                                                        Text(
                                                            text = formatFailureLabel("Quad select", result.failure),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.error,
                                                        )
                                                    }
                                                }
                                            }
                                            rectifiedSizeOutcome?.let { outcome ->
                                                when (outcome) {
                                                    is PageDetectOutcomeV1.Success -> {
                                                        Text(
                                                            text = formatRectifiedSizeLabel(outcome.value),
                                                            style = MaterialTheme.typography.bodySmall,
                                                        )
                                                    }
                                                    is PageDetectOutcomeV1.Failure -> {
                                                        Text(
                                                            text = formatRectifiedFailureLabel(outcome.failure),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.error,
                                                        )
                                                    }
                                                }
                                            pipelineResult?.let { result ->
                                                val metrics = result.skewMetrics
                                                val blurVariance = resolvedBlurVariance(
                                                    screenState = screenState,
                                                    captureMeta = loadedCaptureMeta,
                                                )
                                                Text(
                                                    text = formatSkewMetrics(metrics),
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                                Text(
                                                    text = formatBlurVarianceLabel(blurVariance),
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                                Text(
                                                    text = formatExposureMetrics(result.exposureMetrics),
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                                result.qualityGate?.let { gate ->
                                                    Text(
                                                        text = formatQualityGateDecision(gate),
                                                        style = MaterialTheme.typography.bodySmall,
                                                    )
                                                    Text(
                                                        text = formatQualityGateReasons(gate),
                                                        style = MaterialTheme.typography.bodySmall,
                                                    )
                                                }
                                            }
                                            quadSelectionFailure?.let { failure ->
                                                Text(
                                                    text = formatFailureLabel("Quad selection", failure),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                )
                                            }
                                            orderFailure?.let { failure ->
                                                Text(
                                                    text = formatFailureLabel("Ordering", failure),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                )
                                            }
                                            orderedCorners?.let { corners ->
                                                Text(
                                                    text = formatOrderedCornersLabel(corners),
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                            refineOutcome?.let { result ->
                                                when (result) {
                                                    is PageDetectOutcomeV1.Success -> {
                                                        Text(
                                                            text = formatRefineLabel(result.value),
                                                            style = MaterialTheme.typography.bodySmall,
                                                        )
                                                    }
                                                    is PageDetectOutcomeV1.Failure -> {
                                                        Text(
                                                            text = formatFailureLabel("Refine", result.failure),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.error,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (BuildConfig.DEBUG) {
                        DebugDetailsCard(
                            lastEventName = lastEventName,
                            lastErrorCode = lastErrorCode,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (uiState is CameraPermissionState.Ready) {
                        if (screenState is DrawingImportUiState.Saved) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val session = (screenState as? DrawingImportUiState.Saved)?.session
                                        logDiagnosticsEvent(
                                            logger = diagnosticsLogger,
                                            event = DrawingImportEvent.SESSION_RESET,
                                            state = "retake",
                                            projectId = session?.projectId,
                                            onLogged = {
                                                lastEventName = DrawingImportEvent.SESSION_RESET.eventName
                                                lastErrorCode = null
                                            },
                                        )
                                        session?.projectDir?.deleteRecursively()
                                        activeProjectId = null
                                        imageError = false
                                        pageDetectInfo = null
                                        pageDetectFrame = null
                                        preprocessFailure = null
                                        isPreparingFrame = false
                                        contourInfo = null
                                        contourFailure = null
                                        isRunningContours = false
                                        contourCache = emptyList()
                                        quadSelectionOutcome = null
                                        quadSelectionFailure = null
                                        orderFailure = null
                                        isSelectingQuad = false
                                        rectifiedSizeOutcome = null
                                        orderedCorners = null
                                        refineOutcome = null
                                        refineFailure = null
                                        pipelineResult = null
                                        pipelineJob?.cancel()
                                        processState = DrawingImportProcessState.Idle
                                        screenState = DrawingImportUiState.Ready
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary,
                                    ),
                                ) {
                                    Text(text = "Retake")
                                }
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Next: page detection (coming in PR07+)",
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                                ) {
                                    Text(text = "Continue")
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        if (screenState == DrawingImportUiState.Capturing) return@launch
                                        screenState = DrawingImportUiState.Capturing
                                        val captureProjectId = activeProjectId ?: UUID.randomUUID().toString()
                                        activeProjectId = captureProjectId
                                        pageDetectInfo = null
                                        pageDetectFrame = null
                                        preprocessFailure = null
                                        isPreparingFrame = false
                                        contourInfo = null
                                        contourFailure = null
                                        isRunningContours = false
                                        contourCache = emptyList()
                                        quadSelectionOutcome = null
                                        quadSelectionFailure = null
                                        orderFailure = null
                                        isSelectingQuad = false
                                        rectifiedSizeOutcome = null
                                        orderedCorners = null
                                        refineOutcome = null
                                        refineFailure = null
                                        pipelineJob?.cancel()
                                        processState = DrawingImportProcessState.Idle
                                        logDiagnosticsEvent(
                                            logger = diagnosticsLogger,
                                            event = DrawingImportEvent.CAPTURE_START,
                                            state = "start",
                                            projectId = captureProjectId,
                                            onLogged = {
                                                lastEventName = DrawingImportEvent.CAPTURE_START.eventName
                                            },
                                        )
                                        val tempDir = File(context.cacheDir, "drawing-import")
                                        if (!tempDir.exists()) {
                                            tempDir.mkdirs()
                                        }
                                        val tempFile = File(tempDir, "raw_capture.jpg")
                                        try {
                                            val targetRotation = previewView?.display?.rotation
                                            cameraSession.captureImage(tempFile, targetRotation)
                                        } catch (error: Throwable) {
                                            val code = DrawingImportErrorMapper.fromThrowable(error)
                                            screenState = DrawingImportUiState.Error(
                                                DrawingImportErrorMapper.messageFor(code),
                                            )
                                            logDiagnosticsError(
                                                logger = diagnosticsLogger,
                                                projectId = captureProjectId,
                                                code = code,
                                                onLogged = {
                                                    lastEventName = DrawingImportEvent.ERROR.eventName
                                                    lastErrorCode = code
                                                },
                                            )
                                            snackbarHostState.showSnackbar(
                                                DrawingImportErrorMapper.messageFor(code),
                                            )
                                            tempFile.delete()
                                            return@launch
                                        }
                                        try {
                                            val projectDir = File(
                                                DrawingImportArtifacts.artifactsRoot(context),
                                                captureProjectId,
                                            )
                                            val store = FileArtifactStoreV1(projectDir)
                                            val rawEntry = store.writeBytes(
                                                kind = ArtifactKindV1.RAW_IMAGE,
                                                relPath = DrawingImportArtifacts.rawImageRelPath(),
                                                bytes = tempFile.readBytes(),
                                                mime = "image/jpeg",
                                            )
                                            val manifestEntry = ManifestWriterV1().write(
                                                projectDir,
                                                DrawingImportArtifacts.buildManifest(
                                                    projectId = captureProjectId,
                                                    artifacts = listOf(rawEntry),
                                                ),
                                            )
                                            imageError = false
                                            screenState = DrawingImportUiState.Saved(
                                                DrawingImportSession(
                                                    projectId = captureProjectId,
                                                    projectDir = projectDir,
                                                    artifacts = listOf(rawEntry, manifestEntry),
                                                )
                                            )
                                            logDiagnosticsEvent(
                                                logger = diagnosticsLogger,
                                                event = DrawingImportEvent.CAPTURE_SAVED,
                                                state = "saved",
                                                projectId = captureProjectId,
                                                extras = mapOf(
                                                    "artifactRawRelPath" to rawEntry.relPath,
                                                    "artifactRawSha" to shortenSha(rawEntry.sha256),
                                                    "artifactManifestRelPath" to manifestEntry.relPath,
                                                    "artifactManifestSha" to shortenSha(manifestEntry.sha256),
                                                ),
                                                onLogged = {
                                                    lastEventName = DrawingImportEvent.CAPTURE_SAVED.eventName
                                                },
                                            )
                                            snackbarHostState.showSnackbar("Raw saved")
                                        } catch (error: Throwable) {
                                            val code = DrawingImportErrorMapper.fromThrowable(error)
                                            val message = DrawingImportErrorMapper.messageFor(code)
                                            screenState = DrawingImportUiState.Error(message)
                                            logDiagnosticsError(
                                                logger = diagnosticsLogger,
                                                projectId = captureProjectId,
                                                code = code,
                                                onLogged = {
                                                    lastEventName = DrawingImportEvent.ERROR.eventName
                                                    lastErrorCode = code
                                                },
                                            )
                                            snackbarHostState.showSnackbar(message)
                                        } finally {
                                            tempFile.delete()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            ) {
                                Text(text = "Capture")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    logDiagnosticsEvent(
                                        logger = diagnosticsLogger,
                                        event = DrawingImportEvent.SESSION_RESET,
                                        state = "reset",
                                        projectId = activeProjectId,
                                        onLogged = {
                                            lastEventName = DrawingImportEvent.SESSION_RESET.eventName
                                            lastErrorCode = null
                                        },
                                    )
                                    activeProjectId = null
                                    imageError = false
                                    pageDetectInfo = null
                                    pageDetectFrame = null
                                    preprocessFailure = null
                                    isPreparingFrame = false
                                    contourInfo = null
                                    contourFailure = null
                                    isRunningContours = false
                                    contourCache = emptyList()
                                    quadSelectionOutcome = null
                                    quadSelectionFailure = null
                                    orderFailure = null
                                    isSelectingQuad = false
                                    isSavingOverlay = false
                                    orderedCorners = null
                                    refineOutcome = null
                                    refineFailure = null
                                    pipelineJob?.cancel()
                                    processState = DrawingImportProcessState.Idle
                                    screenState = DrawingImportUiState.Ready
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary,
                                ),
                            ) {
                                Text(text = "Reset")
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(bottom = 12.dp),
            )
        }
    }

    DisposableEffect(Unit) {
        logDiagnosticsEvent(
            logger = diagnosticsLogger,
            event = DrawingImportEvent.SCREEN_OPENED,
            state = "opened",
            projectId = activeProjectId,
            onLogged = { lastEventName = DrawingImportEvent.SCREEN_OPENED.eventName },
        )
        onDispose { }
    }
}

private data class PageDetectFrameInfo(
    val width: Int,
    val height: Int,
    val downscaleFactor: Double,
    val rotationAppliedDeg: Int,
)

private data class ContourDebugInfo(
    val totalContours: Int,
    val topContours: List<ContourV1>,
)

private fun formatContourLabel(index: Int, contour: ContourV1): String {
    val area = "%.1f".format(Locale.US, contour.area)
    val bbox = "x=${contour.bbox.x}, y=${contour.bbox.y}, w=${contour.bbox.width}, h=${contour.bbox.height}"
    return "#${index + 1} area=$area • bbox=($bbox)"
}

private fun formatQuadLabel(candidate: PageQuadCandidate): String {
    val area = "%.1f".format(Locale.US, candidate.contourArea)
    val score = "%.2f".format(Locale.US, candidate.score)
    val points = candidate.points.joinToString(prefix = "[", postfix = "]") { "(${it.x},${it.y})" }
    return "Quad area=$area • score=$score • points=$points"
}

private fun formatRectifiedSizeLabel(size: RectifiedSizeV1): String {
    return "Rectified size=${size.width}x${size.height}"
}

private fun formatSkewMetrics(metrics: SkewMetricsV1): String {
    val angleMax = "%.2f".format(Locale.US, metrics.angleMaxAbsDeg)
    val angleMean = "%.2f".format(Locale.US, metrics.angleMeanAbsDeg)
    val keystoneW = "%.3f".format(Locale.US, metrics.keystoneWidthRatio)
    val keystoneH = "%.3f".format(Locale.US, metrics.keystoneHeightRatio)
    val pageFill = "%.3f".format(Locale.US, metrics.pageFillRatio)
    return "Angle dev (max/mean): $angleMax°/$angleMean° • " +
        "Keystone W/H: $keystoneW/$keystoneH • Page fill: $pageFill • Status: ${metrics.status.name}"
}

private fun resolvedBlurVariance(
    screenState: DrawingImportUiState,
    captureMeta: CaptureMetaV1?,
): Double? {
    val sessionBlur = (screenState as? DrawingImportUiState.Saved)
        ?.session
        ?.rectifiedQualityMetrics
        ?.blurVariance
    val metaBlur = captureMeta?.metrics?.blurVariance
    return sessionBlur ?: metaBlur
}

private fun formatBlurVarianceLabel(blurVariance: Double?): String {
    val formatted = blurVariance?.let { value ->
        String.format(Locale.US, "%.2f", value)
    } ?: "—"
    return "Blur (VarLap): $formatted"
}

private fun formatExposureMetrics(metrics: ExposureMetricsV1): String {
    val meanY = "%.1f".format(Locale.US, metrics.meanY)
    val clipLow = "%.2f".format(Locale.US, metrics.clipLowPct)
    val clipHigh = "%.2f".format(Locale.US, metrics.clipHighPct)
    return "Mean Y: $meanY • Clipped low: $clipLow% • Clipped high: $clipHigh%"
}

private fun formatQualityGateDecision(result: QualityGateResultV1): String {
    return "Quality: ${result.decision.name}"
}

private fun formatQualityGateReasons(result: QualityGateResultV1): String {
    if (result.reasons.isEmpty()) return "Reasons: —"
    val formatted = result.reasons.joinToString { code ->
        val hint = QualityGateV1.hintFor(code)
        if (hint == null) {
            code.name
        } else {
            "${code.name} ($hint)"
        }
    }
    return "Reasons: $formatted"
}

private fun formatFailureLabel(label: String, failure: PageDetectFailureV1): String {
    val guidance = "Try retake with better lighting and keep the page flat."
    val debugCode = "${failure.stage.name}:${failure.code.name}"
    val debugMessage = failure.debugMessage?.let { " • detail=$it" } ?: ""
    return "$label failed: stage=${failure.stage.name} • code=${failure.code.name} • $guidance • debug=$debugCode$debugMessage"
}

private fun formatRectifiedFailureLabel(failure: PageDetectFailureV1): String {
    return formatFailureLabel("Rectified size", failure)
}

private fun formatStageLabel(stage: PageDetectStageV1): String {
    return when (stage) {
        PageDetectStageV1.LOAD_UPRIGHT -> "Load upright bitmap"
        PageDetectStageV1.PREPROCESS -> "Preprocess"
        PageDetectStageV1.EDGES -> "Detect edges"
        PageDetectStageV1.CONTOURS -> "Extract contours"
        PageDetectStageV1.QUAD_SELECT -> "Select quad"
        PageDetectStageV1.ORDER -> "Order corners"
        PageDetectStageV1.REFINE -> "Refine corners"
        PageDetectStageV1.RECTIFY_SIZE -> "Rectified size"
        PageDetectStageV1.RECTIFY -> "Rectify bitmap"
        PageDetectStageV1.SAVE -> "Save artifacts"
    }
}

private fun formatOrderedCornersLabel(corners: PreprocessOrderedCornersV1): String {
    val points = corners.toList()
        .joinToString(prefix = "[", postfix = "]") { "(${it.x.formatPx()},${it.y.formatPx()})" }
    return "Ordered corners (TL/TR/BR/BL): $points"
}

private fun frameToBitmap(frame: PageDetectFrame): Bitmap {
    val expectedSize = frame.width * frame.height
    require(frame.gray.size == expectedSize) {
        "Frame gray data mismatch: expected $expectedSize bytes, got ${frame.gray.size}."
    }
    val pixels = IntArray(expectedSize)
    for (i in 0 until expectedSize) {
        val gray = frame.gray[i].toInt() and 0xFF
        pixels[i] = 0xFF000000.toInt() or (gray shl 16) or (gray shl 8) or gray
    }
    return Bitmap.createBitmap(
        pixels,
        frame.width,
        frame.height,
        Bitmap.Config.ARGB_8888,
    )
}

private fun encodePng(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

private fun formatRefineLabel(result: RefineResultV1): String {
    val deltas = result.deltasPx.joinToString(prefix = "[", postfix = "]") { it.formatDeltaPx() }
    val status = result.status.name
    return "Refine $status • deltas=$deltas"
}

private fun Double.formatDeltaPx(): String = "%.2fpx".format(Locale.US, this)

private fun Double.formatPx(): String = "%.1f".format(Locale.US, this)

private sealed interface CameraPermissionState {
    data object NeedsPermission : CameraPermissionState
    data object PermissionDenied : CameraPermissionState
    data object Ready : CameraPermissionState
    data class CameraError(val message: String) : CameraPermissionState
}

@Composable
private fun ArtifactSummaryRow(
    label: String,
    relPath: String,
    sha256: String,
    pixelSha256: String? = null,
    sizeLabel: String? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = relPath,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "SHA: ${shortenSha(sha256)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        pixelSha256?.let { pixelSha ->
            Text(
                text = "Pixel SHA: ${shortenSha(pixelSha)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        sizeLabel?.let { size ->
            Text(
                text = "Size: $size",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

private fun shortenSha(sha256: String, max: Int = 12): String {
    return if (sha256.length <= max) sha256 else sha256.take(max)
}

@Composable
private fun DebugDetailsCard(
    lastEventName: String?,
    lastErrorCode: DrawingImportErrorCode?,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Debug details",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Last event: ${lastEventName ?: "none"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Last error: ${lastErrorCode?.name ?: "none"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun logDiagnosticsEvent(
    logger: DrawingImportEventLogger,
    event: DrawingImportEvent,
    state: String,
    projectId: String?,
    extras: Map<String, String> = emptyMap(),
    onLogged: () -> Unit,
) {
    logger.logEvent(
        event = event,
        state = state,
        projectId = projectId,
        extras = extras,
    )
    onLogged()
}

private fun logPageDetectFailure(
    logger: DrawingImportEventLogger,
    failure: PageDetectFailureV1,
    projectId: String?,
    onLogged: () -> Unit,
) {
    val extras = buildMap {
        put("detectStage", failure.stage.name)
        put("detectCode", failure.code.name)
        failure.debugMessage?.let { put("debugMessage", it) }
    }
    logger.logEvent(
        event = DrawingImportEvent.ERROR,
        state = "page_detect_failure",
        projectId = projectId,
        errorCode = DrawingImportErrorCode.UNKNOWN,
        message = "${failure.stage.name}:${failure.code.name}",
        extras = extras,
    )
    onLogged()
}

private fun logDiagnosticsError(
    logger: DrawingImportEventLogger,
    projectId: String?,
    code: DrawingImportErrorCode,
    onLogged: () -> Unit,
) {
    logger.logEvent(
        event = DrawingImportEvent.ERROR,
        state = "error",
        projectId = projectId,
        errorCode = code,
        message = DrawingImportErrorMapper.messageFor(code),
    )
    onLogged()
}
