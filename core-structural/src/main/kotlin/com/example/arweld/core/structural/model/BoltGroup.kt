package com.example.arweld.core.structural.model

import kotlinx.serialization.Serializable

@Serializable
data class BoltGroup(
    val id: String,
    val boltDiaMm: Double,
    val grade: String? = null,
    val pattern: List<BoltPoint2D> = emptyList()
)
