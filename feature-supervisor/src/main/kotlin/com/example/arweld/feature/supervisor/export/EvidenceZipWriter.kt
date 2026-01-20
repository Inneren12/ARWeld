package com.example.arweld.feature.supervisor.export

import java.io.File
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class EvidenceZipWriter @Inject constructor() {
    fun writeZip(outputFile: File, descriptors: List<EvidenceFileDescriptor>): EvidenceZipWriteResult {
        val sorted = descriptors.sortedWith(EvidenceFileDescriptor.ORDERING)
        val missingFiles = mutableListOf<MissingEvidenceFile>()
        var bytesWritten = 0L
        var fileCount = 0

        ZipOutputStream(outputFile.outputStream().buffered()).use { zip ->
            sorted.forEachIndexed { index, descriptor ->
                val sourceFile = resolveEvidenceFile(descriptor.uri)
                val extension = sourceFile?.extension?.ifBlank { null }
                    ?: extractExtensionFromUri(descriptor.uri)
                    ?: DEFAULT_EXTENSION
                val entryName = buildEntryName(descriptor, index, extension)

                if (sourceFile == null) {
                    missingFiles.add(
                        MissingEvidenceFile(
                            descriptor = descriptor,
                            expectedPath = entryName,
                            reason = "Unsupported URI scheme",
                        ),
                    )
                    return@forEachIndexed
                }

                if (!sourceFile.exists()) {
                    missingFiles.add(
                        MissingEvidenceFile(
                            descriptor = descriptor,
                            expectedPath = entryName,
                            reason = "File missing",
                        ),
                    )
                    return@forEachIndexed
                }

                zip.putNextEntry(ZipEntry(entryName))
                sourceFile.inputStream().buffered().use { input ->
                    bytesWritten += input.copyTo(zip)
                }
                zip.closeEntry()
                fileCount += 1
            }
        }

        return EvidenceZipWriteResult(
            fileCount = fileCount,
            bytesWritten = bytesWritten,
            missingFiles = missingFiles,
        )
    }

    private fun buildEntryName(
        descriptor: EvidenceFileDescriptor,
        index: Int,
        extension: String,
    ): String {
        val workItemId = sanitizeSegment(descriptor.workItemId).ifBlank { DEFAULT_WORK_ITEM }
        val evidenceId = sanitizeSegment(descriptor.evidenceId).ifBlank { "evidence_${index + 1}" }
        val kind = sanitizeSegment(descriptor.kind)
            .ifBlank { DEFAULT_KIND }
            .lowercase(Locale.US)
        val safeExtension = sanitizeSegment(extension).ifBlank { DEFAULT_EXTENSION }
        return "evidence/$workItemId/${evidenceId}_${kind}.$safeExtension"
    }

    private fun sanitizeSegment(value: String?): String {
        return value
            ?.trim()
            ?.replace('/', '_')
            ?.replace('\\', '_')
            ?: ""
    }

    private fun resolveEvidenceFile(uriString: String): File? {
        return runCatching {
            val uri = java.net.URI(uriString)
            when (uri.scheme) {
                null -> File(uriString)
                "file" -> File(uri)
                else -> null
            }
        }.getOrNull()
    }

    private fun extractExtensionFromUri(uriString: String): String? {
        return runCatching {
            val uri = java.net.URI(uriString)
            val path = uri.path ?: return@runCatching null
            val name = path.substringAfterLast('/')
            val extension = name.substringAfterLast('.', "")
            extension.ifBlank { null }
        }.getOrNull()
    }

    companion object {
        private const val DEFAULT_EXTENSION = "bin"
        private const val DEFAULT_KIND = "unknown"
        private const val DEFAULT_WORK_ITEM = "unknown-work-item"
    }
}
