package com.example.arweld.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.arweld.core.data.db.entity.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Event table.
 */
@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: EventEntity): Long

    @Query("SELECT * FROM events WHERE workItemId = :workItemId ORDER BY timestamp ASC")
    fun observeByWorkItem(workItemId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestEvents(limit: Int): List<EventEntity>
}
