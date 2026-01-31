package com.example.arweld.core.data.drawing2d

import com.example.arweld.core.domain.drawing2d.Project2D3DWorkspace
import java.io.File

/**
 * Workspace root for Drawing2D storage.
 */
data class Drawing2DWorkspace(
    val projectDir: File,
    val layout: Project2D3DWorkspace = Project2D3DWorkspace(),
)
