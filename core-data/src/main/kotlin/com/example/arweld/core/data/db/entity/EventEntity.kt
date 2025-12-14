package com.example.arweld.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for Event table.
 */
@Entity(
    tableName = "events",
    indices = [
        Index(value = ["workItemId"]),
        Index(value = ["actorId"])
    ]
)
data class EventEntity(
    @PrimaryKey val id: String,
    val workItemId: String,
    val type: String,
    val timestamp: Long,
    val actorId: String,
    val actorRole: String,
    val deviceId: String,
    val payloadJson: String?, // Raw JSON payload
)
