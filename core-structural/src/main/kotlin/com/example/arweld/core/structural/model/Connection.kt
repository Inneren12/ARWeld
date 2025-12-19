package com.example.arweld.core.structural.model

import kotlinx.serialization.Serializable

/**
 * Connection references member/plate/bolt group ids; detailed hardware is not modeled yet
 * (bolt groups are minimal/reserved in v0.1).
 */
@Serializable
data class Connection(
    val id: String,
    val memberIds: List<String>,
    val plateIds: List<String> = emptyList(),
    val boltGroupIds: List<String> = emptyList()
)
