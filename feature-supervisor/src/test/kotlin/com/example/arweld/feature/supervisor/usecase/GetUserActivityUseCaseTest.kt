package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.db.entity.EventEntity
import com.example.arweld.core.data.db.entity.UserEntity
import com.example.arweld.core.data.db.entity.WorkItemEntity
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for GetUserActivityUseCase.
 * Verifies "who does what" computation from event log.
 */
class GetUserActivityUseCaseTest {

    private lateinit var userDao: UserDao
    private lateinit var eventDao: EventDao
    private lateinit var workItemDao: WorkItemDao
    private lateinit var useCase: GetUserActivityUseCase

    @Before
    fun setup() {
        userDao = mock()
        eventDao = mock()
        workItemDao = mock()
        useCase = GetUserActivityUseCase(userDao, eventDao, workItemDao)
    }

    @Test
    fun `getUserActivity with no users returns empty list`() = runTest {
        // Given: no users
        whenever(userDao.observeAll()).thenReturn(flowOf(emptyList()))

        // When
        val activities = useCase()

        // Then
        assertTrue("Expected no activities when no users exist", activities.isEmpty())
    }

    @Test
    fun `getUserActivity filters out users with no events`() = runTest {
        // Given: 2 users, only 1 has events
        val users = listOf(
            createUserEntity("user1", "Alice", Role.ASSEMBLER),
            createUserEntity("user2", "Bob", Role.QC)
        )

        whenever(userDao.observeAll()).thenReturn(flowOf(users))

        // Mock event dao: only user1 has events
        whenever(eventDao.getLastEventByUser("user1")).thenReturn(
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, System.currentTimeMillis())
        )
        whenever(eventDao.getLastEventByUser("user2")).thenReturn(null)

        // Mock work item dao
        whenever(workItemDao.getById("wi1")).thenReturn(
            createWorkItemEntity("wi1", "WI-001")
        )

        // When
        val activities = useCase()

        // Then: only user1 should be included
        assertEquals(1, activities.size)
        assertEquals("user1", activities[0].userId)
        assertEquals("Alice", activities[0].userName)
    }

    @Test
    fun `getUserActivity includes last event details`() = runTest {
        // Given: user with event
        val now = System.currentTimeMillis()
        val users = listOf(
            createUserEntity("user1", "Alice", Role.ASSEMBLER)
        )

        whenever(userDao.observeAll()).thenReturn(flowOf(users))

        // Mock event dao
        whenever(eventDao.getLastEventByUser("user1")).thenReturn(
            createEventEntity("e1", "wi1", EventType.WORK_STARTED, now - 3600000)
        )

        // Mock work item dao
        whenever(workItemDao.getById("wi1")).thenReturn(
            createWorkItemEntity("wi1", "WI-001")
        )

        // When
        val activities = useCase()

        // Then
        assertEquals(1, activities.size)
        assertEquals("user1", activities[0].userId)
        assertEquals("Alice", activities[0].userName)
        assertEquals(Role.ASSEMBLER, activities[0].role)
        assertEquals("wi1", activities[0].currentWorkItemId)
        assertEquals("WI-001", activities[0].currentWorkItemCode)
        assertEquals("WORK_STARTED", activities[0].lastActionType)
        assertEquals(now - 3600000, activities[0].lastActionTimeMs)
    }

    @Test
    fun `getUserActivity sorts by last action time descending`() = runTest {
        // Given: 3 users with different last action times
        val now = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000L
        val twoHours = 2 * oneHour
        val threeHours = 3 * oneHour

        val users = listOf(
            createUserEntity("user1", "Alice", Role.ASSEMBLER),
            createUserEntity("user2", "Bob", Role.QC),
            createUserEntity("user3", "Charlie", Role.SUPERVISOR)
        )

        whenever(userDao.observeAll()).thenReturn(flowOf(users))

        // Mock event dao: user2 has most recent action, then user1, then user3
        whenever(eventDao.getLastEventByUser("user1")).thenReturn(
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - twoHours, actorId = "user1")
        )
        whenever(eventDao.getLastEventByUser("user2")).thenReturn(
            createEventEntity("e2", "wi2", EventType.QC_STARTED, now - oneHour, actorId = "user2")
        )
        whenever(eventDao.getLastEventByUser("user3")).thenReturn(
            createEventEntity("e3", "wi3", EventType.WORK_STARTED, now - threeHours, actorId = "user3")
        )

        // Mock work item dao
        whenever(workItemDao.getById("wi1")).thenReturn(createWorkItemEntity("wi1", "WI-001"))
        whenever(workItemDao.getById("wi2")).thenReturn(createWorkItemEntity("wi2", "WI-002"))
        whenever(workItemDao.getById("wi3")).thenReturn(createWorkItemEntity("wi3", "WI-003"))

        // When
        val activities = useCase()

        // Then: should be sorted by last action time descending (user2, user1, user3)
        assertEquals(3, activities.size)
        assertEquals("user2", activities[0].userId)  // Most recent (1 hour ago)
        assertEquals("user1", activities[1].userId)  // 2 hours ago
        assertEquals("user3", activities[2].userId)  // 3 hours ago (oldest)

        // Verify timestamps are in descending order
        assertTrue(activities[0].lastActionTimeMs > activities[1].lastActionTimeMs)
        assertTrue(activities[1].lastActionTimeMs > activities[2].lastActionTimeMs)
    }

    @Test
    fun `getUserActivity handles missing work item code gracefully`() = runTest {
        // Given: user with event but work item doesn't exist
        val now = System.currentTimeMillis()
        val users = listOf(
            createUserEntity("user1", "Alice", Role.ASSEMBLER)
        )

        whenever(userDao.observeAll()).thenReturn(flowOf(users))

        // Mock event dao
        whenever(eventDao.getLastEventByUser("user1")).thenReturn(
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - 3600000)
        )

        // Mock work item dao: work item not found
        whenever(workItemDao.getById("wi1")).thenReturn(null)

        // When
        val activities = useCase()

        // Then: should still include activity but with null code
        assertEquals(1, activities.size)
        assertEquals("wi1", activities[0].currentWorkItemId)
        assertNull(activities[0].currentWorkItemCode)
    }

    @Test
    fun `getUserActivity includes different event types`() = runTest {
        // Given: users with different event types
        val now = System.currentTimeMillis()
        val users = listOf(
            createUserEntity("user1", "Alice", Role.ASSEMBLER),
            createUserEntity("user2", "Bob", Role.QC),
            createUserEntity("user3", "Charlie", Role.ASSEMBLER)
        )

        whenever(userDao.observeAll()).thenReturn(flowOf(users))

        // Mock event dao with different event types
        whenever(eventDao.getLastEventByUser("user1")).thenReturn(
            createEventEntity("e1", "wi1", EventType.WORK_STARTED, now - 1800000, actorId = "user1")
        )
        whenever(eventDao.getLastEventByUser("user2")).thenReturn(
            createEventEntity("e2", "wi2", EventType.QC_PASSED, now - 900000, actorId = "user2")
        )
        whenever(eventDao.getLastEventByUser("user3")).thenReturn(
            createEventEntity("e3", "wi3", EventType.WORK_READY_FOR_QC, now - 300000, actorId = "user3")
        )

        // Mock work item dao
        whenever(workItemDao.getById("wi1")).thenReturn(createWorkItemEntity("wi1", "WI-001"))
        whenever(workItemDao.getById("wi2")).thenReturn(createWorkItemEntity("wi2", "WI-002"))
        whenever(workItemDao.getById("wi3")).thenReturn(createWorkItemEntity("wi3", "WI-003"))

        // When
        val activities = useCase()

        // Then: should include all event types
        assertEquals(3, activities.size)
        assertTrue(activities.any { it.lastActionType == "WORK_STARTED" })
        assertTrue(activities.any { it.lastActionType == "QC_PASSED" })
        assertTrue(activities.any { it.lastActionType == "WORK_READY_FOR_QC" })
    }

    @Test
    fun `getUserActivity query efficiency - uses LIMIT 1 on DAO`() = runTest {
        // Given: user with event
        val now = System.currentTimeMillis()
        val users = listOf(
            createUserEntity("user1", "Alice", Role.ASSEMBLER)
        )

        whenever(userDao.observeAll()).thenReturn(flowOf(users))

        // Mock event dao - getLastEventByUser should be called (which uses LIMIT 1)
        whenever(eventDao.getLastEventByUser("user1")).thenReturn(
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - 3600000)
        )

        // Mock work item dao
        whenever(workItemDao.getById("wi1")).thenReturn(
            createWorkItemEntity("wi1", "WI-001")
        )

        // When
        val activities = useCase()

        // Then: should successfully get activity
        assertEquals(1, activities.size)

        // Note: This test verifies that we're using getLastEventByUser (with LIMIT 1)
        // instead of getLastEventsByUser (which could return all events for a user)
        // The former is more efficient for this use case
    }

    private fun createUserEntity(id: String, name: String, role: Role) = UserEntity(
        id = id,
        displayName = name,
        role = role.name,
        isActive = true
    )

    private fun createWorkItemEntity(id: String, code: String) = WorkItemEntity(
        id = id,
        projectId = "project1",
        type = "NODE",
        code = code,
        description = "Test work item",
        zoneId = null,
        nodeId = null,
        createdAt = System.currentTimeMillis()
    )

    private fun createEventEntity(
        id: String,
        workItemId: String,
        type: EventType,
        timestamp: Long,
        actorId: String = "user1"
    ) = EventEntity(
        id = id,
        workItemId = workItemId,
        type = type.name,
        timestamp = timestamp,
        actorId = actorId,
        actorRole = Role.ASSEMBLER.name,
        deviceId = "device1",
        payloadJson = null
    )
}
