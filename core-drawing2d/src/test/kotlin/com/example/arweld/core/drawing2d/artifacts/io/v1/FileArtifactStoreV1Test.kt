package com.example.arweld.core.drawing2d.artifacts.io.v1

import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.core.drawing2d.crypto.Sha256V1
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileArtifactStoreV1Test {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `writeBytes writes file and returns metadata`() {
        val baseDir = tempFolder.newFolder("artifacts")
        val store = FileArtifactStoreV1(baseDir)
        val bytes = "artifact payload".toByteArray()

        val entry = store.writeBytes(
            kind = ArtifactKindV1.MANIFEST_JSON,
            relPath = "manifests/manifest.json",
            bytes = bytes,
            mime = "application/json"
        )

        val expectedFile = File(baseDir, "manifests/manifest.json")
        assertThat(expectedFile.exists()).isTrue()
        assertThat(expectedFile.readBytes()).isEqualTo(bytes)
        assertThat(entry.sha256).isEqualTo(Sha256V1.hashBytes(bytes))
        assertThat(entry.byteSize).isEqualTo(bytes.size.toLong())
        assertThat(entry.relPath).isEqualTo("manifests/manifest.json")
        assertThat(entry.mime).isEqualTo("application/json")
    }

    @Test
    fun `writeBytes with same content yields same sha256`() {
        val baseDir = tempFolder.newFolder("artifacts")
        val store = FileArtifactStoreV1(baseDir)
        val bytes = ByteArray(64) { 42 }

        val first = store.writeBytes(
            kind = ArtifactKindV1.RAW_IMAGE,
            relPath = "previews/preview.png",
            bytes = bytes,
            mime = "image/png"
        )
        val second = store.writeBytes(
            kind = ArtifactKindV1.RAW_IMAGE,
            relPath = "previews/preview.png",
            bytes = bytes,
            mime = "image/png"
        )

        assertThat(first.sha256).isEqualTo(second.sha256)
    }

    @Test
    fun `writeBytes rejects unsafe paths`() {
        val store = FileArtifactStoreV1(tempFolder.newFolder("artifacts"))

        assertThrows(IllegalArgumentException::class.java) {
            store.writeBytes(
                kind = ArtifactKindV1.MANIFEST_JSON,
                relPath = "../escape.json",
                bytes = byteArrayOf(1, 2, 3),
                mime = "application/json"
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            store.writeBytes(
                kind = ArtifactKindV1.MANIFEST_JSON,
                relPath = "/abs/path.json",
                bytes = byteArrayOf(1, 2, 3),
                mime = "application/json"
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            store.writeBytes(
                kind = ArtifactKindV1.MANIFEST_JSON,
                relPath = "dir\\file.json",
                bytes = byteArrayOf(1, 2, 3),
                mime = "application/json"
            )
        }
    }

    @Test
    fun `writeBytes cleans up temp files after success`() {
        val baseDir = tempFolder.newFolder("artifacts")
        val store = FileArtifactStoreV1(baseDir)
        val relPath = "assets/blob.bin"

        store.writeBytes(
            kind = ArtifactKindV1.CHECKSUMS_SHA256,
            relPath = relPath,
            bytes = ByteArray(8) { it.toByte() },
            mime = "application/octet-stream"
        )

        val targetFile = File(baseDir, relPath)
        assertThat(targetFile.exists()).isTrue()

        val tmpFiles = targetFile.parentFile?.listFiles()?.filter {
            it.name.startsWith("${'$'}{targetFile.name}.tmp.")
        }.orEmpty()
        assertThat(tmpFiles).isEmpty()
    }
}
