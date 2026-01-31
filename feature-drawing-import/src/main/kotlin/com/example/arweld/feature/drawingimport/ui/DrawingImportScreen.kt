package com.example.arweld.feature.drawingimport.ui

import android.Manifest
import android.content.pm.PackageManager
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
import com.example.arweld.core.drawing2d.artifacts.io.v1.FileArtifactStoreV1
import com.example.arweld.core.drawing2d.artifacts.io.v1.ManifestWriterV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.feature.drawingimport.BuildConfig
import com.example.arweld.feature.drawingimport.artifacts.DrawingImportArtifacts
import com.example.arweld.feature.drawingimport.camera.CameraSession
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportErrorCode
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportErrorMapper
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportEvent
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportEventLogger
import com.example.arweld.feature.drawingimport.preprocess.ContourStats
import com.example.arweld.feature.drawingimport.preprocess.ContourV1
import com.example.arweld.feature.drawingimport.preprocess.CornerOrderingV1
import com.example.arweld.feature.drawingimport.preprocess.CornerRefinerV1
import com.example.arweld.feature.drawingimport.preprocess.OrderedCornersV1
import com.example.arweld.feature.drawingimport.quality.DrawingImportPipelineResultV1
import com.example.arweld.feature.drawingimport.quality.OrderedCornersV1
import com.example.arweld.feature.drawingimport.quality.QualityMetricsV1
import com.example.arweld.feature.drawingimport.quality.SkewMetricsV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFailure
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFailureCode
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFrame
import com.example.arweld.feature.drawingimport.preprocess.PageDetectInput
import com.example.arweld.feature.drawingimport.preprocess.PageDetectContourExtractor
import com.example.arweld.feature.drawingimport.preprocess.PageDetectEdgeDetector
import com.example.arweld.feature.drawingimport.preprocess.PageDetectPreprocessor
import com.example.arweld.feature.drawingimport.preprocess.PageQuadCandidate
import com.example.arweld.feature.drawingimport.preprocess.PageQuadSelectionResult
import com.example.arweld.feature.drawingimport.preprocess.PageQuadSelector
import com.example.arweld.feature.drawingimport.preprocess.OrderedCornersV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectOutcomeV1
import com.example.arweld.feature.drawingimport.preprocess.RectifiedSizeV1
import com.example.arweld.feature.drawingimport.preprocess.RectifySizeParamsV1
import com.example.arweld.feature.drawingimport.preprocess.RectifySizePolicyV1
import com.example.arweld.feature.drawingimport.preprocess.RefineParamsV1
import com.example.arweld.feature.drawingimport.preprocess.RefineResultV1
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
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
    var pageDetectError by remember { mutableStateOf<String?>(null) }
    var isPreparingFrame by remember { mutableStateOf(false) }
    var contourInfo by remember { mutableStateOf<ContourDebugInfo?>(null) }
    var contourError by remember { mutableStateOf<String?>(null) }
    var isRunningContours by remember { mutableStateOf(false) }
    var contourCache by remember { mutableStateOf<List<ContourV1>>(emptyList()) }
    var quadSelectionResult by remember { mutableStateOf<PageQuadSelectionResult?>(null) }
    var quadSelectionError by remember { mutableStateOf<String?>(null) }
    var isSelectingQuad by remember { mutableStateOf(false) }
    var rectifiedSizeOutcome by remember { mutableStateOf<PageDetectOutcomeV1<RectifiedSizeV1>?>(null) }
    var orderedCorners by remember { mutableStateOf<OrderedCornersV1?>(null) }
    var refineResult by remember { mutableStateOf<RefineResultV1?>(null) }
    var refineError by remember { mutableStateOf<String?>(null) }
    var pipelineResult by remember { mutableStateOf<DrawingImportPipelineResultV1?>(null) }
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

    DisposableEffect(shouldBindCamera) {
        if (!shouldBindCamera) {
            hasLoggedCameraReady = false
        }
        onDispose { }
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
                                    val rawFile = rawEntry?.let {
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
                                        ArtifactSummaryRow(
                                            label = "Manifest",
                                            relPath = manifestEntry?.relPath ?: "Missing",
                                            sha256 = manifestEntry?.sha256 ?: "--",
                                        )
                                    }
                                    if (BuildConfig.DEBUG) {
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
                                                        pageDetectError = null
                                                        isPreparingFrame = true
                                                        contourInfo = null
                                                        contourError = null
                                                        isRunningContours = false
                                                        contourCache = emptyList()
                                                        quadSelectionResult = null
                                                        quadSelectionError = null
                                                        isSelectingQuad = false
                                                        rectifiedSizeOutcome = null
                                                        orderedCorners = null
                                                        refineResult = null
                                                        refineError = null
                                                        pipelineResult = null
                                                        coroutineScope.launch(Dispatchers.Default) {
                                                            runCatching {
                                                                pageDetectPreprocessor.preprocess(
                                                                    PageDetectInput(rawImageFile = rawFile),
                                                                )
                                                            }.onSuccess { frame ->
                                                                withContext(Dispatchers.Main) {
                                                                    pageDetectFrame = frame
                                                                    pageDetectInfo = PageDetectFrameInfo(
                                                                        width = frame.width,
                                                                        height = frame.height,
                                                                        downscaleFactor = frame.downscaleFactor,
                                                                        rotationAppliedDeg = frame.rotationAppliedDeg,
                                                                    )
                                                                    isPreparingFrame = false
                                                                }
                                                            }.onFailure { error ->
                                                                withContext(Dispatchers.Main) {
                                                                    pageDetectError = error.message ?: "Failed to preprocess."
                                                                    isPreparingFrame = false
                                                                }
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
                                                        contourError = null
                                                        pageDetectError = null
                                                        isPreparingFrame = true
                                                        isRunningContours = true
                                                        quadSelectionResult = null
                                                        quadSelectionError = null
                                                        isSelectingQuad = false
                                                        rectifiedSizeOutcome = null
                                                        coroutineScope.launch(Dispatchers.Default) {
                                                            runCatching {
                                                                val frame = pageDetectFrame ?: pageDetectPreprocessor.preprocess(
                                                                    PageDetectInput(rawImageFile = rawFile),
                                                                )
                                                                val edges = edgeDetector.detect(frame)
                                                                val contours = contourExtractor.extract(edges)
                                                                Triple(frame, contours, ContourStats.topByArea(contours, 3))
                                                            }.onSuccess { (frame, contours, topContours) ->
                                                                withContext(Dispatchers.Main) {
                                                                    pageDetectFrame = frame
                                                                    pageDetectInfo = PageDetectFrameInfo(
                                                                        width = frame.width,
                                                                        height = frame.height,
                                                                        downscaleFactor = frame.downscaleFactor,
                                                                        rotationAppliedDeg = frame.rotationAppliedDeg,
                                                                    )
                                                                    contourInfo = ContourDebugInfo(
                                                                        totalContours = contours.size,
                                                                        topContours = topContours,
                                                                    )
                                                                    contourCache = contours
                                                                    isPreparingFrame = false
                                                                    isRunningContours = false
                                                                }
                                                            }.onFailure { error ->
                                                                withContext(Dispatchers.Main) {
                                                                    contourError = error.message ?: "Failed to run edges+contours."
                                                                    isPreparingFrame = false
                                                                    isRunningContours = false
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
                                                        quadSelectionResult = null
                                                        quadSelectionError = null
                                                        isPreparingFrame = true
                                                        isSelectingQuad = true
                                                        rectifiedSizeOutcome = null
                                                        pipelineResult = null
                                                        coroutineScope.launch(Dispatchers.Default) {
                                                            runCatching {
                                                                val frame = pageDetectFrame ?: pageDetectPreprocessor.preprocess(
                                                                    PageDetectInput(rawImageFile = rawFile),
                                                                )
                                                                val contours = if (contourCache.isNotEmpty()) {
                                                                    contourCache
                                                                } else {
                                                                    val edges = edgeDetector.detect(frame)
                                                                    contourExtractor.extract(edges)
                                                                }
                                                                val result = quadSelector.select(contours, frame.width, frame.height)
                                                                val sizeOutcome = when (result) {
                                                                    is PageQuadSelectionResult.Success -> {
                                                                        val orderedCorners = OrderedCornersV1.fromPoints(result.candidate.points)
                                                                        if (orderedCorners == null) {
                                                                            PageDetectOutcomeV1.Failure(
                                                                                PageDetectFailure(
                                                                                    code = PageDetectFailureCode.DEGENERATE_QUAD,
                                                                                    message = "Unable to order quad corners.",
                                                                                ),
                                                                            )
                                                                        } else {
                                                                            RectifySizePolicyV1.compute(
                                                                                orderedCorners,
                                                                                RectifySizeParamsV1(
                                                                                    maxSide = 2048,
                                                                                    minSide = 256,
                                                                                    enforceEven = true,
                                                                                ),
                                                                            )
                                                                        }
                                                                    }
                                                                    is PageQuadSelectionResult.Failure -> null
                                                                }
                                                                QuadSelectionSnapshot(frame, contours, result, sizeOutcome)
                                                            }.onSuccess { (frame, contours, result, sizeOutcome) ->
                                                                withContext(Dispatchers.Main) {
                                                                    pageDetectFrame = frame
                                                                    pageDetectInfo = PageDetectFrameInfo(
                                                                        width = frame.width,
                                                                        height = frame.height,
                                                                        downscaleFactor = frame.downscaleFactor,
                                                                        rotationAppliedDeg = frame.rotationAppliedDeg,
                                                                    )
                                                                    contourCache = contours
                                                                    contourInfo = contourInfo ?: ContourDebugInfo(
                                                                        totalContours = contours.size,
                                                                        topContours = ContourStats.topByArea(contours, 3),
                                                                    )
                                                                    quadSelectionResult = result
                                                                    rectifiedSizeOutcome = sizeOutcome
                                                                    pipelineResult = when (result) {
                                                                        is PageQuadSelectionResult.Success -> {
                                                                            val orderedCorners = OrderedCornersV1.fromPoints(result.candidate.points)
                                                                            orderedCorners?.let { corners ->
                                                                                val metrics = QualityMetricsV1.skewFromQuad(
                                                                                    corners,
                                                                                    frame.width,
                                                                                    frame.height,
                                                                                )
                                                                                DrawingImportPipelineResultV1(
                                                                                    orderedCorners = corners,
                                                                                    refinedCorners = null,
                                                                                    imageWidth = frame.width,
                                                                                    imageHeight = frame.height,
                                                                                    skewMetrics = metrics,
                                                                                )
                                                                            }
                                                                        }
                                                                        is PageQuadSelectionResult.Failure -> null
                                                                    }
                                                                    isPreparingFrame = false
                                                                    isSelectingQuad = false
                                                                    orderedCorners = (result as? PageQuadSelectionResult.Success)
                                                                        ?.candidate
                                                                        ?.points
                                                                        ?.let { points ->
                                                                            runCatching { CornerOrderingV1.order(points) }.getOrNull()
                                                                        }
                                                                    refineResult = null
                                                                    refineError = null
                                                                }
                                                            }.onFailure { error ->
                                                                withContext(Dispatchers.Main) {
                                                                    quadSelectionError = error.message ?: "Failed to select page quad."
                                                                    isPreparingFrame = false
                                                                    isSelectingQuad = false
                                                                    rectifiedSizeOutcome = null
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
                                                        refineError = null
                                                        coroutineScope.launch(Dispatchers.Default) {
                                                            runCatching {
                                                                CornerRefinerV1.refine(frame, ordered, refineParams)
                                                            }.onSuccess { result ->
                                                                withContext(Dispatchers.Main) {
                                                                    refineResult = result
                                                                }
                                                            }.onFailure { error ->
                                                                withContext(Dispatchers.Main) {
                                                                    refineResult = null
                                                                    refineError = error.message ?: "Corner refinement failed."
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
                                            pageDetectError?.let { message ->
                                                Text(
                                                    text = "Prep failed: $message",
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
                                            contourError?.let { message ->
                                                Text(
                                                    text = "Contour run failed: $message",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                )
                                            }
                                            quadSelectionResult?.let { result ->
                                                when (result) {
                                                    is PageQuadSelectionResult.Success -> {
                                                        Text(
                                                            text = formatQuadLabel(result.candidate),
                                                            style = MaterialTheme.typography.bodySmall,
                                                        )
                                                    }
                                                    is PageQuadSelectionResult.Failure -> {
                                                        Text(
                                                            text = formatFailureLabel(result.failure),
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
                                                Text(
                                                    text = formatSkewMetrics(metrics),
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                            quadSelectionError?.let { message ->
                                                Text(
                                                    text = "Quad selection failed: $message",
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
                                            refineResult?.let { result ->
                                                Text(
                                                    text = formatRefineLabel(result),
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                            refineError?.let { message ->
                                                Text(
                                                    text = "Refine failed: $message",
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
                                        pageDetectError = null
                                        isPreparingFrame = false
                                        contourInfo = null
                                        contourError = null
                                        isRunningContours = false
                                        contourCache = emptyList()
                                        quadSelectionResult = null
                                        quadSelectionError = null
                                        isSelectingQuad = false
                                        rectifiedSizeOutcome = null
                                        orderedCorners = null
                                        refineResult = null
                                        refineError = null
                                        pipelineResult = null
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
                                        pageDetectError = null
                                        isPreparingFrame = false
                                        contourInfo = null
                                        contourError = null
                                        isRunningContours = false
                                        contourCache = emptyList()
                                        quadSelectionResult = null
                                        quadSelectionError = null
                                        isSelectingQuad = false
                                        rectifiedSizeOutcome = null
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
                                    pageDetectError = null
                                    isPreparingFrame = false
                                    contourInfo = null
                                    contourError = null
                                    isRunningContours = false
                                    contourCache = emptyList()
                                    quadSelectionResult = null
                                    quadSelectionError = null
                                    isSelectingQuad = false
                                    orderedCorners = null
                                    refineResult = null
                                    refineError = null
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

private data class QuadSelectionSnapshot(
    val frame: PageDetectFrame,
    val contours: List<ContourV1>,
    val result: PageQuadSelectionResult,
    val sizeOutcome: PageDetectOutcomeV1<RectifiedSizeV1>?,
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
private fun formatSkewMetrics(metrics: SkewMetricsV1): String {
    val angleMax = "%.2f".format(Locale.US, metrics.angleMaxAbsDeg)
    val angleMean = "%.2f".format(Locale.US, metrics.angleMeanAbsDeg)
    val keystoneW = "%.3f".format(Locale.US, metrics.keystoneWidthRatio)
    val keystoneH = "%.3f".format(Locale.US, metrics.keystoneHeightRatio)
    val pageFill = "%.3f".format(Locale.US, metrics.pageFillRatio)
    return "Angle dev (max/mean): $angleMax°/$angleMean° • " +
        "Keystone W/H: $keystoneW/$keystoneH • Page fill: $pageFill • Status: ${metrics.status.name}"
}

private fun formatFailureLabel(failure: PageDetectFailure): String {
    return "Quad selection: ${failure.code.name} • ${failure.message}"
}

private fun formatRectifiedFailureLabel(failure: PageDetectFailure): String {
    return "Rectified size: ${failure.code.name} • ${failure.message}"
}

private fun formatOrderedCornersLabel(corners: OrderedCornersV1): String {
    val points = corners.toList()
        .joinToString(prefix = "[", postfix = "]") { "(${it.x.formatPx()},${it.y.formatPx()})" }
    return "Ordered corners (TL/TR/BR/BL): $points"
}

private fun formatRefineLabel(result: RefineResultV1): String {
    val deltas = result.deltasPx.joinToString(prefix = "[", postfix = "]") { it.formatDeltaPx() }
    val status = result.status.name
    val failure = result.failureCode?.name?.let { " • $it" } ?: ""
    return "Refine $status$failure • deltas=$deltas"
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
