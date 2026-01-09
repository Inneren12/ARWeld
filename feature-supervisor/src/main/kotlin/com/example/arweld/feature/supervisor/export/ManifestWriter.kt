package com.example.arweld.feature.supervisor.export

import com.example.arweld.core.data.file.computeSha256
import java.io.File
import javax.inject.Inject

class ManifestWriter @Inject constructor() {
    fun writeManifest(files: List<ManifestEntry>, file: File) {
        val sorted = files.sortedBy { it.relativePath }
        val content = sorted.joinToString(separator = "\n") { entry ->
            "${entry.sha256}  ${entry.relativePath}"
        }
        file.writeText(content)
    }

    fun buildEntries(exportRoot: File, files: List<File>): List<ManifestEntry> {
        return files.filter { it.exists() }.map { file ->
            val relative = exportRoot.toPath().relativize(file.toPath()).toString()
            ManifestEntry(
                relativePath = relative.replace(File.separatorChar, '/'),
                sha256 = computeSha256(file),
            )
        }
    }
}

data class ManifestEntry(
    val relativePath: String,
    val sha256: String,
)
