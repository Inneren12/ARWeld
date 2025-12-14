package com.example.arweld.core.auth

import com.example.arweld.core.domain.model.User

/**
 * Repository interface for authentication and session management.
 * MVP implementation uses local user storage (no password).
 */
interface AuthRepository {
    suspend fun getCurrentUser(): User?
    suspend fun login(userId: String): Result<User>
    suspend fun logout()
}
