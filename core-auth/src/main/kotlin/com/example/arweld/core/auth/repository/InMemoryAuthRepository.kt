package com.example.arweld.core.auth.repository

import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryAuthRepository @Inject constructor() : AuthRepository {

    private val mutex = Mutex()
    private var cachedUser: User? = null
    private val _currentUserFlow = MutableStateFlow<User?>(null)
    override val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()

    private val _currentUserFlow = MutableStateFlow<User?>(null)
    override val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()

    private val _currentUserFlow = MutableStateFlow<User?>(null)
    override val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()

    private val _currentUserFlow = MutableStateFlow<User?>(null)
    overrid

    override suspend fun loginMock(role: Role): User = mutex.withLock {
        val user = User(
            id = UUID.randomUUID().toString(),
            username = role.name.lowercase(),
            displayName = role.toDisplayName(),
            role = role
        )
        cachedUser = user
        _currentUserFlow.value = user
        user
    }

    override suspend fun logout() {
        mutex.withLock {
            cachedUser = null
            _currentUserFlow.value = null
        }
    }

    override suspend fun currentUser(): User? = mutex.withLock { cachedUser }

    override suspend fun availableUsers(): List<User> = mutex.withLock {
        listOf(
            User(id = "mem-assembler", username = "assembler", displayName = "Assembler", role = Role.ASSEMBLER),
            User(id = "mem-qc", username = "qc", displayName = "QC", role = Role.QC),
            User(id = "mem-supervisor", username = "supervisor", displayName = "Supervisor", role = Role.SUPERVISOR),
        )
    }

    override suspend fun loginWithUserId(userId: String): User = mutex.withLock {
        val user = availableUsers().firstOrNull { it.id == userId }
            ?: error("User $userId not found in in-memory list")
        cachedUser = user
        _currentUserFlow.value = user
        user
    }

    private fun Role.toDisplayName(): String = when (this) {
        Role.ASSEMBLER -> "Assembler"
        Role.QC -> "QC Inspector"
        Role.SUPERVISOR -> "Supervisor"
        Role.DIRECTOR -> "Director"
    }
}
