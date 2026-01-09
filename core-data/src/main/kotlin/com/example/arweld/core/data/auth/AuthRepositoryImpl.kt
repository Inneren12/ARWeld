package com.example.arweld.core.data.auth

import android.content.Context
import androidx.core.content.edit
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.entity.UserEntity
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val PREFS_NAME = "auth_prefs"
private const val KEY_CURRENT_USER = "current_user"

/**
 * Mock authentication repository for Sprint 1.
 *
 * Stores the current user in-memory and persists it to SharedPreferences so
 * the session survives process recreation within the app's lifetime.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val userDao: UserDao,
) : AuthRepository {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUserFlow = MutableStateFlow<User?>(null)
    override val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()

    init {
        val serializedUser = sharedPreferences.getString(KEY_CURRENT_USER, null)
        _currentUserFlow.value = serializedUser?.let {
            runCatching { json.decodeFromString(User.serializer(), it) }.getOrNull()
        }
    }

    override suspend fun loginMock(role: Role): User {
        val user = userDao.getFirstActiveByRole(role.name)?.toDomain()
            ?: User(
                id = "mock-${role.name.lowercase()}",
                username = "${role.name.lowercase()}@mock",
                displayName = role.name.lowercase().replaceFirstChar { it.titlecase() },
                role = role,
            )
        setUser(user)
        return user
    }

    override suspend fun availableUsers(): List<User> = userDao.getAll().map { it.toDomain() }

    override suspend fun loginWithUserId(userId: String): User {
        val user = userDao.getById(userId)?.toDomain()
            ?: error("User $userId not found in local database")
        setUser(user)
        return user
    }

    override suspend fun currentUser(): User? = _currentUserFlow.value

    override suspend fun logout() {
        _currentUserFlow.value = null
        sharedPreferences.edit { remove(KEY_CURRENT_USER) }
    }

    private fun setUser(user: User) {
        _currentUserFlow.value = user
        sharedPreferences.edit {
            putString(KEY_CURRENT_USER, json.encodeToString(User.serializer(), user))
        }
    }

    private val json: Json = Json { ignoreUnknownKeys = true }

    private fun UserEntity.toDomain(): User = User(
        id = id,
        username = id,
        displayName = name ?: role,
        role = Role.valueOf(role),
    )
}
