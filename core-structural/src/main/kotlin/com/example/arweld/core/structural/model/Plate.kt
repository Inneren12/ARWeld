package com.example.arweld.core.structural.model

import kotlinx.serialization.Serializable

/**
 * Plate definition; all dimensions in millimeters (mm).
 */
@Serializable
data class Plate(
    val id: String,
    val thickness: Double,
    val width: Double,
    val length: Double
)
