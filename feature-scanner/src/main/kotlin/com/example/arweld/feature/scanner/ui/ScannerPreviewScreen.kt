package com.example.arweld.feature.scanner.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ScannerPreviewScreen() {
    val isReady = remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ScannerPreview(
                modifier = Modifier.fillMaxSize(),
                onCameraReady = { isReady.value = true },
            )

            if (isReady.value) {
                Text(
                    text = "Camera ready",
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
