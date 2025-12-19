package com.example.arweld.core.structural.model

import kotlinx.serialization.Serializable

/**
 * Корневая структура модели. Все единицы измерения — миллиметры (mm).
 * Plates are a minimal list of plates; bolts/angles/hardware are not modeled yet.
 */
@Serializable
data class StructuralModel(
    val id: String,
    val nodes: List<Node>,
    val members: List<Member>,
    val connections: List<Connection> = emptyList(),
    val plates: List<Plate> = emptyList(),
    val boltGroups: List<BoltGroup> = emptyList(),
    val meta: Map<String, String> = emptyMap()
)
