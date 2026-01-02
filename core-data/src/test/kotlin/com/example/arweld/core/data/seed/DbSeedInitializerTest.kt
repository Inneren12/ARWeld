package com.example.arweld.core.data.seed

import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.db.entity.UserEntity
import com.example.arweld.core.data.db.entity.WorkItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DbSeedInitializerTest {

    @Test
    fun `seeds work items and users when tables are empty`() = runBlocking {
        val workItemDao = FakeWorkItemDao()
        val userDao = FakeUserDao()
        val initializer = DbSeedInitializer(workItemDao, userDao)

        initializer.seedIfEmpty()

        assertEquals(SeedWorkItems.items, workItemDao.items)
        assertEquals(SeedUsers.users, userDao.users)
    }

    @Test
    fun `does not reseed when tables already populated`() = runBlocking {
        val workItemDao = FakeWorkItemDao(
            mutableListOf(
                WorkItemEntity(
                    id = "existing",
                    projectId = "proj-1",
                    zoneId = null,
                    type = "NODE",
                    code = null,
                    description = null,
                    nodeId = null,
                    createdAt = null,
                )
            )
        )
        val userDao = FakeUserDao(
            mutableListOf(
                UserEntity(
                    id = "existing",
                    name = "Existing",
                    role = "ASSEMBLER",
                    lastSeenAt = null,
                )
            )
        )
        val initializer = DbSeedInitializer(workItemDao, userDao)

        initializer.seedIfEmpty()

        assertEquals(
            listOf(
                WorkItemEntity(
                    id = "existing",
                    projectId = "proj-1",
                    zoneId = null,
                    type = "NODE",
                    code = null,
                    description = null,
                    nodeId = null,
                    createdAt = null,
                )
            ),
            workItemDao.items,
        )
        assertEquals(
            listOf(
                UserEntity(
                    id = "existing",
                    name = "Existing",
                    role = "ASSEMBLER",
                    lastSeenAt = null,
                )
            ),
            userDao.users,
        )
    }
}

private class FakeWorkItemDao(
    val items: MutableList<WorkItemEntity> = mutableListOf(),
) : WorkItemDao {
    override suspend fun insert(workItem: WorkItemEntity) {
        items += workItem
    }

    override suspend fun insertAll(items: List<WorkItemEntity>) {
        this.items.addAll(items)
    }

    override suspend fun getByCode(code: String): WorkItemEntity? = items.firstOrNull { it.code == code }

    override suspend fun countAll(): Int = items.size

    override suspend fun getById(id: String): WorkItemEntity? = items.firstOrNull { it.id == id }

    override fun observeAll(): Flow<List<WorkItemEntity>> = flowOf(items.toList())
}

private class FakeUserDao(
    val users: MutableList<UserEntity> = mutableListOf(),
) : UserDao {
    override suspend fun getById(id: String): UserEntity? = users.firstOrNull { it.id == id }

    override suspend fun getAll(): List<UserEntity> = users.toList()

    override suspend fun getFirstActiveByRole(role: String): UserEntity? = users.firstOrNull { it.role == role }

    override suspend fun countAll(): Int = users.size

    override suspend fun insertAll(users: List<UserEntity>) {
        this.users.addAll(users)
    }
}
