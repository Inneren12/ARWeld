package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.db.entity.EventEntity
import com.example.arweld.core.data.db.entity.UserEntity
import com.example.arweld.core.data.db.entity.WorkItemEntity
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.state.WorkStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for GetWorkItemDetailUseCase.
 * Verifies work item detail retrieval and timeline generation with stable sorting.
 */
class GetWorkItemDetailUseCaseTest {

    private lateinit var workItemDao: WorkItemDao
    private lateinit var eventDao: EventDao
    private lateinit var userDao: UserDao
    private lateinit var useCase: GetWorkItemDetailUseCase

    @Before
    fun setup() {
        workItemDao = mock()
        eventDao = mock()
        userDao = mock()
        useCase = GetWorkItemDetailUseCase(workItemDao, eventDao, userDao)
    }

    @Test
    fun `getWorkItemDetail returns null when work item not found`() = runTest {
        // Given: work item doesn't exist
        whenever(workItemDao.getById("nonexistent")).thenReturn(null)

        // When
        val detail = useCase("nonexistent")

        // Then
        assertNull("Expected null when work item doesn't exist", detail)
    }

    @Test
    fun `getWorkItemDetail returns correct detail with derived state`() = runTest {
        // Given: work item with events
        val now = System.currentTimeMillis()
        val workItem = createWorkItemEntity("wi1", "WI-001", now - 7200000)

        whenever(workItemDao.getById("wi1")).thenReturn(workItem)

        // Mock events showing item is READY_FOR_QC
        val events = listOf(
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - 3600000, "user1"),
            createEventEntity("e2", "wi1", EventType.WORK_STARTED, now - 2400000, "user1"),
            createEventEntity("e3", "wi1", EventType.WORK_READY_FOR_QC, now - 1800000, "user1")
        )
        whenever(eventDao.getByWorkItemId("wi1")).thenReturn(events)

        // Mock user
        whenever(userDao.getById("user1")).thenReturn(
            createUserEntity("user1", "Alice", Role.ASSEMBLER)
        )

        // When
        val detail = useCase("wi1")

        // Then
        assertNotNull(detail)
        assertEquals("wi1", detail!!.workItem.id)
        assertEquals("WI-001", detail.workItem.code)
        assertEquals(WorkStatus.READY_FOR_QC, detail.status)
        assertEquals("user1", detail.currentAssigneeId)
        assertEquals("Alice", detail.currentAssigneeName)
        assertEquals(now - 7200000, detail.createdAt)
        assertEquals(now - 1800000, detail.lastUpdated) // Last event timestamp
    }

    @Test
    fun `getTimeline returns events in chronological order with stable sort`() = runTest {
        // Given: work item with multiple events at same timestamp
        val now = System.currentTimeMillis()
        val sameTimestamp = now - 3600000

        // Create events with same timestamp but different IDs
        // IDs are lexicographically: e1-aaa, e2-bbb, e3-ccc, e4-ddd
        val events = listOf(
            createEventEntity("e3-ccc", "wi1", EventType.WORK_READY_FOR_QC, sameTimestamp, "user1"),
            createEventEntity("e1-aaa", "wi1", EventType.WORK_CLAIMED, sameTimestamp - 1000, "user1"),
            createEventEntity("e4-ddd", "wi1", EventType.QC_STARTED, sameTimestamp + 1000, "user2"),
            createEventEntity("e2-bbb", "wi1", EventType.WORK_STARTED, sameTimestamp, "user1")
        )
        whenever(eventDao.getByWorkItemId("wi1")).thenReturn(events)

        // Mock users
        whenever(userDao.getById("user1")).thenReturn(
            createUserEntity("user1", "Alice", Role.ASSEMBLER)
        )
        whenever(userDao.getById("user2")).thenReturn(
            createUserEntity("user2", "Bob", Role.QC)
        )

        // When
        val timeline = useCase.getTimeline("wi1")

        // Then: should be sorted by timestamp first, then by eventId (stable sort)
        assertEquals(4, timeline.size)

        // First event: timestamp - 1000
        assertEquals("e1-aaa", timeline[0].eventId)
        assertEquals(sameTimestamp - 1000, timeline[0].timestamp)

        // Events at same timestamp should be sorted by eventId
        assertEquals("e2-bbb", timeline[1].eventId)
        assertEquals(sameTimestamp, timeline[1].timestamp)
        assertEquals("e3-ccc", timeline[2].eventId)
        assertEquals(sameTimestamp, timeline[2].timestamp)

        // Last event: timestamp + 1000
        assertEquals("e4-ddd", timeline[3].eventId)
        assertEquals(sameTimestamp + 1000, timeline[3].timestamp)
    }

    @Test
    fun `getTimeline includes actor information and formatted descriptions`() = runTest {
        // Given: work item with events from different actors
        val now = System.currentTimeMillis()
        val events = listOf(
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - 3600000, "user1"),
            createEventEntity("e2", "wi1", EventType.WORK_READY_FOR_QC, now - 1800000, "user1"),
            createEventEntity("e3", "wi1", EventType.QC_STARTED, now - 900000, "user2"),
            createEventEntity("e4", "wi1", EventType.QC_PASSED, now - 300000, "user2")
        )
        whenever(eventDao.getByWorkItemId("wi1")).thenReturn(events)

        // Mock users
        whenever(userDao.getById("user1")).thenReturn(
            createUserEntity("user1", "Alice", Role.ASSEMBLER)
        )
        whenever(userDao.getById("user2")).thenReturn(
            createUserEntity("user2", "Bob", Role.QC)
        )

        // When
        val timeline = useCase.getTimeline("wi1")

        // Then
        assertEquals(4, timeline.size)

        // Verify first entry
        assertEquals("user1", timeline[0].actorId)
        assertEquals("Alice", timeline[0].actorName)
        assertEquals(Role.ASSEMBLER, timeline[0].actorRole)
        assertEquals("WORK_CLAIMED", timeline[0].eventType)
        assertEquals("Alice claimed work", timeline[0].eventDescription)

        // Verify QC entry
        assertEquals("user2", timeline[2].actorId)
        assertEquals("Bob", timeline[2].actorName)
        assertEquals(Role.QC, timeline[2].actorRole)
        assertEquals("QC_STARTED", timeline[2].eventType)
        assertEquals("Bob started QC inspection", timeline[2].eventDescription)
    }

    @Test
    fun `getTimeline handles missing user gracefully`() = runTest {
        // Given: event with unknown user
        val now = System.currentTimeMillis()
        val events = listOf(
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - 3600000, "unknown_user")
        )
        whenever(eventDao.getByWorkItemId("wi1")).thenReturn(events)

        // Mock user dao returns null
        whenever(userDao.getById("unknown_user")).thenReturn(null)

        // When
        val timeline = useCase.getTimeline("wi1")

        // Then: should use "Unknown" as actor name
        assertEquals(1, timeline.size)
        assertEquals("unknown_user", timeline[0].actorId)
        assertEquals("Unknown", timeline[0].actorName)
        assertEquals("Unknown claimed work", timeline[0].eventDescription)
    }

    @Test
    fun `getTimeline with no events returns empty list`() = runTest {
        // Given: work item with no events
        whenever(eventDao.getByWorkItemId("wi1")).thenReturn(emptyList())

        // When
        val timeline = useCase.getTimeline("wi1")

        // Then
        assertTrue("Expected empty timeline when no events exist", timeline.isEmpty())
    }

    @Test
    fun `getTimeline includes payload summary when available`() = runTest {
        // Given: event with payload
        val now = System.currentTimeMillis()
        val events = listOf(
            createEventEntity(
                id = "e1",
                workItemId = "wi1",
                type = EventType.EVIDENCE_CAPTURED,
                timestamp = now,
                actorId = "user1",
                payloadJson = "{\"evidenceId\":\"ev123\",\"kind\":\"PHOTO\"}"
            )
        )
        whenever(eventDao.getByWorkItemId("wi1")).thenReturn(events)

        whenever(userDao.getById("user1")).thenReturn(
            createUserEntity("user1", "Alice", Role.ASSEMBLER)
        )

        // When
        val timeline = useCase.getTimeline("wi1")

        // Then
        assertEquals(1, timeline.size)
        assertEquals("{\"evidenceId\":\"ev123\",\"kind\":\"PHOTO\"}", timeline[0].payloadSummary)
    }

    @Test
    fun `getTimeline stable sort maintains order for events with identical timestamps`() = runTest {
        // Given: 5 events at exactly the same timestamp
        val now = System.currentTimeMillis()
        val sameTimestamp = now - 1000

        val events = listOf(
            createEventEntity("e-5", "wi1", EventType.WORK_CLAIMED, sameTimestamp, "user1"),
            createEventEntity("e-3", "wi1", EventType.WORK_CLAIMED, sameTimestamp, "user1"),
            createEventEntity("e-1", "wi1", EventType.WORK_CLAIMED, sameTimestamp, "user1"),
            createEventEntity("e-4", "wi1", EventType.WORK_CLAIMED, sameTimestamp, "user1"),
            createEventEntity("e-2", "wi1", EventType.WORK_CLAIMED, sameTimestamp, "user1")
        )
        whenever(eventDao.getByWorkItemId("wi1")).thenReturn(events)

        whenever(userDao.getById("user1")).thenReturn(
            createUserEntity("user1", "Alice", Role.ASSEMBLER)
        )

        // When
        val timeline = useCase.getTimeline("wi1")

        // Then: should be sorted by eventId lexicographically
        assertEquals(5, timeline.size)
        assertEquals("e-1", timeline[0].eventId)
        assertEquals("e-2", timeline[1].eventId)
        assertEquals("e-3", timeline[2].eventId)
        assertEquals("e-4", timeline[3].eventId)
        assertEquals("e-5", timeline[4].eventId)

        // All should have the same timestamp
        timeline.forEach { entry ->
            assertEquals(sameTimestamp, entry.timestamp)
        }
    }

    private fun createWorkItemEntity(
        id: String,
        code: String,
        createdAt: Long
    ) = WorkItemEntity(
        id = id,
        projectId = "project1",
        type = "NODE",
        code = code,
        description = "Test work item",
        zoneId = null,
        nodeId = null,
        createdAt = createdAt
    )

    private fun createUserEntity(id: String, name: String, role: Role, lastSeenAt: Long = 0L) = UserEntity(
        id = id,
        name = name,
        role = role.name,
        isActive = true,
        lastSeenAt = lastSeenAt,
    )

    private fun createEventEntity(
        id: String,
        workItemId: String,
        type: EventType,
        timestamp: Long,
        actorId: String,
        payloadJson: String? = null
    ) = EventEntity(
        id = id,
        workItemId = workItemId,
        type = type.name,
        timestamp = timestamp,
        actorId = actorId,
        actorRole = Role.ASSEMBLER.name,
        deviceId = "device1",
        payloadJson = payloadJson
    )
}
