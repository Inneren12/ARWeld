package com.example.arweld.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Event table.
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val workItemId: String,
    val type: String,
    val actorId: String,
    val deviceId: String,
    val timestamp: Long,
    val payload: String // JSON serialized
)
