package com.example.arweld.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing captured evidence linked to an event.
 */
@Entity(
    tableName = "evidence",
    indices = [Index(value = ["eventId"])]
)
data class EvidenceEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val kind: String,
    val uri: String,
    val sha256: String,
    val metaJson: String?,
    val createdAt: Long,
)
