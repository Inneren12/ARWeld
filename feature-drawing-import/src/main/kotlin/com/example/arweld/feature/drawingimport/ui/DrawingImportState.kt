package com.example.arweld.feature.drawingimport.ui

import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.feature.drawingimport.pipeline.PipelineResultV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFailureV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectStageV1
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

sealed interface DrawingImportProcessState {
    data object Idle : DrawingImportProcessState
    data class Running(val stage: PageDetectStageV1) : DrawingImportProcessState
    data class Success(val result: PipelineResultV1) : DrawingImportProcessState
    data class Failure(val failure: PageDetectFailureV1) : DrawingImportProcessState
}
