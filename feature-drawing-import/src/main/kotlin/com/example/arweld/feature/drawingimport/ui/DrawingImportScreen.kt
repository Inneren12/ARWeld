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
import java.io.File
import java.util.UUID
import kotlinx.coroutines.launch

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
    val diagnosticsLogger = remember(diagnosticsRecorder) {
        DrawingImportEventLogger(diagnosticsRecorder)
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
