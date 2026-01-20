package com.example.arweld.core.domain.reporting

import kotlinx.serialization.Serializable

@Serializable
data class NodeFailStats(
    val nodeId: String,
    val failCount: Int,
    val workItemIds: List<String> = emptyList(),
)
