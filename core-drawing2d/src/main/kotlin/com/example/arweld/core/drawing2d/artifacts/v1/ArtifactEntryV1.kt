package com.example.arweld.core.drawing2d.artifacts.v1

import kotlinx.serialization.Serializable

/**
 * Manifest entry for a stored artifact.
 *
 * @property kind Artifact kind classification
 * @property relPath Relative path within the artifact bundle
 * @property sha256 SHA-256 checksum (lowercase hex, 64 chars)
 * @property byteSize File size in bytes
 * @property mime MIME type (e.g. image/png, application/json)
 * @property pixelSha256 Optional pixel hash for images (hex)
 */
@Serializable
data class ArtifactEntryV1(
    val kind: ArtifactKindV1,
    val relPath: String,
    val sha256: String,
    val byteSize: Long,
    val mime: String,
    val pixelSha256: String? = null
) {
    /**
     * Canonical key used for deterministic ordering of entries.
     */
    fun canonicalKey(): Pair<ArtifactKindV1, String> {
        return kind to relPath
    }
}
