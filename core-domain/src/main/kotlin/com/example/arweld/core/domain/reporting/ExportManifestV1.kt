package com.example.arweld.core.domain.reporting

import kotlinx.serialization.Serializable

@Serializable
data class ExportManifestV1(
    val manifestVersion: Int = 1,
    val generatedAt: String,
    val period: ManifestPeriod,
    val files: List<ManifestFileEntry>,
    val warnings: List<String> = emptyList(),
)

@Serializable
data class ManifestPeriod(
    val startMillis: Long,
    val endMillis: Long,
)

@Serializable
data class ManifestFileEntry(
    val name: String,
    val sizeBytes: Long,
    val sha256Hex: String,
)
