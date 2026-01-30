package com.example.arweld.core.drawing2d.artifacts.io

import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import java.io.File

class ChecksumsWriterV1 {
    fun write(baseDir: File, artifacts: List<ArtifactEntryV1>): ArtifactEntryV1 {
        val store = FileArtifactStoreV1(baseDir)
        val sorted = artifacts.sortedBy { it.relPath }
        val content = buildString {
            sorted.forEachIndexed { index, entry ->
                if (index > 0) {
                    append('\n')
                }
                append(entry.sha256)
                append("  ")
                append(entry.relPath)
            }
            append('\n')
        }
        return store.writeText(
            kind = ArtifactKindV1.CHECKSUMS_SHA256,
            relPath = ProjectLayoutV1.CHECKSUMS_SHA256,
            text = content,
            mime = "text/plain"
        )
    }
}
