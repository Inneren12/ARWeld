package com.example.arweld.core.domain.auth

import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.User

/**
 * Authentication contract for the ARWeld app.
 *
 * Sprint 1 provides a mock implementation that can later be swapped for
 * a real backend-backed data source without changing callers.
 */
interface AuthRepository {

    /**
     * Performs a mock login for the given [role].
     * Returns a synthetic [User] so the UI can exercise role-based flows.
     */
    suspend fun loginMock(role: Role): User

    /**
     * Returns the currently signed-in user, or null if none has been cached.
     */
    suspend fun currentUser(): User?

    /**
     * Clears the active session and any persisted credentials.
     */
    suspend fun logout()
}
