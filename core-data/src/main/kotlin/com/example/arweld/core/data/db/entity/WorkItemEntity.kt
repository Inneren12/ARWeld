package com.example.arweld.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for WorkItem table.
 */
@Entity(
    tableName = "work_items",
    indices = [
        Index(value = ["code"]),
    ],
)
data class WorkItemEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val zoneId: String?,
    val type: String,
    val code: String?,
    val description: String?,
    val nodeId: String?,
    val createdAt: Long?,
)
