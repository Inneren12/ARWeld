package com.example.arweld.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing user records locally.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val role: String,
    val lastSeenAt: Long?,
    val isActive: Boolean = true,
)
