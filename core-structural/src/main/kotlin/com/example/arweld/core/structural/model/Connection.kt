package com.example.arweld.core.structural.model

import kotlinx.serialization.Serializable

/**
 * Plate definition; all dimensions in millimeters (mm).
 */
@Serializable
data class Plate(
    val id: String,
    val thicknessMm: Double,
    val widthMm: Double,
    val lengthMm: Double
)

@Serializable
data class Connection(
    val id: String,
    val memberIds: List<String>,
    val plateIds: List<String>
)
