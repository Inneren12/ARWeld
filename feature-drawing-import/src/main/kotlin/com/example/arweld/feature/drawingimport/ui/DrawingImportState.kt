package com.example.arweld.feature.drawingimport.ui

import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import java.io.File

data class DrawingImportSession(
    val projectId: String,
    val projectDir: File,
    val artifacts: List<ArtifactEntryV1>,
)

sealed interface DrawingImportUiState {
    data object Idle : DrawingImportUiState
    data object Ready : DrawingImportUiState
    data object Capturing : DrawingImportUiState
    data class Saved(val session: DrawingImportSession) : DrawingImportUiState
    data class Error(val message: String) : DrawingImportUiState
}
