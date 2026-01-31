package com.example.arweld.core.drawing2d.artifacts.v1

import kotlinx.serialization.Serializable

@Serializable
data class ProjectCompleteMarkerV1(
    val schemaVersion: Int = 1,
    val projectId: String,
    val status: String = "complete",
)
