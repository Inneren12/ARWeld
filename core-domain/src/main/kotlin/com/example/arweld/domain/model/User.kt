package com.example.arweld.domain.model

import kotlinx.serialization.Serializable

/**
 * Basic user identity used across the app.
 */
@Serializable
data class User(
    val id: String,
    val displayName: String,
    val role: Role
)
