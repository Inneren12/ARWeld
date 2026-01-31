package com.example.arweld.core.drawing2d.artifacts.io.v1

import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import java.io.File

interface ArtifactStoreV1 {
    val baseDir: File

    fun writeBytes(
        kind: ArtifactKindV1,
        relPath: String,
        bytes: ByteArray,
        mime: String
    ): ArtifactEntryV1

    fun writeText(
        kind: ArtifactKindV1,
        relPath: String,
        text: String,
        mime: String = "application/json"
    ): ArtifactEntryV1
}

fun interface ArtifactWriteFaultInjectorV1 {
    fun onWrite(kind: ArtifactKindV1, relPath: String, byteCount: Int)
}
