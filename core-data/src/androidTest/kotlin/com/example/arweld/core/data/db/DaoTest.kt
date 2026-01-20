package com.example.arweld.core.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.SyncQueueDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.db.entity.EventEntity
import com.example.arweld.core.data.db.entity.SyncQueueEntity
import com.example.arweld.core.data.db.entity.WorkItemEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoTest {

    private lateinit var database: AppDatabase
    private lateinit var workItemDao: WorkItemDao
    private lateinit var eventDao: EventDao
    private lateinit var syncQueueDao: SyncQueueDao

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
        syncQueueDao = database.syncQueueDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun workItemDao_canInsertAndFindByIdOrCode() = runBlocking {
        val entity = WorkItemEntity(
            id = "w1",
            projectId = "p1",
            zoneId = "z1",
            type = "PART",
            code = "CODE-123",
            description = "Test work item",
            nodeId = null,
            createdAt = 100L,
        )

        workItemDao.insertAll(listOf(entity))

        assertEquals(entity, workItemDao.getById("w1"))
        assertEquals(entity, workItemDao.getByCode("CODE-123"))
    }

    @Test
    fun eventDao_returnsOrderedResults() = runBlocking {
        val events = listOf(
            EventEntity(
                id = "e1",
                workItemId = "w1",
                type = "WORK_STARTED",
                timestamp = 1_000L,
                actorId = "userA",
                actorRole = "ASSEMBLER",
                deviceId = "device1",
                payloadJson = null,
            ),
            EventEntity(
                id = "e2",
                workItemId = "w1",
                type = "QC_STARTED",
                timestamp = 2_000L,
                actorId = "userB",
                actorRole = "QC",
                deviceId = "device1",
                payloadJson = null,
            ),
            EventEntity(
                id = "e3",
                workItemId = "w2",
                type = "WORK_STARTED",
                timestamp = 1_500L,
                actorId = "userA",
                actorRole = "ASSEMBLER",
                deviceId = "device1",
                payloadJson = null,
            ),
        )

        eventDao.insertAll(events)

        val workItemEvents = eventDao.getByWorkItemId("w1")
        assertEquals(listOf(events[0], events[1]), workItemEvents)

        val userEvents = eventDao.getLastEventsByUser("userA")
        assertEquals(listOf(events[2], events[0]), userEvents)
    }

    @Test
    fun syncQueueDao_returnsPendingOrderedByCreatedAt() = runBlocking {
        val pendingA = SyncQueueEntity(
            id = "q1",
            type = "EVENT",
            eventType = "WORK_STARTED",
            workItemId = "w1",
            payloadJson = "{}",
            fileUri = "",
            mimeType = "",
            sizeBytes = 0L,
            status = "PENDING",
            createdAt = 50L,
        )
        val pendingB = SyncQueueEntity(
            id = "q2",
            type = "EVENT",
            eventType = "QC_STARTED",
            workItemId = "w1",
            payloadJson = "{}",
            fileUri = "",
            mimeType = "",
            sizeBytes = 0L,
            status = "PENDING",
            createdAt = 75L,
        )
        val error = SyncQueueEntity(
            id = "q3",
            type = "EVENT",
            eventType = "WORK_READY_FOR_QC",
            workItemId = "w2",
            payloadJson = "{}",
            fileUri = "",
            mimeType = "",
            sizeBytes = 0L,
            status = "ERROR",
            createdAt = 10L,
        )

        syncQueueDao.insertAll(listOf(pendingB, pendingA, error))

        val pendingItems = syncQueueDao.getPending(limit = 1)
        assertEquals(1, pendingItems.size)
        assertEquals(pendingA, pendingItems.first())

        val allPending = syncQueueDao.getPending(limit = 5)
        assertEquals(listOf(pendingA, pendingB), allPending)
        assertTrue(allPending.none { it.status == "ERROR" })
    }
}
