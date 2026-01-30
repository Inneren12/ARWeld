package com.example.arweld.core.drawing2d.artifacts.io

import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.core.drawing2d.crypto.Sha256V1
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

class FileArtifactStoreV1(
    override val baseDir: File
) : ArtifactStoreV1 {

    override fun writeBytes(
        kind: ArtifactKindV1,
        relPath: String,
        bytes: ByteArray,
        mime: String
    ): ArtifactEntryV1 {
        if (!baseDir.exists()) {
            check(baseDir.mkdirs()) { "Failed to create base directory: ${'$'}{baseDir.path}" }
        }
        val normalizedPath = RelPathV1.normalizeOrThrow(relPath)
        val targetFile = File(baseDir, normalizedPath)
        val parentDir = targetFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            check(parentDir.mkdirs()) { "Failed to create directory: ${'$'}{parentDir.path}" }
        }

        val tmpFile = File(parentDir ?: baseDir, "${'$'}{targetFile.name}.tmp.${'$'}{UUID.randomUUID()}")
        try {
            Files.newOutputStream(tmpFile.toPath()).use { output ->
                output.write(bytes)
            }
            moveAtomically(tmpFile, targetFile)
        } catch (error: Exception) {
            tmpFile.delete()
            throw error
        }

        val sha256 = Sha256V1.hashBytes(bytes)
        return ArtifactEntryV1(
            kind = kind,
            relPath = normalizedPath,
            sha256 = sha256,
            byteSize = bytes.size.toLong(),
            mime = mime
        )
    }

    override fun writeText(
        kind: ArtifactKindV1,
        relPath: String,
        text: String,
        mime: String
    ): ArtifactEntryV1 {
        return writeBytes(kind, relPath, text.toByteArray(Charsets.UTF_8), mime)
    }

    private fun moveAtomically(tmpFile: File, targetFile: File) {
        try {
            Files.move(
                tmpFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (error: AtomicMoveNotSupportedException) {
            // Best-effort atomic move fallback when ATOMIC_MOVE is unsupported.
            Files.move(
                tmpFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }
}
