package com.example.arweld.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for WorkItem table.
 */
@Entity(tableName = "work_items")
data class WorkItemEntity(
    @PrimaryKey val id: String,
    val code: String,
    val type: String,
    val description: String,
    val zone: String?,
    val nodeId: String?,
    val createdAt: Long
)
