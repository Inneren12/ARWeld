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
    val payloadJson: String,
    val createdAt: Long,
    val status: String,
    val retryCount: Int,
)
