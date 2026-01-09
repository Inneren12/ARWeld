package com.example.arweld.feature.scanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arweld.feature.scanner.camera.ScannerEngine

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ScanCodeScreen(
    onCodeResolved: (String) -> Unit,
    onBack: () -> Unit,
    resolutionState: ScanCodeResolutionState,
    onResolutionReset: () -> Unit,
    scannerEngine: ScannerEngine,
) {
    var lastCode by remember { mutableStateOf<String?>(null) }
    var permissionState by remember { mutableStateOf(CameraPermissionState.Requesting) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Scan code") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ScannerPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                scannerEngine = scannerEngine,
                onPermissionStateChanged = { state -> permissionState = state },
                onCodeDetected = { detected ->
                    if (permissionState != CameraPermissionState.Granted) {
                        return@ScannerPreview
                    }
                    if (resolutionState is ScanCodeResolutionState.Resolving) {
                        return@ScannerPreview
                    }
                    onResolutionReset()
                    lastCode = detected
                    onCodeResolved(detected)
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val statusText = when {
                    permissionState == CameraPermissionState.Denied ->
                        "Camera permission denied. Enable it in settings to scan."
                    resolutionState is ScanCodeResolutionState.Resolving ->
                        "Resolving code…"
                    resolutionState is ScanCodeResolutionState.NotFound ->
                        "No work item found for ${resolutionState.code}"
                    resolutionState is ScanCodeResolutionState.Error ->
                        resolutionState.message
                    !lastCode.isNullOrBlank() -> "Code detected: $lastCode"
                    else -> "Scanning…"
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyLarge,
                )

                if (resolutionState is ScanCodeResolutionState.Resolving) {
                    CircularProgressIndicator()
                    Text(
                        text = "Please hold steady…",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
