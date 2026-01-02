package com.example.arweld.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arweld.core.data.db.entity.EvidenceEntity

/**
 * Data Access Object for Evidence table.
 */
@Dao
interface EvidenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(evidence: EvidenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(evidence: List<EvidenceEntity>)

    @Query("SELECT * FROM evidence WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): EvidenceEntity?

    @Query("SELECT * FROM evidence WHERE eventId = :eventId ORDER BY createdAt ASC")
    suspend fun listByEvent(eventId: String): List<EvidenceEntity>

    /**
     * Batch query to fetch evidence for multiple events.
     * Useful for loading evidence for a timeline without N+1 queries.
     */
    @Query("SELECT * FROM evidence WHERE eventId IN (:eventIds)")
    suspend fun listByEvents(eventIds: List<String>): List<EvidenceEntity>

    /**
     * Get all evidence associated with a work item.
     * Joins with events table to find all evidence for events related to this work item.
     */
    @Query("""
        SELECT e.* FROM evidence e
        INNER JOIN events ev ON e.eventId = ev.id
        WHERE ev.workItemId = :workItemId
        ORDER BY e.createdAt ASC
    """)
    suspend fun listByWorkItem(workItemId: String): List<EvidenceEntity>
}
