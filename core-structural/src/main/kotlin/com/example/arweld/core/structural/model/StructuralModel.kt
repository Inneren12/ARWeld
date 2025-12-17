package com.example.arweld.core.structural.model

import kotlinx.serialization.Serializable

/**
 * Structural model snapshot (v0.1).
 *
 * All coordinates and dimensions are expressed in millimeters (mm).
 * Metadata map can store auxiliary properties such as units or source info.
 */
@Serializable
data class StructuralModel(
    val id: String,
    val nodes: List<Node>,
    val members: List<Member>,
    val connections: List<Connection> = emptyList(),
    val plates: List<Plate> = emptyList(),
    val meta: Map<String, String> = emptyMap()
)
