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
    val fileUri: String = "",
    val mimeType: String = "",
    val sizeBytes: Long = 0,
    val status: String,
    val createdAt: Long,
    val lastAttemptAt: Long? = null,
)
