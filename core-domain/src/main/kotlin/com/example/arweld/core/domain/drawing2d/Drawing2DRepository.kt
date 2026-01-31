package com.example.arweld.core.domain.drawing2d

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D

interface Drawing2DRepository {
    suspend fun getCurrentDrawing(): Drawing2D
}
