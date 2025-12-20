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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for GetQcBottleneckUseCase.
 * Verifies bottleneck detection with threshold filtering and correct sorting.
 */
class GetQcBottleneckUseCaseTest {

    private lateinit var workItemDao: WorkItemDao
    private lateinit var eventDao: EventDao
    private lateinit var userDao: UserDao
    private lateinit var useCase: GetQcBottleneckUseCase

    @Before
    fun setup() {
        workItemDao = mock()
        eventDao = mock()
        userDao = mock()
        useCase = GetQcBottleneckUseCase(workItemDao, eventDao, userDao)
    }

    @Test
    fun `getQcBottleneck with no READY_FOR_QC items returns empty list`() = runTest {
        // Given: work items not in READY_FOR_QC status
        val now = System.currentTimeMillis()
        val workItems = listOf(
            createWorkItemEntity("wi1"),
            createWorkItemEntity("wi2")
        )

        whenever(workItemDao.observeAll()).thenReturn(flowOf(workItems))

        // Mock batch query with events showing items are in progress
        val allEvents = listOf(
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - 3600000),
            createEventEntity("e2", "wi2", EventType.WORK_CLAIMED, now - 7200000)
        )
        whenever(eventDao.getByWorkItemIds(listOf("wi1", "wi2")))
            .thenReturn(allEvents)

        // When
        val bottlenecks = useCase(thresholdMs = 0L)

        // Then
        assertTrue("Expected no bottlenecks for items not in READY_FOR_QC", bottlenecks.isEmpty())
    }

    @Test
    fun `getQcBottleneck filters items below threshold`() = runTest {
        // Given: 3 items waiting for QC with different wait times
        val now = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000L
        val twoHours = 2 * oneHour
        val threeHours = 3 * oneHour

        val workItems = listOf(
            createWorkItemEntity("wi1"),
            createWorkItemEntity("wi2"),
            createWorkItemEntity("wi3")
        )

        whenever(workItemDao.observeAll()).thenReturn(flowOf(workItems))

        // Mock batch query with all events for all work items
        val allEvents = listOf(
            // wi1: waiting 30 minutes (below threshold)
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - oneHour),
            createEventEntity("e2", "wi1", EventType.WORK_READY_FOR_QC, now - (oneHour / 2)),
            // wi2: waiting 2 hours (above threshold)
            createEventEntity("e3", "wi2", EventType.WORK_CLAIMED, now - threeHours),
            createEventEntity("e4", "wi2", EventType.WORK_READY_FOR_QC, now - twoHours),
            // wi3: waiting 3 hours (above threshold)
            createEventEntity("e5", "wi3", EventType.WORK_CLAIMED, now - 4 * oneHour),
            createEventEntity("e6", "wi3", EventType.WORK_READY_FOR_QC, now - threeHours)
        )
        whenever(eventDao.getByWorkItemIds(listOf("wi1", "wi2", "wi3")))
            .thenReturn(allEvents)

        // When: threshold = 1 hour
        val bottlenecks = useCase(thresholdMs = oneHour)

        // Then: only wi2 and wi3 should be included (waiting >= 1 hour)
        assertEquals("Expected 2 bottlenecks above 1 hour threshold", 2, bottlenecks.size)
        assertTrue(bottlenecks.any { it.workItemId == "wi2" })
        assertTrue(bottlenecks.any { it.workItemId == "wi3" })
        assertFalse(bottlenecks.any { it.workItemId == "wi1" })
    }

    @Test
    fun `getQcBottleneck sorts by wait time descending`() = runTest {
        // Given: 3 items with different wait times
        val now = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000L
        val twoHours = 2 * oneHour
        val threeHours = 3 * oneHour

        val workItems = listOf(
            createWorkItemEntity("wi1"),
            createWorkItemEntity("wi2"),
            createWorkItemEntity("wi3")
        )

        whenever(workItemDao.observeAll()).thenReturn(flowOf(workItems))

        // Mock batch query with all events for all work items
        val allEvents = listOf(
            // wi1: waiting 1 hour
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - twoHours),
            createEventEntity("e2", "wi1", EventType.WORK_READY_FOR_QC, now - oneHour),
            // wi2: waiting 3 hours (longest wait)
            createEventEntity("e3", "wi2", EventType.WORK_CLAIMED, now - 4 * oneHour),
            createEventEntity("e4", "wi2", EventType.WORK_READY_FOR_QC, now - threeHours),
            // wi3: waiting 2 hours
            createEventEntity("e5", "wi3", EventType.WORK_CLAIMED, now - threeHours),
            createEventEntity("e6", "wi3", EventType.WORK_READY_FOR_QC, now - twoHours)
        )
        whenever(eventDao.getByWorkItemIds(listOf("wi1", "wi2", "wi3")))
            .thenReturn(allEvents)

        // When
        val bottlenecks = useCase(thresholdMs = 0L)

        // Then: should be sorted by wait time descending (wi2, wi3, wi1)
        assertEquals(3, bottlenecks.size)
        assertEquals("wi2", bottlenecks[0].workItemId)
        assertEquals("wi3", bottlenecks[1].workItemId)
        assertEquals("wi1", bottlenecks[2].workItemId)

        // Verify wait times are correct
        assertTrue("wi2 should have longest wait", bottlenecks[0].waitTimeMs > bottlenecks[1].waitTimeMs)
        assertTrue("wi3 should have longer wait than wi1", bottlenecks[1].waitTimeMs > bottlenecks[2].waitTimeMs)
    }

    @Test
    fun `getQcBottleneck includes assignee name when available`() = runTest {
        // Given: work item with assignee
        val now = System.currentTimeMillis()
        val workItems = listOf(createWorkItemEntity("wi1"))

        whenever(workItemDao.observeAll()).thenReturn(flowOf(workItems))

        // Mock batch query with events showing assigned user
        val allEvents = listOf(
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - 7200000, actorId = "user1"),
            createEventEntity("e2", "wi1", EventType.WORK_READY_FOR_QC, now - 3600000, actorId = "user1")
        )
        whenever(eventDao.getByWorkItemIds(listOf("wi1")))
            .thenReturn(allEvents)

        // Mock user dao to return user name
        whenever(userDao.getUserById("user1")).thenReturn(
            UserEntity(
                id = "user1",
                displayName = "John Doe",
                role = Role.ASSEMBLER.name,
                isActive = true
            )
        )

        // When
        val bottlenecks = useCase(thresholdMs = 0L)

        // Then
        assertEquals(1, bottlenecks.size)
        assertEquals("user1", bottlenecks[0].assigneeId)
        assertEquals("John Doe", bottlenecks[0].assigneeName)
    }

    @Test
    fun `getQcBottleneck with zero threshold includes all READY_FOR_QC items`() = runTest {
        // Given: items with minimal wait time
        val now = System.currentTimeMillis()
        val workItems = listOf(
            createWorkItemEntity("wi1"),
            createWorkItemEntity("wi2")
        )

        whenever(workItemDao.observeAll()).thenReturn(flowOf(workItems))

        // Mock batch query with events showing items just marked ready
        val allEvents = listOf(
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - 60000),
            createEventEntity("e2", "wi1", EventType.WORK_READY_FOR_QC, now - 1000), // 1 second ago
            createEventEntity("e3", "wi2", EventType.WORK_CLAIMED, now - 120000),
            createEventEntity("e4", "wi2", EventType.WORK_READY_FOR_QC, now - 2000) // 2 seconds ago
        )
        whenever(eventDao.getByWorkItemIds(listOf("wi1", "wi2")))
            .thenReturn(allEvents)

        // When: threshold = 0 (show all)
        val bottlenecks = useCase(thresholdMs = 0L)

        // Then: all items should be included
        assertEquals(2, bottlenecks.size)
    }

    @Test
    fun `getQcBottleneck computes correct wait time and status`() = runTest {
        // Given: work item waiting for QC
        val now = System.currentTimeMillis()
        val twoHours = 2 * 60 * 60 * 1000L
        val workItems = listOf(createWorkItemEntity("wi1"))

        whenever(workItemDao.observeAll()).thenReturn(flowOf(workItems))

        // Mock batch query with events
        val readyTimestamp = now - twoHours
        val allEvents = listOf(
            createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - 3 * 60 * 60 * 1000L),
            createEventEntity("e2", "wi1", EventType.WORK_READY_FOR_QC, readyTimestamp)
        )
        whenever(eventDao.getByWorkItemIds(listOf("wi1")))
            .thenReturn(allEvents)

        // When
        val bottlenecks = useCase(thresholdMs = 0L)

        // Then
        assertEquals(1, bottlenecks.size)
        assertEquals(WorkStatus.READY_FOR_QC, bottlenecks[0].status)
        assertEquals(readyTimestamp, bottlenecks[0].readyForQcSince)

        // Wait time should be approximately 2 hours
        val tolerance = 5000L // 5 seconds tolerance
        assertTrue(
            "Expected wait time around $twoHours but got ${bottlenecks[0].waitTimeMs}",
            Math.abs(bottlenecks[0].waitTimeMs - twoHours) < tolerance
        )
    }

    private fun createWorkItemEntity(id: String) = WorkItemEntity(
        id = id,
        projectId = "project1",
        type = "NODE",
        code = "CODE-$id",
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
