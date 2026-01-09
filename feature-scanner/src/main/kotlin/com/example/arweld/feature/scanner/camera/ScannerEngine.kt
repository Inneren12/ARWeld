package com.example.arweld.feature.scanner.camera

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

interface ScannerEngine {
    fun bind(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onCodeDetected: (String) -> Unit,
    )

    fun shutdown()
}
