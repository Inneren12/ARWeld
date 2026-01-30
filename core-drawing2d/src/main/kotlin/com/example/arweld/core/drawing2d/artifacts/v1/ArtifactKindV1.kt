package com.example.arweld.core.drawing2d.artifacts.v1

import kotlinx.serialization.Serializable

/**
 * Artifact kinds supported in the v1 artifact manifest.
 */
@Serializable
enum class ArtifactKindV1 {
    RAW_IMAGE,
    RECTIFIED_IMAGE,
    OVERLAY,
    DRAWING2D_JSON,
    PATCH_JSON,
    MODEL_JSON,
    CAPTURE_META,
    MANIFEST_JSON,
    CHECKSUMS_SHA256
}
