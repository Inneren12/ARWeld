package com.example.arweld.core.drawing2d.v1

import kotlinx.serialization.Serializable

/**
 * Reference to an attachment associated with a Drawing2D export.
 *
 * @property kind The attachment kind (raw image, rectified image, etc.)
 * @property relPath Relative path within the export bundle
 * @property sha256 Optional checksum (nullable until storage sprint)
 */
@Serializable
data class AttachmentRefV1(
    val kind: AttachmentKindV1,
    val relPath: String,
    val sha256: String? = null
)
