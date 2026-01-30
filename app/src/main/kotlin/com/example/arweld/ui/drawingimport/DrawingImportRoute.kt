package com.example.arweld.ui.drawingimport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.arweld.di.DiagnosticsEntryPoint
import com.example.arweld.feature.drawingimport.ui.DrawingImportScreen
import dagger.hilt.android.EntryPointAccessors

@Composable
fun DrawingImportRoute() {
    val context = LocalContext.current
    val diagnosticsRecorder = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            DiagnosticsEntryPoint::class.java,
        ).diagnosticsRecorder()
    }
    DrawingImportScreen(diagnosticsRecorder = diagnosticsRecorder)
}
