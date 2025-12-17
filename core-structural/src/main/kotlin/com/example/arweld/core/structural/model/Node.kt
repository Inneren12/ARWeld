package com.example.arweld.core.structural.model

import kotlinx.serialization.Serializable

/**
 * Spatial node of the structural model.
 *
 * Coordinates are expressed in millimeters (mm) in the project-local
 * coordinate system.
 */
@Serializable
data class Node(
    val id: String,
    val xMm: Double,
    val yMm: Double,
    val zMm: Double
)
