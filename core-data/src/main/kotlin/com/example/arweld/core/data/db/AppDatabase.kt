package com.example.arweld.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.EvidenceDao
import com.example.arweld.core.data.db.dao.SyncQueueDao
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.db.entity.EventEntity
import com.example.arweld.core.data.db.entity.EvidenceEntity
import com.example.arweld.core.data.db.entity.SyncQueueEntity
import com.example.arweld.core.data.db.entity.UserEntity
import com.example.arweld.core.data.db.entity.WorkItemEntity

/**
 * Room database for ARWeld application.
 * Contains tables for WorkItems, Events, Evidence, and more.
 */
@Database(
    entities = [
        WorkItemEntity::class,
        EventEntity::class,
        EvidenceEntity::class,
        UserEntity::class,
        SyncQueueEntity::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workItemDao(): WorkItemDao
    abstract fun eventDao(): EventDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun userDao(): UserDao
    abstract fun syncQueueDao(): SyncQueueDao
}
