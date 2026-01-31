package com.example.arweld.core.data.drawing2d

import android.net.Uri
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.example.arweld.core.domain.drawing2d.Project2D3DWorkspace
import java.io.File

class Project2D3DWorkspaceResolver(
    private val workspace: Project2D3DWorkspace = Project2D3DWorkspace(),
) {
    fun workspaceDir(projectDir: File): File = File(projectDir, workspace.workspaceDirName)

    fun drawing2dJsonFile(projectDir: File): File = File(workspaceDir(projectDir), workspace.drawing2dJsonName)

    fun overlayPreviewFile(projectDir: File): File = File(workspaceDir(projectDir), workspace.overlayPreviewName)

    fun underlayFile(projectDir: File): File? =
        workspace.underlayName?.let { File(workspaceDir(projectDir), it) }

    fun drawing2dJsonUri(projectDir: File): Uri = drawing2dJsonFile(projectDir).toUri()

    fun overlayPreviewUri(projectDir: File): Uri = overlayPreviewFile(projectDir).toUri()

    fun underlayUri(projectDir: File): Uri? = underlayFile(projectDir)?.toUri()

    fun workspaceDir(projectTree: DocumentFile, createIfMissing: Boolean = false): DocumentFile? {
        var current: DocumentFile? = projectTree
        for (segment in workspace.workspaceSegments()) {
            current = current?.let { ensureDirectory(it, segment, createIfMissing) }
        }
        return current
    }

    fun drawing2dJsonDocumentFile(
        projectTree: DocumentFile,
        createIfMissing: Boolean = false,
    ): DocumentFile? = workspaceDir(projectTree, createIfMissing)?.let {
        ensureFile(it, workspace.drawing2dJsonName, JSON_MIME, createIfMissing)
    }

    fun overlayPreviewDocumentFile(
        projectTree: DocumentFile,
        createIfMissing: Boolean = false,
    ): DocumentFile? = workspaceDir(projectTree, createIfMissing)?.let {
        ensureFile(it, workspace.overlayPreviewName, PNG_MIME, createIfMissing)
    }

    fun underlayDocumentFile(
        projectTree: DocumentFile,
        mimeType: String = OCTET_STREAM_MIME,
        createIfMissing: Boolean = false,
    ): DocumentFile? {
        val underlayName = workspace.underlayName ?: return null
        return workspaceDir(projectTree, createIfMissing)?.let {
            ensureFile(it, underlayName, mimeType, createIfMissing)
        }
    }

    fun drawing2dJsonUri(projectTree: DocumentFile, createIfMissing: Boolean = false): Uri? =
        drawing2dJsonDocumentFile(projectTree, createIfMissing)?.uri

    fun overlayPreviewUri(projectTree: DocumentFile, createIfMissing: Boolean = false): Uri? =
        overlayPreviewDocumentFile(projectTree, createIfMissing)?.uri

    fun underlayUri(
        projectTree: DocumentFile,
        mimeType: String = OCTET_STREAM_MIME,
        createIfMissing: Boolean = false,
    ): Uri? = underlayDocumentFile(projectTree, mimeType, createIfMissing)?.uri

    private fun ensureDirectory(
        parent: DocumentFile,
        name: String,
        createIfMissing: Boolean,
    ): DocumentFile? {
        val existing = parent.findFile(name)
        if (existing != null) {
            return existing.takeIf { it.isDirectory }
        }
        if (!createIfMissing) return null
        return parent.createDirectory(name)
    }

    private fun ensureFile(
        parent: DocumentFile,
        name: String,
        mimeType: String,
        createIfMissing: Boolean,
    ): DocumentFile? {
        val existing = parent.findFile(name)
        if (existing != null) {
            return existing.takeIf { existing.isFile }
        }
        if (!createIfMissing) return null
        return parent.createFile(mimeType, name)
    }

    companion object {
        const val JSON_MIME = "application/json"
        const val PNG_MIME = "image/png"
        const val OCTET_STREAM_MIME = "application/octet-stream"
    }
}
