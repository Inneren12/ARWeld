package com.example.arweld.core.data.work

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.arweld.core.data.db.AppDatabase
import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.db.entity.EventEntity
import com.example.arweld.core.data.db.entity.WorkItemEntity
import com.example.arweld.core.domain.state.WorkStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class WorkRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var workItemDao: WorkItemDao
    private lateinit var eventDao: EventDao
    private lateinit var repository: WorkRepositoryImpl

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

        workItemDao = database.workItemDao()
        eventDao = database.eventDao()
        repository = WorkRepositoryImpl(workItemDao, eventDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getWorkItemState_derivesStateFromEvents() = runBlocking {
        val workItemId = "work-1"
        val entity = WorkItemEntity(
            id = workItemId,
            projectId = "project-1",
            zoneId = null,
            type = "PART",
            code = "CODE-123",
            description = "Test item",
            nodeId = null,
            createdAt = 0L,
        )
        workItemDao.insert(entity)

        val events = listOf(
            EventEntity(
                id = "e1",
                workItemId = workItemId,
                type = "WORK_CLAIMED",
                timestamp = 1L,
                actorId = "assembler-1",
                actorRole = "ASSEMBLER",
                deviceId = "device-1",
                payloadJson = null,
            ),
            EventEntity(
                id = "e2",
                workItemId = workItemId,
                type = "WORK_READY_FOR_QC",
                timestamp = 2L,
                actorId = "assembler-1",
                actorRole = "ASSEMBLER",
                deviceId = "device-1",
                payloadJson = null,
            ),
            EventEntity(
                id = "e3",
                workItemId = workItemId,
                type = "QC_STARTED",
                timestamp = 3L,
                actorId = "qc-1",
                actorRole = "QC",
                deviceId = "device-1",
                payloadJson = null,
            ),
            EventEntity(
                id = "e4",
                workItemId = workItemId,
                type = "QC_PASSED",
                timestamp = 4L,
                actorId = "qc-1",
                actorRole = "QC",
                deviceId = "device-1",
                payloadJson = null,
            ),
        )
        eventDao.insertAll(events)

        val state = repository.getWorkItemState(workItemId)

        assertEquals(WorkStatus.APPROVED, state.status)
        assertNotNull(state.lastEvent)
        assertEquals("e4", state.lastEvent?.id)
    }

    @Test
    fun getWorkItemState_setsReadyForQcSinceWhenReadyOrInProgress() = runBlocking {
        val workItemId = "work-ready"
        val entity = WorkItemEntity(
            id = workItemId,
            projectId = "project-1",
            zoneId = null,
            type = "PART",
            code = "CODE-789",
            description = "Ready item",
            nodeId = null,
            createdAt = 0L,
        )
        workItemDao.insert(entity)

        val events = listOf(
            EventEntity(
                id = "e1",
                workItemId = workItemId,
                type = "WORK_READY_FOR_QC",
                timestamp = 5L,
                actorId = "assembler-1",
                actorRole = "ASSEMBLER",
                deviceId = "device-1",
                payloadJson = null,
            ),
        )
        eventDao.insertAll(events)

        val readyState = repository.getWorkItemState(workItemId)
        assertEquals(WorkStatus.READY_FOR_QC, readyState.status)
        assertEquals(5L, readyState.readyForQcSince)

        val inProgressEvents = events + EventEntity(
            id = "e2",
            workItemId = workItemId,
            type = "QC_STARTED",
            timestamp = 10L,
            actorId = "qc-1",
            actorRole = "QC",
            deviceId = "device-1",
            payloadJson = null,
        )
        eventDao.insertAll(listOf(inProgressEvents.last()))

        val inProgressState = repository.getWorkItemState(workItemId)
        assertEquals(WorkStatus.QC_IN_PROGRESS, inProgressState.status)
        assertEquals(5L, inProgressState.readyForQcSince)
    }

    @Test
    fun getWorkItemById_returnsPersistedItem() = runBlocking {
        val workItemId = "work-2"
        val entity = WorkItemEntity(
            id = workItemId,
            projectId = "project-2",
            zoneId = "zone-1",
            type = "NODE",
            code = "CODE-456",
            description = "Another item",
            nodeId = "node-123",
            createdAt = 123L,
        )
        workItemDao.insert(entity)

        val workItem = repository.getWorkItemById(workItemId)

        assertNotNull(workItem)
        assertEquals(workItemId, workItem?.id)
        assertEquals("CODE-456", workItem?.code)
    }
}
