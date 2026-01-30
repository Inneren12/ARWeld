package com.example.arweld.feature.drawingimport.artifacts

import android.content.Context
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.core.drawing2d.artifacts.v1.ManifestV1
import java.io.File

object DrawingImportArtifacts {
    private const val RAW_IMAGE_EXTENSION = "jpg"

    fun artifactsRoot(context: Context): File = File(context.filesDir, "artifacts")

    fun rawImageRelPath(): String = "${ProjectLayoutV1.RAW_IMAGE}.$RAW_IMAGE_EXTENSION"

    fun buildManifest(
        projectId: String,
        artifacts: List<ArtifactEntryV1>,
    ): ManifestV1 {
        return ManifestV1(
            projectId = projectId,
            artifacts = artifacts,
        )
    }
}
