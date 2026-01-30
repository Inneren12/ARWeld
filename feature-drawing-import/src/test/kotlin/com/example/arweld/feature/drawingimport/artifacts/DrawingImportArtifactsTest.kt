package com.example.arweld.feature.drawingimport.artifacts

import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.core.drawing2d.artifacts.v1.ManifestV1
import com.example.arweld.core.drawing2d.artifacts.v1.canonicalize
import org.junit.Assert.assertEquals
import org.junit.Test

class DrawingImportArtifactsTest {

    @Test
    fun `raw image relPath uses project layout`() {
        assertEquals("${ProjectLayoutV1.RAW_IMAGE}.jpg", DrawingImportArtifacts.rawImageRelPath())
    }

    @Test
    fun `manifest canonicalization sorts by kind then relPath`() {
        val overlay = ArtifactEntryV1(
            kind = ArtifactKindV1.OVERLAY,
            relPath = "b/overlay.png",
            sha256 = "b".repeat(64),
            byteSize = 1L,
            mime = "image/png",
        )
        val raw = ArtifactEntryV1(
            kind = ArtifactKindV1.RAW_IMAGE,
            relPath = "a/raw.jpg",
            sha256 = "a".repeat(64),
            byteSize = 2L,
            mime = "image/jpeg",
        )
        val manifest = ManifestV1(
            projectId = "project-1",
            artifacts = listOf(overlay, raw),
        )

        val canonical = manifest.canonicalize()

        assertEquals(listOf(raw, overlay), canonical.artifacts)
    }
}
