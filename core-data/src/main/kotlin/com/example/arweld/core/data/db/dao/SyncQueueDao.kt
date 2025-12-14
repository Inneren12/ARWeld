package com.example.arweld.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arweld.core.data.db.entity.SyncQueueEntity

/**
 * Data Access Object for the sync queue table.
 */
@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SyncQueueEntity>)

    @Query(
        "SELECT * FROM sync_queue WHERE status = :pendingStatus ORDER BY createdAt ASC LIMIT :limit"
    )
    suspend fun getPending(
        pendingStatus: String = "PENDING",
        limit: Int = 100,
    ): List<SyncQueueEntity>
}
