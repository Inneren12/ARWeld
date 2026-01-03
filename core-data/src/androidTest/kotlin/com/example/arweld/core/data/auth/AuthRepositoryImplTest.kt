package com.example.arweld.core.data.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.arweld.core.data.db.AppDatabase
import com.example.arweld.core.data.seed.SeedUsers
import com.example.arweld.core.domain.model.Role
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.After
import org.junit.Test
class AuthRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun loginMockStoresUserAndReflectsCurrentUser() = runBlocking {
        val userDao = database.userDao()
        userDao.insertAll(SeedUsers.users)

        val repository = AuthRepositoryImpl(context, userDao)

        val user = repository.loginMock(Role.ASSEMBLER)

        assertEquals("u-asm-1", user.id)
        assertEquals(Role.ASSEMBLER, user.role)
        assertEquals(user, repository.currentUser())
    }

    @Test
    fun logoutClearsCachedAndStoredUser() = runBlocking {
        val userDao = database.userDao()
        userDao.insertAll(SeedUsers.users)

        val repository = AuthRepositoryImpl(context, userDao)

        repository.loginMock(Role.SUPERVISOR)
        repository.logout()

        assertNull(repository.currentUser())
    }
}
