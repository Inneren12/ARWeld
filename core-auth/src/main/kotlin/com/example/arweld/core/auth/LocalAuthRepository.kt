package com.example.arweld.core.auth

import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local implementation of AuthRepository for MVP.
 * Uses in-memory storage (no password required).
 */
@Singleton
class LocalAuthRepository @Inject constructor() : AuthRepository {

    private var currentUser: User? = null

    override suspend fun getCurrentUser(): User? {
        return currentUser
    }

    override suspend fun login(userId: String): Result<User> {
        // For MVP, create a stub user based on userId
        val user = when (userId) {
            "assembler1" -> User(
                id = "assembler1",
                username = "assembler1",
                displayName = "John Smith",
                role = Role.ASSEMBLER
            )
            "qc1" -> User(
                id = "qc1",
                username = "qc1",
                displayName = "Jane Doe",
                role = Role.QC
            )
            "supervisor1" -> User(
                id = "supervisor1",
                username = "supervisor1",
                displayName = "Bob Johnson",
                role = Role.SUPERVISOR
            )
            "director1" -> User(
                id = "director1",
                username = "director1",
                displayName = "Alice Williams",
                role = Role.DIRECTOR
            )
            else -> return Result.failure(Exception("User not found"))
        }
        currentUser = user
        return Result.success(user)
    }

    override suspend fun logout() {
        currentUser = null
    }
}
