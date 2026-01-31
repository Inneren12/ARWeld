package com.example.arweld.feature.drawingimport.ui

import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import java.io.File

data class RectifiedImageInfo(
    val width: Int,
    val height: Int,
)

data class DrawingImportSession(
    val projectId: String,
    val projectDir: File,
    val artifacts: List<ArtifactEntryV1>,
    val rectifiedImageInfo: RectifiedImageInfo? = null,
)

sealed interface DrawingImportUiState {
    data object Idle : DrawingImportUiState
    data object Ready : DrawingImportUiState
    data object Capturing : DrawingImportUiState
    data class Saved(val session: DrawingImportSession) : DrawingImportUiState
    data class Error(val message: String) : DrawingImportUiState
}
