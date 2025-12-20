package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.db.entity.EventEntity
import com.example.arweld.core.data.db.entity.WorkItemEntity
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.state.WorkStatus
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for CalculateKpisUseCase.
 * Verifies KPI computation from event log without database.
 */
class CalculateKpisUseCaseTest {

    private lateinit var workItemDao: WorkItemDao
    private lateinit var eventDao: EventDao
    private lateinit var useCase: CalculateKpisUseCase

    @Before
    fun setup() {
        workItemDao = mock()
        eventDao = mock()
        useCase = CalculateKpisUseCase(workItemDao, eventDao)
    }

    @Test
    fun `calculateKpis with empty event log returns zero counts`() = runTest {
        // Given: no work items
        whenever(workItemDao.observeAll()).thenReturn(flowOf(emptyList()))

        // When
        val kpis = useCase()

        // Then
        assertEquals(0, kpis.totalWorkItems)
        assertEquals(0, kpis.inProgress)
        assertEquals(0, kpis.readyForQc)
        assertEquals(0, kpis.qcInProgress)
        assertEquals(0, kpis.approved)
        assertEquals(0, kpis.rework)
        assertEquals(0L, kpis.avgQcWaitTimeMs)
        assertEquals(0f, kpis.qcPassRate, 0.001f)
    }

    @Test
    fun `calculateKpis counts work items by derived status`() = runTest {
        // Given: 5 work items with events
        val now = System.currentTimeMillis()
        val workItems = listOf(
            createWorkItemEntity("wi1"),
            createWorkItemEntity("wi2"),
            createWorkItemEntity("wi3"),
            createWorkItemEntity("wi4"),
            createWorkItemEntity("wi5")
        )

        whenever(workItemDao.observeAll()).thenReturn(flowOf(workItems))

        // wi1: IN_PROGRESS (WORK_CLAIMED)
        whenever(eventDao.getByWorkItemId("wi1")).thenReturn(
            listOf(createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - 3600000))
        )

        // wi2: READY_FOR_QC (WORK_READY_FOR_QC)
        whenever(eventDao.getByWorkItemId("wi2")).thenReturn(
            listOf(
                createEventEntity("e2", "wi2", EventType.WORK_CLAIMED, now - 7200000),
                createEventEntity("e3", "wi2", EventType.WORK_READY_FOR_QC, now - 1800000)
            )
        )

        // wi3: QC_IN_PROGRESS (QC_STARTED)
        whenever(eventDao.getByWorkItemId("wi3")).thenReturn(
            listOf(
                createEventEntity("e4", "wi3", EventType.WORK_CLAIMED, now - 9000000),
                createEventEntity("e5", "wi3", EventType.WORK_READY_FOR_QC, now - 5400000),
                createEventEntity("e6", "wi3", EventType.QC_STARTED, now - 1200000)
            )
        )

        // wi4: APPROVED (QC_PASSED)
        whenever(eventDao.getByWorkItemId("wi4")).thenReturn(
            listOf(
                createEventEntity("e7", "wi4", EventType.WORK_CLAIMED, now - 10800000),
                createEventEntity("e8", "wi4", EventType.WORK_READY_FOR_QC, now - 7200000),
                createEventEntity("e9", "wi4", EventType.QC_STARTED, now - 3600000),
                createEventEntity("e10", "wi4", EventType.QC_PASSED, now - 1800000)
            )
        )

        // wi5: REWORK_REQUIRED (QC_FAILED_REWORK)
        whenever(eventDao.getByWorkItemId("wi5")).thenReturn(
            listOf(
                createEventEntity("e11", "wi5", EventType.WORK_CLAIMED, now - 12600000),
                createEventEntity("e12", "wi5", EventType.WORK_READY_FOR_QC, now - 9000000),
                createEventEntity("e13", "wi5", EventType.QC_STARTED, now - 5400000),
                createEventEntity("e14", "wi5", EventType.QC_FAILED_REWORK, now - 3600000)
            )
        )

        // When
        val kpis = useCase()

        // Then
        assertEquals(5, kpis.totalWorkItems)
        assertEquals(1, kpis.inProgress)
        assertEquals(1, kpis.readyForQc)
        assertEquals(1, kpis.qcInProgress)
        assertEquals(1, kpis.approved)
        assertEquals(1, kpis.rework)
    }

    @Test
    fun `calculateKpis computes correct QC pass rate`() = runTest {
        // Given: 10 completed QC items (7 passed, 3 failed)
        val now = System.currentTimeMillis()
        val workItems = (1..10).map { createWorkItemEntity("wi$it") }

        whenever(workItemDao.observeAll()).thenReturn(flowOf(workItems))

        // 7 passed
        (1..7).forEach { i ->
            whenever(eventDao.getByWorkItemId("wi$i")).thenReturn(
                listOf(
                    createEventEntity("e${i}a", "wi$i", EventType.WORK_CLAIMED, now - 7200000),
                    createEventEntity("e${i}b", "wi$i", EventType.WORK_READY_FOR_QC, now - 3600000),
                    createEventEntity("e${i}c", "wi$i", EventType.QC_STARTED, now - 1800000),
                    createEventEntity("e${i}d", "wi$i", EventType.QC_PASSED, now - 900000)
                )
            )
        }

        // 3 failed
        (8..10).forEach { i ->
            whenever(eventDao.getByWorkItemId("wi$i")).thenReturn(
                listOf(
                    createEventEntity("e${i}a", "wi$i", EventType.WORK_CLAIMED, now - 7200000),
                    createEventEntity("e${i}b", "wi$i", EventType.WORK_READY_FOR_QC, now - 3600000),
                    createEventEntity("e${i}c", "wi$i", EventType.QC_STARTED, now - 1800000),
                    createEventEntity("e${i}d", "wi$i", EventType.QC_FAILED_REWORK, now - 900000)
                )
            )
        }

        // When
        val kpis = useCase()

        // Then: QC pass rate = 7 / 10 = 0.7
        assertEquals(0.7f, kpis.qcPassRate, 0.001f)
        assertEquals(7, kpis.approved)
        assertEquals(3, kpis.rework)
    }

    @Test
    fun `calculateKpis computes average QC wait time`() = runTest {
        // Given: 3 work items waiting for QC
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

        // wi1: waiting 1 hour
        whenever(eventDao.getByWorkItemId("wi1")).thenReturn(
            listOf(
                createEventEntity("e1", "wi1", EventType.WORK_CLAIMED, now - twoHours),
                createEventEntity("e2", "wi1", EventType.WORK_READY_FOR_QC, now - oneHour)
            )
        )

        // wi2: waiting 2 hours
        whenever(eventDao.getByWorkItemId("wi2")).thenReturn(
            listOf(
                createEventEntity("e3", "wi2", EventType.WORK_CLAIMED, now - threeHours),
                createEventEntity("e4", "wi2", EventType.WORK_READY_FOR_QC, now - twoHours)
            )
        )

        // wi3: waiting 3 hours
        whenever(eventDao.getByWorkItemId("wi3")).thenReturn(
            listOf(
                createEventEntity("e5", "wi3", EventType.WORK_CLAIMED, now - 4 * oneHour),
                createEventEntity("e6", "wi3", EventType.WORK_READY_FOR_QC, now - threeHours)
            )
        )

        // When
        val kpis = useCase()

        // Then: average wait time = (1h + 2h + 3h) / 3 = 2h
        val expectedAvg = twoHours
        val tolerance = 5000L // 5 seconds tolerance
        assertTrue(
            "Expected avg QC wait time around $expectedAvg but got ${kpis.avgQcWaitTimeMs}",
            Math.abs(kpis.avgQcWaitTimeMs - expectedAvg) < tolerance
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
        timestamp: Long
    ) = EventEntity(
        id = id,
        workItemId = workItemId,
        type = type.name,
        timestamp = timestamp,
        actorId = "user1",
        actorRole = Role.ASSEMBLER.name,
        deviceId = "device1",
        payloadJson = null
    )
}
