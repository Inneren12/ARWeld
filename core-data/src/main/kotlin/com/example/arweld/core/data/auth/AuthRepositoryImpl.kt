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

    @Volatile
    private var cachedUser: User? = null

    override suspend fun loginMock(role: Role): User {
        val user = userDao.getFirstActiveByRole(role.name)?.toDomain()
            ?: User(
                id = "mock-${role.name.lowercase()}",
                username = "${role.name.lowercase()}@mock",
                displayName = role.name.lowercase().replaceFirstChar { it.titlecase() },
                role = role,
            )
        cacheUser(user)
        return user
    }

    override suspend fun currentUser(): User? {
        cachedUser?.let { return it }
        val serializedUser = sharedPreferences.getString(KEY_CURRENT_USER, null) ?: return null
        return runCatching { json.decodeFromString(User.serializer(), serializedUser) }.
            getOrNull()?.also { cachedUser = it }
    }

    override suspend fun logout() {
        cachedUser = null
        sharedPreferences.edit { remove(KEY_CURRENT_USER) }
    }

    private fun cacheUser(user: User) {
        cachedUser = user
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
