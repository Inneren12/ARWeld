package com.example.arweld.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local sync queue entries captured for offline-first operations.
 */
@Entity(
    tableName = "sync_queue",
    indices = [Index(value = ["status"])]
)
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val type: String,
    val eventType: String,
    val workItemId: String?,
    val payloadJson: String,
    val status: String,
    val createdAt: Long,
)
