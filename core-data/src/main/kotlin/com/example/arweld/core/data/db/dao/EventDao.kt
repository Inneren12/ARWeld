package com.example.arweld.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arweld.core.data.db.entity.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Event table.
 */
@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>)

    @Query("SELECT * FROM events WHERE workItemId = :workItemId ORDER BY timestamp ASC")
    suspend fun getByWorkItemId(workItemId: String): List<EventEntity>

    /**
     * Batch query to fetch events for multiple work items.
     * Orders by timestamp ASC to ensure deterministic event ordering for state reduction.
     */
    @Query("SELECT * FROM events WHERE workItemId IN (:workItemIds) ORDER BY timestamp ASC")
    suspend fun getByWorkItemIds(workItemIds: List<String>): List<EventEntity>

    /**
     * Get the last event for a user, ordered by timestamp DESC.
     * Use LIMIT 1 to avoid pulling large lists when only the latest event is needed.
     */
    @Query("SELECT * FROM events WHERE actorId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEventByUser(userId: String): EventEntity?

    @Query("SELECT * FROM events WHERE actorId = :userId ORDER BY timestamp DESC")
    suspend fun getLastEventsByUser(userId: String): List<EventEntity>

    @Query("SELECT * FROM events WHERE workItemId = :workItemId ORDER BY timestamp ASC")
    fun observeByWorkItem(workItemId: String): Flow<List<EventEntity>>
}
