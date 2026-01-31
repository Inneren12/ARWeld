package com.example.arweld.feature.drawingimport.artifacts

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.artifacts.io.v1.ArtifactStoreV1
import com.example.arweld.core.drawing2d.artifacts.io.v1.ManifestWriterV1
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.core.drawing2d.artifacts.v1.CaptureMetaV1
import com.example.arweld.feature.drawingimport.ui.DrawingImportSession

class CaptureMetaWriterV1(
    private val manifestWriter: ManifestWriterV1 = ManifestWriterV1(),
) {
    fun write(
        captureMeta: CaptureMetaV1,
        projectStore: ArtifactStoreV1,
        session: DrawingImportSession,
        rewriteManifest: Boolean = true,
    ): DrawingImportSession {
        val json = Drawing2DJson.encodeToString(captureMeta)
        val entry = projectStore.writeText(
            kind = ArtifactKindV1.CAPTURE_META,
            relPath = ProjectLayoutV1.CAPTURE_META_JSON,
            text = json,
            mime = "application/json",
        )
        val artifactsWithoutManifest = session.artifacts
            .filter { it.kind != ArtifactKindV1.MANIFEST_JSON }
            .filterNot { it.kind == ArtifactKindV1.CAPTURE_META && it.relPath == entry.relPath }
        val updatedArtifacts = artifactsWithoutManifest + entry
        val finalArtifacts = if (rewriteManifest) {
            val manifestEntry = manifestWriter.write(
                session.projectDir,
                DrawingImportArtifacts.buildManifest(
                    projectId = session.projectId,
                    artifacts = updatedArtifacts,
                ),
            )
            updatedArtifacts + manifestEntry
        } else {
            updatedArtifacts
        }
        return session.copy(artifacts = finalArtifacts)
    }
}
