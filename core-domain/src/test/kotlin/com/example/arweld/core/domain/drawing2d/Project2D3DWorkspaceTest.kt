package com.example.arweld.core.domain.drawing2d

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Project2D3DWorkspaceTest {
    @Test
    fun `default workspace names are stable`() {
        val workspace = Project2D3DWorkspace()

        assertThat(workspace.drawing2dJsonName).isEqualTo(Project2D3DWorkspace.DRAWING2D_JSON_NAME)
        assertThat(workspace.overlayPreviewName).isEqualTo(Project2D3DWorkspace.OVERLAY_PREVIEW_NAME)
        assertThat(workspace.underlayName).isNull()
    }

    @Test
    fun `relative paths join workspace root`() {
        val workspace = Project2D3DWorkspace()

        assertThat(workspace.drawing2dJsonRelativePath())
            .isEqualTo("workspace/2d3d/drawing2d.json")
        assertThat(workspace.overlayPreviewRelativePath())
            .isEqualTo("workspace/2d3d/overlay_preview.png")
        assertThat(workspace.underlayRelativePath()).isNull()
    }

    @Test
    fun `default underlay name keeps extension when provided`() {
        assertThat(Project2D3DWorkspace.defaultUnderlayName("jpg")).isEqualTo("underlay.jpg")
        assertThat(Project2D3DWorkspace.defaultUnderlayName(".png")).isEqualTo("underlay.png")
        assertThat(Project2D3DWorkspace.defaultUnderlayName(null)).isEqualTo("underlay")
    }
}
