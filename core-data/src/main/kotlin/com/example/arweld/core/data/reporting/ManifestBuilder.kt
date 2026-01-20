package com.example.arweld.core.data.reporting

import android.net.Uri
import com.example.arweld.core.data.file.Sha256Hasher
import com.example.arweld.core.domain.reporting.ExportManifestV1
import com.example.arweld.core.domain.reporting.ManifestFileEntry
import com.example.arweld.core.domain.reporting.ManifestPeriod
import com.example.arweld.core.domain.reporting.ReportPeriod
import com.example.arweld.core.domain.system.TimeProvider
import java.io.File
import java.time.Instant
import javax.inject.Inject

class ManifestBuilder @Inject constructor(
    private val sha256Hasher: Sha256Hasher,
    private val documentMetadataResolver: DocumentMetadataResolver,
    private val timeProvider: TimeProvider,
) {
    fun build(
        period: ReportPeriod,
        exportedFiles: List<ExportedFileReference>,
        warnings: List<String> = emptyList(),
    ): ExportManifestV1 {
        val entries = exportedFiles.map { reference ->
            val name = resolveName(reference)
            val hashResult = resolveHash(reference)
            ManifestFileEntry(
                name = name,
                sizeBytes = hashResult.sizeBytes,
                sha256Hex = hashResult.sha256Hex,
            )
        }.sortedBy { it.name }

        return ExportManifestV1(
            manifestVersion = 1,
            generatedAt = Instant.ofEpochMilli(timeProvider.nowMillis()).toString(),
            period = ManifestPeriod(startMillis = period.startMillis, endMillis = period.endMillis),
            files = entries,
            warnings = warnings,
        )
    }

    private fun resolveName(reference: ExportedFileReference): String {
        reference.nameOverride?.let { return it }
        reference.file?.name?.let { return it }
        reference.uri?.let { uri ->
            return documentMetadataResolver.displayName(uri)
                ?: throw IllegalStateException("Unable to resolve display name for $uri")
        }
        throw IllegalArgumentException("Exported file reference requires a file or uri")
    }

    private fun resolveHash(reference: ExportedFileReference) = when {
        reference.file != null -> sha256Hasher.streamWithSize(reference.file)
        reference.uri != null -> sha256Hasher.streamWithSize(reference.uri)
        else -> throw IllegalArgumentException("Exported file reference requires a file or uri")
    }
}

data class ExportedFileReference(
    val file: File? = null,
    val uri: Uri? = null,
    val nameOverride: String? = null,
)
