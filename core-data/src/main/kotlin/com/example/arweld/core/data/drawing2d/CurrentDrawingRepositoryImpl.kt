package com.example.arweld.core.data.drawing2d

import android.content.Context
import com.example.arweld.core.domain.drawing2d.Drawing2DRepository
import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Drawing2DEditorJson
import com.example.arweld.core.drawing2d.editor.v1.toCanonicalJson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class CurrentDrawingRepositoryImpl @Inject constructor(
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

    override suspend fun saveCurrentDrawing(drawing: Drawing2D) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, CURRENT_DRAWING_PATH)
        val parentDir = file.parentFile
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw IOException("Failed to create drawing2d directory: ${parentDir.path}")
        }
        val tmpFile = File(parentDir ?: context.filesDir, "${file.name}.tmp")
        val json = drawing.toCanonicalJson()
        val bytes = json.toByteArray(Charsets.UTF_8)

        try {
            FileOutputStream(tmpFile).use { output ->
                output.write(bytes)
                output.flush()
                output.fd.sync()
            }
            if (!tmpFile.renameTo(file)) {
                throw IOException("Failed to rename ${tmpFile.path} to ${file.path}")
            }
        } catch (error: Exception) {
            tmpFile.delete()
            throw error
        }
    }

    private fun emptyDrawing(): Drawing2D = Drawing2D(nodes = emptyList(), members = emptyList())

    private companion object {
        const val CURRENT_DRAWING_PATH = "drawing2d/current_drawing.json"
    }
}
