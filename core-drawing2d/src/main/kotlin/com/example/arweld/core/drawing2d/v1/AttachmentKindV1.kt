package com.example.arweld.core.drawing2d.v1

import kotlinx.serialization.Serializable

/**
 * Enumerates supported attachment kinds for Drawing2D v1 exports.
 */
@Serializable
enum class AttachmentKindV1 {
    RAW_IMAGE,
    RECTIFIED_IMAGE,
    OVERLAY,
    CAPTURE_META,
    DRAWING2D_JSON,
    MODEL_JSON,
    PATCH_JSON
}
