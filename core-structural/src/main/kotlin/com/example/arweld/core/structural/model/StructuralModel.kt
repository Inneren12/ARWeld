package com.example.arweld.core.structural.model

import kotlinx.serialization.Serializable

/**
 * Корневая структура модели.
 */
@Serializable
data class StructuralModel(
    val id: String,
    val nodes: List<Node>,
    val members: List<Member>,
    val connections: List<Connection>,
    val meta: Map<String, String> = emptyMap()
)
