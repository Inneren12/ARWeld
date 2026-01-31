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
import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.Drawing2DEditorJson
import com.example.arweld.core.drawing2d.editor.v1.toCanonicalJson
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class Drawing2DRepositoryImpl @Inject constructor() : Drawing2DRepository {
    override suspend fun load(workspace: Drawing2DWorkspace): Drawing2D = withContext(Dispatchers.IO) {
        val resolver = Project2D3DWorkspaceResolver(workspace.layout)
        val drawingFile = resolver.drawing2dJsonFile(workspace.projectDir)
        if (!drawingFile.exists()) {
            return@withContext Drawing2D(nodes = emptyList(), members = emptyList())
        }
        val json = drawingFile.readText(Charsets.UTF_8)
        Drawing2DEditorJson.decodeFromString(json)
    }

    override suspend fun save(workspace: Drawing2DWorkspace, drawing: Drawing2D) = withContext(Dispatchers.IO) {
        val resolver = Project2D3DWorkspaceResolver(workspace.layout)
        val workspaceDir = resolver.workspaceDir(workspace.projectDir)
        ensureDir(workspaceDir)

        val targetFile = resolver.drawing2dJsonFile(workspace.projectDir)
        val tmpFile = File(targetFile.parentFile ?: workspaceDir, "${targetFile.name}.tmp")
        val json = drawing.toCanonicalJson()
        val bytes = json.toByteArray(Charsets.UTF_8)

        try {
            FileOutputStream(tmpFile).use { output ->
                output.write(bytes)
                output.flush()
                output.fd.sync()
            }
            if (!tmpFile.renameTo(targetFile)) {
                throw IOException("Failed to rename ${tmpFile.path} to ${targetFile.path}")
            }
        } catch (error: Exception) {
            tmpFile.delete()
            throw error
        }
    }

    private fun ensureDir(dir: File) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw IOException("Failed to create workspace directory: ${dir.path}")
        }
    }
}
