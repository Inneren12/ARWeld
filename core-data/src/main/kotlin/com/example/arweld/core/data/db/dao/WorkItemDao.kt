package com.example.arweld.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arweld.core.data.db.entity.WorkItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for WorkItem table.
 */
@Dao
interface WorkItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workItem: WorkItemEntity)

    @Query("SELECT * FROM work_items WHERE id = :id")
    suspend fun getById(id: String): WorkItemEntity?

    @Query("SELECT * FROM work_items WHERE code = :code LIMIT 1")
    suspend fun findByCode(code: String): WorkItemEntity?

    @Query("SELECT * FROM work_items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<WorkItemEntity>>
}
