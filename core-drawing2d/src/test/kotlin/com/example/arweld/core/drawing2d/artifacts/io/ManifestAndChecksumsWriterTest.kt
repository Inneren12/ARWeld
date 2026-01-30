package com.example.arweld.core.drawing2d.artifacts.io

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.core.drawing2d.artifacts.v1.ManifestV1
import com.example.arweld.core.drawing2d.artifacts.v1.canonicalize
import com.example.arweld.core.drawing2d.crypto.Sha256V1
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ManifestAndChecksumsWriterTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `writes canonical manifest json`() {
        val baseDir = tempFolder.newFolder("artifacts")
        val writer = ManifestWriterV1()
        val manifest = ManifestV1(
            projectId = "project-123",
            artifacts = listOf(
                ArtifactEntryV1(
                    kind = ArtifactKindV1.RECTIFIED_IMAGE,
                    relPath = "rectified/rectified.png",
                    sha256 = "bbb",
                    byteSize = 20,
                    mime = "image/png"
                ),
                ArtifactEntryV1(
                    kind = ArtifactKindV1.RAW_IMAGE,
                    relPath = "raw/image",
                    sha256 = "aaa",
                    byteSize = 10,
                    mime = "image/jpeg"
                )
            )
        )

        writer.write(baseDir, manifest)

        val manifestFile = File(baseDir, ProjectLayoutV1.MANIFEST_JSON)
        val decoded = Drawing2DJson.decodeFromString<ManifestV1>(manifestFile.readText())
        assertThat(decoded).isEqualTo(manifest.canonicalize())
    }

    @Test
    fun `writes checksums sorted by relPath`() {
        val baseDir = tempFolder.newFolder("artifacts")
        val writer = ChecksumsWriterV1()
        val artifacts = listOf(
            ArtifactEntryV1(
                kind = ArtifactKindV1.RAW_IMAGE,
                relPath = "zeta.bin",
                sha256 = Sha256V1.hashBytes("zeta".toByteArray()),
                byteSize = 4,
                mime = "application/octet-stream"
            ),
            ArtifactEntryV1(
                kind = ArtifactKindV1.DRAWING2D_JSON,
                relPath = "alpha.json",
                sha256 = Sha256V1.hashBytes("alpha".toByteArray()),
                byteSize = 5,
                mime = "application/json"
            ),
            ArtifactEntryV1(
                kind = ArtifactKindV1.MODEL_JSON,
                relPath = "model/model.json",
                sha256 = Sha256V1.hashBytes("model".toByteArray()),
                byteSize = 5,
                mime = "application/json"
            )
        )

        val entry = writer.write(baseDir, artifacts)

        val expected = buildString {
            append(Sha256V1.hashBytes("alpha".toByteArray()))
            append("  alpha.json\n")
            append(Sha256V1.hashBytes("model".toByteArray()))
            append("  model/model.json\n")
            append(Sha256V1.hashBytes("zeta".toByteArray()))
            append("  zeta.bin\n")
        }
        val checksumsFile = File(baseDir, ProjectLayoutV1.CHECKSUMS_SHA256)
        assertThat(checksumsFile.readText()).isEqualTo(expected)
        assertThat(entry.relPath).isEqualTo(ProjectLayoutV1.CHECKSUMS_SHA256)
        assertThat(entry.kind).isEqualTo(ArtifactKindV1.CHECKSUMS_SHA256)
    }
}
