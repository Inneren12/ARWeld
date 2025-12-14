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

    @Query("SELECT * FROM evidence WHERE eventId = :eventId")
    suspend fun getByEventId(eventId: String): List<EvidenceEntity>
}
