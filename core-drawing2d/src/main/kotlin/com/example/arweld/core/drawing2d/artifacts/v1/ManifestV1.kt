package com.example.arweld.core.drawing2d.artifacts.v1

import kotlinx.serialization.Serializable

/**
 * Manifest schema for a Drawing2D artifact bundle.
 *
 * @property schemaVersion Schema version for the manifest (v1 = 1)
 * @property projectId Project identifier this manifest belongs to
 * @property createdAtUtc Optional ISO-8601 timestamp (UTC) when generated
 * @property createdBy Optional creator metadata (fixed fields only)
 * @property artifacts List of artifact entries
 */
@Serializable
data class ManifestV1(
    val schemaVersion: Int = 1,
    val projectId: String,
    val createdAtUtc: String? = null,
    val createdBy: CreatedByV1? = null,
    val artifacts: List<ArtifactEntryV1>
)

/**
 * Optional creator metadata for manifests.
 */
@Serializable
data class CreatedByV1(
    val appVersion: String? = null,
    val gitSha: String? = null,
    val deviceModel: String? = null
)
