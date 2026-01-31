package com.example.arweld.core.drawing2d.artifacts.io.v1

import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProjectFinalizerV1Test {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `finalize writes manifest checksums and marker`() {
        val projectDir = tempFolder.newFolder("project")
        val store = FileArtifactStoreV1(projectDir)
        val rawEntry = store.writeText(
            kind = ArtifactKindV1.RAW_IMAGE,
            relPath = ProjectLayoutV1.RAW_IMAGE,
            text = "raw",
            mime = "image/jpeg",
        )
        val rectifiedEntry = store.writeText(
            kind = ArtifactKindV1.RECTIFIED_IMAGE,
            relPath = ProjectLayoutV1.RECTIFIED_IMAGE_PNG,
            text = "rectified",
            mime = "image/png",
        )

        val outcome = ProjectFinalizerV1().finalize(projectDir, listOf(rawEntry, rectifiedEntry))

        assertThat(outcome).isInstanceOf(FinalizeOutcomeV1.Success::class.java)
        assertThat(File(projectDir, ProjectLayoutV1.MANIFEST_JSON).exists()).isTrue()
        assertThat(File(projectDir, ProjectLayoutV1.CHECKSUMS_SHA256).exists()).isTrue()
        assertThat(File(projectDir, ProjectLayoutV1.PROJECT_COMPLETE_JSON).exists()).isTrue()
        val artifacts = (outcome as FinalizeOutcomeV1.Success).artifacts
        assertThat(artifacts.map { it.relPath }).containsAtLeast(
            ProjectLayoutV1.MANIFEST_JSON,
            ProjectLayoutV1.CHECKSUMS_SHA256,
            ProjectLayoutV1.PROJECT_COMPLETE_JSON,
        )
    }

    @Test
    fun `finalize detects tampered file`() {
        val projectDir = tempFolder.newFolder("tamper")
        val store = FileArtifactStoreV1(projectDir)
        val entry = store.writeText(
            kind = ArtifactKindV1.RAW_IMAGE,
            relPath = ProjectLayoutV1.RAW_IMAGE,
            text = "original",
            mime = "image/jpeg",
        )
        File(projectDir, entry.relPath).writeText("changed")

        val outcome = ProjectFinalizerV1().finalize(projectDir, listOf(entry))

        assertThat(outcome).isInstanceOf(FinalizeOutcomeV1.Failure::class.java)
        val failure = (outcome as FinalizeOutcomeV1.Failure).failure
        assertThat(failure.code).isEqualTo(FinalizeFailureCodeV1.SHA_MISMATCH)
        assertThat(failure.relPath).isEqualTo(ProjectLayoutV1.RAW_IMAGE)
    }

    @Test
    fun `finalize detects missing file`() {
        val projectDir = tempFolder.newFolder("missing")
        val store = FileArtifactStoreV1(projectDir)
        val entry = store.writeText(
            kind = ArtifactKindV1.RAW_IMAGE,
            relPath = ProjectLayoutV1.RAW_IMAGE,
            text = "raw",
            mime = "image/jpeg",
        )
        File(projectDir, entry.relPath).delete()

        val outcome = ProjectFinalizerV1().finalize(projectDir, listOf(entry))

        assertThat(outcome).isInstanceOf(FinalizeOutcomeV1.Failure::class.java)
        val failure = (outcome as FinalizeOutcomeV1.Failure).failure
        assertThat(failure.code).isEqualTo(FinalizeFailureCodeV1.MISSING_FILE)
        assertThat(failure.relPath).isEqualTo(ProjectLayoutV1.RAW_IMAGE)
    }

    @Test
    fun `finalize is idempotent`() {
        val projectDir = tempFolder.newFolder("idempotent")
        val store = FileArtifactStoreV1(projectDir)
        val entry = store.writeText(
            kind = ArtifactKindV1.RAW_IMAGE,
            relPath = ProjectLayoutV1.RAW_IMAGE,
            text = "raw",
            mime = "image/jpeg",
        )

        val first = ProjectFinalizerV1().finalize(projectDir, listOf(entry)) as FinalizeOutcomeV1.Success
        val manifestBytes = File(projectDir, ProjectLayoutV1.MANIFEST_JSON).readBytes()
        val checksumsBytes = File(projectDir, ProjectLayoutV1.CHECKSUMS_SHA256).readBytes()

        val second = ProjectFinalizerV1().finalize(projectDir, first.artifacts) as FinalizeOutcomeV1.Success
        val manifestBytesSecond = File(projectDir, ProjectLayoutV1.MANIFEST_JSON).readBytes()
        val checksumsBytesSecond = File(projectDir, ProjectLayoutV1.CHECKSUMS_SHA256).readBytes()

        assertThat(second.artifacts.size).isEqualTo(first.artifacts.size)
        assertThat(manifestBytesSecond).isEqualTo(manifestBytes)
        assertThat(checksumsBytesSecond).isEqualTo(checksumsBytes)
    }
}
