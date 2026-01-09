package com.example.arweld.core.domain.auth

import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.User
import kotlinx.coroutines.flow.StateFlow

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
     * Returns the list of locally available users (seeded into Room on first launch).
     */
    suspend fun availableUsers(): List<User>

    /**
     * Logs in as a specific seeded user. Fails if the user is not present in the database.
     */
    suspend fun loginWithUserId(userId: String): User

    /**
     * Returns the currently signed-in user, or null if none has been cached.
     */
    suspend fun currentUser(): User?

    /**
     * Observable flow of the current user state. Emits null when logged out, non-null when logged in.
     */
    val currentUserFlow: StateFlow<User?>

    /**
     * Clears the active session and any persisted credentials.
     */
    suspend fun logout()
}
