package com.example.arweld.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a user in the system.
 */
@Serializable
data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val role: Role
)
