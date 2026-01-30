package com.example.arweld.core.drawing2d.artifacts.layout.v1

object ProjectLayoutV1 {
    const val RAW_IMAGE = "raw/image"
    const val RECTIFIED_IMAGE_PNG = "rectified/rectified.png"
    const val DRAWING2D_JSON = "drawing2d/drawing2d.json"
    const val MODEL_JSON = "model/model.json"
    const val CAPTURE_META_JSON = "meta/capture_meta.json"
    const val MANIFEST_JSON = "manifest.json"
    const val CHECKSUMS_SHA256 = "checksums.sha256"

    fun overlay(name: String): String {
        val safe = safeName(name)
        return "overlays/${safe}.png"
    }

    fun patch(seq: Int): String {
        require(seq >= 0) { "patch sequence must be non-negative" }
        return "drawing2d/patches/${"%06d".format(seq)}.patch.json"
    }

    fun safeName(name: String): String {
        val normalized = name.trim().lowercase()
        val sanitized = normalized.replace(Regex("[^a-z0-9_-]"), "_")
        return sanitized.ifBlank { "unnamed" }
    }
}
