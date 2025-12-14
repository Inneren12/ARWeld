package com.example.arweld.core.data.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.arweld.core.domain.model.Role
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
class AuthRepositoryImplTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun loginMockStoresUserAndReflectsCurrentUser() = runBlocking {
        val repository = AuthRepositoryImpl(context)

        val user = repository.loginMock(Role.ASSEMBLER)

        assertEquals(Role.ASSEMBLER, user.role)
        assertEquals(user, repository.currentUser())
    }

    @Test
    fun logoutClearsCachedAndStoredUser() = runBlocking {
        val repository = AuthRepositoryImpl(context)

        repository.loginMock(Role.SUPERVISOR)
        repository.logout()

        assertNull(repository.currentUser())
    }
}
