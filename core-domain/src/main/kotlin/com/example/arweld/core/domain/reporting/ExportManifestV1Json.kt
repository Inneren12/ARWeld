package com.example.arweld.core.domain.reporting

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ExportManifestV1Json {
    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = false
    }

    fun encode(manifest: ExportManifestV1): String = json.encodeToString(manifest)
}
