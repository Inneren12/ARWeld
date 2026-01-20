package com.example.arweld.feature.supervisor.usecase

import android.util.Log
import com.example.arweld.core.domain.system.TimeProvider
import com.example.arweld.feature.supervisor.export.EvidenceZipWriter
import com.example.arweld.feature.supervisor.export.MissingEvidenceFile
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class EvidenceZipExportResult(
    val outputDir: File,
    val zipFile: File,
    val fileCount: Int,
    val bytesWritten: Long,
    val missingFiles: List<MissingEvidenceFile>,
)

class EvidenceZipExportUseCase @Inject constructor(
    private val evidenceCollector: EvidenceCollector,
    private val evidenceZipWriter: EvidenceZipWriter,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(period: ExportPeriod, outputRoot: File): EvidenceZipExportResult =
        withContext(Dispatchers.IO) {
            val timestamp = timeProvider.nowMillis()
            val exportDir = File(outputRoot, "evidence/evzip_$timestamp").apply { mkdirs() }

            val descriptors = evidenceCollector.collect(period)
            Log.i(TAG, "Evidence collection: ${descriptors.size} items")

            val zipFile = File(exportDir, "evidence.zip")
            val zipResult = evidenceZipWriter.writeZip(zipFile, descriptors)

            Log.i(
                TAG,
                "Evidence zip complete. files=${zipResult.fileCount} bytes=${zipResult.bytesWritten} missing=${zipResult.missingFiles.size}",
            )

            EvidenceZipExportResult(
                outputDir = exportDir,
                zipFile = zipFile,
                fileCount = zipResult.fileCount,
                bytesWritten = zipResult.bytesWritten,
                missingFiles = zipResult.missingFiles,
            )
        }

    private companion object {
        private const val TAG = "EvidenceZipExport"
    }
}
