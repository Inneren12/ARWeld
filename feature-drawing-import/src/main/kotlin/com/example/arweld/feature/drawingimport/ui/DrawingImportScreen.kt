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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.arweld.core.drawing2d.artifacts.io.v1.FileArtifactStoreV1
import com.example.arweld.core.drawing2d.artifacts.io.v1.ManifestWriterV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.core.drawing2d.artifacts.v1.ManifestV1
import com.example.arweld.feature.drawingimport.artifacts.DrawingImportArtifacts
import com.example.arweld.feature.drawingimport.camera.CameraSession
import java.io.File
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun DrawingImportScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraSession = remember { CameraSession(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var captureState by remember { mutableStateOf(CaptureState.Idle) }
    var projectId by rememberSaveable { mutableStateOf<String?>(null) }
    var lastSavedInfo by remember { mutableStateOf<SavedCaptureInfo?>(null) }
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
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        uiState = if (granted) {
            CameraPermissionState.Ready
        } else {
            CameraPermissionState.PermissionDenied
        }
    }

    DisposableEffect(uiState, lifecycleOwner, previewView) {
        val currentPreviewView = previewView
        if (uiState is CameraPermissionState.Ready && currentPreviewView != null) {
            cameraSession.start(lifecycleOwner, currentPreviewView) { throwable ->
                cameraSession.stop()
                uiState = CameraPermissionState.CameraError(
                    throwable.message ?: "Camera failed to start. Please try again.",
                )
            }
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
            if (uiState is CameraPermissionState.Ready) {
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
                            Text(
                                text = "Camera ready",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            lastSavedInfo?.let { saved ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Raw saved",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = "Project: ${saved.projectId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    text = saved.projectDir,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    text = "RelPath: ${saved.relPath}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (captureState == CaptureState.Capturing) {
                        Text(
                            text = "Capturing...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (uiState is CameraPermissionState.Ready) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (captureState == CaptureState.Capturing) return@launch
                                    captureState = CaptureState.Capturing
                                    val captureProjectId = projectId ?: UUID.randomUUID().toString()
                                    projectId = captureProjectId
                                    val tempDir = File(context.cacheDir, "drawing-import")
                                    if (!tempDir.exists()) {
                                        tempDir.mkdirs()
                                    }
                                    val tempFile = File(tempDir, "raw_capture.jpg")
                                    try {
                                        val targetRotation = previewView?.display?.rotation
                                        cameraSession.captureImage(tempFile, targetRotation)
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
                                        val manifest = ManifestV1(
                                            projectId = captureProjectId,
                                            artifacts = listOf(rawEntry),
                                        )
                                        ManifestWriterV1().write(projectDir, manifest)
                                        lastSavedInfo = SavedCaptureInfo(
                                            projectId = captureProjectId,
                                            projectDir = projectDir.path,
                                            relPath = rawEntry.relPath,
                                        )
                                        snackbarHostState.showSnackbar("Raw saved")
                                        captureState = CaptureState.Saved
                                    } catch (error: Throwable) {
                                        captureState = CaptureState.Error
                                        snackbarHostState.showSnackbar(
                                            error.message ?: "Capture failed",
                                        )
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
                                captureState = CaptureState.Idle
                                projectId = UUID.randomUUID().toString()
                                lastSavedInfo = null
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

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(bottom = 12.dp),
            )
        }
    }
}

private sealed interface CameraPermissionState {
    data object NeedsPermission : CameraPermissionState
    data object PermissionDenied : CameraPermissionState
    data object Ready : CameraPermissionState
    data class CameraError(val message: String) : CameraPermissionState
}

private enum class CaptureState {
    Idle,
    Capturing,
    Saved,
    Error,
}

private data class SavedCaptureInfo(
    val projectId: String,
    val projectDir: String,
    val relPath: String,
)
