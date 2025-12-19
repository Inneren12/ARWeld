package com.example.arweld.core.structural.model

import kotlinx.serialization.Serializable

/**
 * Connection references member/plate ids; no bolts or detailed hardware yet.
 */
@Serializable
data class Connection(
    val id: String,
    val memberIds: List<String>,
    val plateIds: List<String> = emptyList(),
    val boltGroupIds: List<String> = emptyList()
)
