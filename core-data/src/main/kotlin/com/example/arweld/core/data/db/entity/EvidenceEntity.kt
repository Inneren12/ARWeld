package com.example.arweld.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing captured evidence linked to an event.
 */
@Entity(
    tableName = "evidence",
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["workItemId"]),
    ]
)
data class EvidenceEntity(
    @PrimaryKey val id: String,
    val workItemId: String,
    val eventId: String,
    val kind: String,
    val uri: String,
    val sha256: String,
    val sizeBytes: Long,
    val metaJson: String?,
    val createdAt: Long,
)
