package com.example.arweld.core.drawing2d.v1.patch

import com.example.arweld.core.drawing2d.v1.MetaEntryV1
import kotlinx.serialization.Serializable

/**
 * Patch event for Drawing2D schema v1.
 */
@Serializable
data class DrawingPatchEvent(
    val schemaVersion: Int = 1,
    val eventId: String,
    val drawingId: String,
    val baseRev: Long,
    val ops: List<PatchOpV1>,
    val author: String? = null,
    val meta: List<MetaEntryV1> = emptyList()
)
