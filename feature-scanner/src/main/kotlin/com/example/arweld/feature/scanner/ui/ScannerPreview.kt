package com.example.arweld.feature.scanner.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.camera.view.PreviewView
import com.example.arweld.feature.scanner.camera.CameraPreviewController

@Composable
fun ScannerPreview(
    modifier: Modifier = Modifier,
    onCameraReady: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(modifier = modifier.fillMaxSize()) {
            Button(onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text(text = "Grant camera access")
            }
        }
        return
    }

    val previewController = remember { CameraPreviewController(context) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize(),
    )

    DisposableEffect(lifecycleOwner, previewView) {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                previewController.bindPreview(previewView, lifecycleOwner, onCameraReady)
            }

            override fun onPause(owner: LifecycleOwner) {
                previewController.unbind()
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        previewController.bindPreview(previewView, lifecycleOwner, onCameraReady)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            previewController.unbind()
        }
    }
}
