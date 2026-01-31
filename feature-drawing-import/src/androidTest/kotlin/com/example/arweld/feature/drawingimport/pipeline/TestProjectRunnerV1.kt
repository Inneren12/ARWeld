package com.example.arweld.feature.drawingimport.pipeline

import android.content.Context
import com.example.arweld.core.drawing2d.artifacts.io.v1.FileArtifactStoreV1
import com.example.arweld.core.drawing2d.artifacts.io.v1.FinalizeOutcomeV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.feature.drawingimport.artifacts.DrawingImportArtifacts
import com.example.arweld.feature.drawingimport.preprocess.PageDetectOutcomeV1
import com.example.arweld.feature.drawingimport.ui.DrawingImportSession
import java.io.File
import java.util.UUID

class TestProjectRunnerV1(
    private val context: Context,
    private val artifactsRoot: File,
) {
    suspend fun run(
        assetName: String,
        projectId: String = "smoke-${UUID.randomUUID()}",
    ): TestProjectRunResult {
        val projectDir = File(File(artifactsRoot, "projects"), projectId)
        val rawEntry = writeRawImage(projectDir, assetName)
        val session = DrawingImportSession(
            projectId = projectId,
            artifactsRoot = artifactsRoot,
            projectDir = projectDir,
            artifacts = listOf(rawEntry),
        )
        val pipeline = DrawingImportPipelineV1()
        val outcome = pipeline.run(session)
        return when (outcome) {
            is PageDetectOutcomeV1.Success -> {
                TestProjectRunResult(
                    artifactsRoot = artifactsRoot,
                    projectDir = projectDir,
                    artifacts = outcome.value.artifacts,
                    finalization = outcome.value.finalization,
                )
            }
            is PageDetectOutcomeV1.Failure -> {
                throw AssertionError("Pipeline failed at ${outcome.failure.stage} (${outcome.failure.code})")
            }
        }
    }

    private fun writeRawImage(projectDir: File, assetName: String): ArtifactEntryV1 {
        val bytes = context.assets.open(assetName).use { it.readBytes() }
        val store = FileArtifactStoreV1(projectDir)
        return store.writeBytes(
            kind = ArtifactKindV1.RAW_IMAGE,
            relPath = DrawingImportArtifacts.rawImageRelPath(),
            bytes = bytes,
            mime = "image/jpeg",
        )
    }
}

data class TestProjectRunResult(
    val artifactsRoot: File,
    val projectDir: File,
    val artifacts: List<ArtifactEntryV1>,
    val finalization: FinalizeOutcomeV1?,
)
