package com.example.arweld.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.db.entity.EventEntity
import com.example.arweld.core.data.db.entity.WorkItemEntity

/**
 * Room database for ARWeld application.
 * Contains tables for WorkItems, Events, Evidence, and more.
 */
@Database(
    entities = [
        WorkItemEntity::class,
        EventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ArWeldDatabase : RoomDatabase() {
    abstract fun workItemDao(): WorkItemDao
    abstract fun eventDao(): EventDao
}
