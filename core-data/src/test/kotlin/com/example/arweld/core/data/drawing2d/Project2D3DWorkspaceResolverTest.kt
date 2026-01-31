package com.example.arweld.core.data.drawing2d

import androidx.documentfile.provider.DocumentFile
import com.example.arweld.core.domain.drawing2d.Project2D3DWorkspace
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class Project2D3DWorkspaceResolverTest {
    @Test
    fun `resolver joins workspace paths for files`() {
        val workspace = Project2D3DWorkspace(underlayName = "underlay.jpg")
        val resolver = Project2D3DWorkspaceResolver(workspace)
        val projectDir = createTempDir(prefix = "project2d3d")

        assertThat(resolver.workspaceDir(projectDir).path)
            .endsWith("workspace${File.separator}2d3d")
        assertThat(resolver.drawing2dJsonFile(projectDir).name).isEqualTo("drawing2d.json")
        assertThat(resolver.overlayPreviewFile(projectDir).name).isEqualTo("overlay_preview.png")
        assertThat(resolver.underlayFile(projectDir)?.name).isEqualTo("underlay.jpg")
    }

    @Test
    fun `resolver creates document files when requested`() {
        val workspace = Project2D3DWorkspace(underlayName = "underlay.png")
        val resolver = Project2D3DWorkspaceResolver(workspace)
        val projectDir = createTempDir(prefix = "project2d3d")
        val projectDocument = DocumentFile.fromFile(projectDir)

        val workspaceDir = resolver.workspaceDir(projectDocument, createIfMissing = true)
        val drawingJson = resolver.drawing2dJsonDocumentFile(projectDocument, createIfMissing = true)
        val overlayPreview = resolver.overlayPreviewDocumentFile(projectDocument, createIfMissing = true)
        val underlay = resolver.underlayDocumentFile(projectDocument, createIfMissing = true)

        assertThat(workspaceDir?.isDirectory).isTrue()
        assertThat(drawingJson?.name).isEqualTo("drawing2d.json")
        assertThat(overlayPreview?.name).isEqualTo("overlay_preview.png")
        assertThat(underlay?.name).isEqualTo("underlay.png")
    }
}
