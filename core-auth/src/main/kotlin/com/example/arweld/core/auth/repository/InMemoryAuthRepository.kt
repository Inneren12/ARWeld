package com.example.arweld.core.auth.repository

import com.example.arweld.domain.auth.AuthRepository
import com.example.arweld.domain.model.Role
import com.example.arweld.domain.model.User
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryAuthRepository @Inject constructor() : AuthRepository {

    private val mutex = Mutex()
    private var cachedUser: User? = null

    override suspend fun loginMock(role: Role): User = mutex.withLock {
        val user = User(
            id = UUID.randomUUID().toString(),
            displayName = role.toDisplayName(),
            role = role
        )
        cachedUser = user
        user
    }

    override suspend fun logout() {
        mutex.withLock {
            cachedUser = null
        }
    }

    override suspend fun currentUser(): User? = mutex.withLock { cachedUser }

    private fun Role.toDisplayName(): String = when (this) {
        Role.ASSEMBLER -> "Assembler"
        Role.QC -> "QC Inspector"
        Role.SUPERVISOR -> "Supervisor"
        Role.DIRECTOR -> "Director"
    }
}
