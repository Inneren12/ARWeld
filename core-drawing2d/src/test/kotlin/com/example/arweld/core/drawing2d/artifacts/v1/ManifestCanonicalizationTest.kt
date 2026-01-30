package com.example.arweld.core.drawing2d.artifacts.v1

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ManifestCanonicalizationTest {

    @Test
    fun `canonicalize sorts artifacts by kind and relPath`() {
        val manifest = ManifestV1(
            projectId = "project-001",
            artifacts = listOf(
                artifact(kind = ArtifactKindV1.OVERLAY, relPath = "b/overlay.png"),
                artifact(kind = ArtifactKindV1.RAW_IMAGE, relPath = "b/raw.png"),
                artifact(kind = ArtifactKindV1.OVERLAY, relPath = "a/overlay.png"),
                artifact(kind = ArtifactKindV1.CAPTURE_META, relPath = "meta/capture.json"),
                artifact(kind = ArtifactKindV1.RAW_IMAGE, relPath = "a/raw.png")
            )
        )

        val canonical = manifest.canonicalize()

        assertThat(canonical.artifacts.map { it.kind }).containsExactly(
            ArtifactKindV1.RAW_IMAGE,
            ArtifactKindV1.RAW_IMAGE,
            ArtifactKindV1.OVERLAY,
            ArtifactKindV1.OVERLAY,
            ArtifactKindV1.CAPTURE_META
        ).inOrder()
        assertThat(canonical.artifacts.map { it.relPath }).containsExactly(
            "a/raw.png",
            "b/raw.png",
            "a/overlay.png",
            "b/overlay.png",
            "meta/capture.json"
        ).inOrder()
    }

    @Test
    fun `canonicalize keeps JSON deterministic`() {
        val manifest = ManifestV1(
            projectId = "project-002",
            createdAtUtc = "2026-01-31T12:00:00Z",
            createdBy = CreatedByV1(appVersion = "1.0.0", gitSha = "abc123", deviceModel = "Pixel"),
            artifacts = listOf(
                artifact(kind = ArtifactKindV1.DRAWING2D_JSON, relPath = "drawing/drawing.json"),
                artifact(kind = ArtifactKindV1.RAW_IMAGE, relPath = "images/raw.png")
            )
        )

        val canonical = manifest.canonicalize()
        val json1 = Drawing2DJson.encodeToString(canonical)
        val json2 = Drawing2DJson.encodeToString(canonical)

        assertThat(json1).isEqualTo(json2)
    }

    @Test
    fun `canonical key matches kind and relPath`() {
        val entry = artifact(kind = ArtifactKindV1.MODEL_JSON, relPath = "models/model.json")

        assertThat(entry.canonicalKey()).isEqualTo(ArtifactKindV1.MODEL_JSON to "models/model.json")
    }

    @Test
    fun `sha256 helper validates hex length`() {
        val valid = "a".repeat(64)
        val invalid = "xyz"

        assertThat(isValidSha256(valid)).isTrue()
        assertThat(isValidSha256(invalid)).isFalse()
    }

    private fun artifact(kind: ArtifactKindV1, relPath: String): ArtifactEntryV1 {
        val sha256 = when (kind) {
            ArtifactKindV1.RAW_IMAGE -> "a".repeat(64)
            ArtifactKindV1.OVERLAY -> "b".repeat(64)
            else -> "c".repeat(64)
        }
        return ArtifactEntryV1(
            kind = kind,
            relPath = relPath,
            sha256 = sha256,
            byteSize = 1024,
            mime = "application/octet-stream",
            pixelSha256 = null
        )
    }

    private fun isValidSha256(value: String): Boolean {
        return value.length == 64 && value.all { it in '0'..'9' || it in 'a'..'f' }
    }
}
