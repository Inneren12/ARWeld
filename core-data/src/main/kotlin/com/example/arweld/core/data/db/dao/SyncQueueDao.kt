package com.example.arweld.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arweld.core.data.db.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the sync queue table.
 */
@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueEvent(item: SyncQueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SyncQueueEntity>)

    @Query(
        "SELECT * FROM sync_queue WHERE status = :pendingStatus ORDER BY createdAt ASC LIMIT :limit"
    )
    suspend fun getPending(
        pendingStatus: String = "PENDING",
        limit: Int = 100,
    ): List<SyncQueueEntity>

    @Query(
        "SELECT * FROM sync_queue WHERE status = :errorStatus ORDER BY createdAt ASC LIMIT :limit"
    )
    suspend fun getErrors(
        errorStatus: String = "ERROR",
        limit: Int = 100,
    ): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = :pendingStatus")
    suspend fun getPendingCount(pendingStatus: String = "PENDING"): Int

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = :errorStatus")
    suspend fun getErrorCount(errorStatus: String = "ERROR"): Int

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = :pendingStatus")
    fun observePendingCount(pendingStatus: String = "PENDING"): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = :errorStatus")
    fun observeErrorCount(errorStatus: String = "ERROR"): Flow<Int>

    @Query("SELECT MAX(createdAt) FROM sync_queue")
    fun observeLastEnqueuedAt(): Flow<Long?>

    @Query(
        "UPDATE sync_queue SET status = :status WHERE id = :id"
    )
    suspend fun updateStatus(
        id: String,
        status: String,
    )

    @Query(
        "UPDATE sync_queue SET lastAttemptAt = :timestamp WHERE id = :id"
    )
    suspend fun updateLastAttempt(
        id: String,
        timestamp: Long,
    )
}
