package com.example.arweld.core.data.drawing2d

import android.content.Context
import com.example.arweld.core.domain.drawing2d.Drawing2DRepository
import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Drawing2DEditorJson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Drawing2DRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : Drawing2DRepository {

    override suspend fun getCurrentDrawing(): Drawing2D = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, CURRENT_DRAWING_PATH)
        if (!file.exists()) {
            return@withContext emptyDrawing()
        }
        return@withContext runCatching {
            Drawing2DEditorJson.decodeFromString(file.readText())
        }.getOrElse {
            emptyDrawing()
        }
    }

    private fun emptyDrawing(): Drawing2D = Drawing2D(nodes = emptyList(), members = emptyList())

    private companion object {
        const val CURRENT_DRAWING_PATH = "drawing2d/current_drawing.json"
    }
}
