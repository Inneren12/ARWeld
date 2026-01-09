package com.example.arweld.feature.scanner.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.arweld.feature.scanner.camera.BarcodeAnalyzer
import com.example.arweld.feature.scanner.camera.CameraPreviewController

@Composable
fun ScannerPreview(
    modifier: Modifier = Modifier,
    onPermissionStateChanged: (CameraPermissionState) -> Unit = {},
    onCodeDetected: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val cameraController = remember { CameraPreviewController(context) }
    val analyzer = remember(onCodeDetected) { BarcodeAnalyzer(onCodeDetected) }

    var permissionState by remember {
        mutableStateOf(
            if (
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                CameraPermissionState.Granted
            } else {
                CameraPermissionState.Requesting
            }
        )
    }
    val hasCameraPermission = permissionState == CameraPermissionState.Granted

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissionState = if (granted) {
                CameraPermissionState.Granted
            } else {
                CameraPermissionState.Denied
            }
        },
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(permissionState) {
        onPermissionStateChanged(permissionState)
    }

    DisposableEffect(lifecycleOwner, hasCameraPermission) {
        if (hasCameraPermission) {
            cameraController.bind(previewView, lifecycleOwner, analyzer)
        }
        onDispose {
            cameraController.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { previewView },
            )
        } else {
            val message = when (permissionState) {
                CameraPermissionState.Requesting -> "Requesting camera permission…"
                CameraPermissionState.Denied -> "Camera permission denied"
                CameraPermissionState.Granted -> "Camera permission required to scan"
            }
            Text(
                text = message,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        if (permissionState == CameraPermissionState.Requesting) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}
