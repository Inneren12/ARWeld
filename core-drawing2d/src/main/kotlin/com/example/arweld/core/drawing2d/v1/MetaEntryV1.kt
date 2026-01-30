package com.example.arweld.core.drawing2d.v1

import kotlinx.serialization.Serializable

/**
 * Key-value metadata entry for Drawing2D v1 schemas.
 */
@Serializable
data class MetaEntryV1(
    val key: String,
    val value: String
)
