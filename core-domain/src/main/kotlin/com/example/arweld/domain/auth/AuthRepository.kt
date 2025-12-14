package com.example.arweld.domain.auth

import com.example.arweld.domain.model.Role
import com.example.arweld.domain.model.User

/**
 * Authentication contract for retrieving and mutating the current user session.
 */
interface AuthRepository {
    suspend fun loginMock(role: Role): User
    suspend fun logout()
    suspend fun currentUser(): User?
}
