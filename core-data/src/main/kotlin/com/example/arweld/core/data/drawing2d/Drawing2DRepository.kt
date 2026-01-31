package com.example.arweld.core.data.drawing2d

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D

interface Drawing2DRepository {
    suspend fun load(workspace: Drawing2DWorkspace): Drawing2D

    suspend fun save(workspace: Drawing2DWorkspace, drawing: Drawing2D)
}
