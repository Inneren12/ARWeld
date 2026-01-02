package com.example.arweld.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arweld.core.data.db.entity.UserEntity

/**
 * Data Access Object for User table.
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY id")
    suspend fun getAll(): List<UserEntity>

    @Query("SELECT * FROM users WHERE role = :role AND isActive = 1 ORDER BY lastSeenAt DESC LIMIT 1")
    suspend fun getFirstActiveByRole(role: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)
}
