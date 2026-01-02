package com.example.arweld.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.arweld.core.data.db.AppDatabase
import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.EvidenceDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.db.entity.EventEntity
import com.example.arweld.core.data.db.entity.EvidenceEntity
import com.example.arweld.core.data.db.entity.WorkItemEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseInstrumentedTest {

    private lateinit var database: AppDatabase
    private lateinit var workItemDao: WorkItemDao
    private lateinit var eventDao: EventDao
    private lateinit var evidenceDao: EvidenceDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        workItemDao = database.workItemDao()
        eventDao = database.eventDao()
        evidenceDao = database.evidenceDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndQueryEvidenceAcrossRelations() = runBlocking {
        val workItem = WorkItemEntity(
            id = "work-1",
            projectId = "project-1",
            zoneId = "zone-1",
            type = "PIPE",
            code = "W-001",
            description = "Weld intake pipe",
            nodeId = "node-123",
            createdAt = 1L,
        )
        workItemDao.insert(workItem)

        val event = EventEntity(
            id = "event-1",
            workItemId = workItem.id,
            type = "QC_STARTED",
            timestamp = 2L,
            actorId = "user-1",
            actorRole = "QC",
            deviceId = "device-1",
            payloadJson = null,
        )
        eventDao.insert(event)

        val evidence = EvidenceEntity(
            id = "evidence-1",
            eventId = event.id,
            kind = "PHOTO",
            uri = "file://evidence/photo.jpg",
            sha256 = "sha-256",
            metaJson = null,
            createdAt = 3L,
        )
        evidenceDao.insert(evidence)

        val eventsForWorkItem = eventDao.getByWorkItemId(workItem.id)
        assertEquals(1, eventsForWorkItem.size)
        val storedEvent = eventsForWorkItem.first()
        assertEquals(event.id, storedEvent.id)
        assertEquals(workItem.id, storedEvent.workItemId)

        val evidenceForEvent = evidenceDao.listByEvent(event.id)
        assertEquals(1, evidenceForEvent.size)
        assertEquals(evidence.id, evidenceForEvent.first().id)
        assertEquals(event.id, evidenceForEvent.first().eventId)

        val evidenceForWorkItem = evidenceDao.listByWorkItem(workItem.id)
        assertEquals(1, evidenceForWorkItem.size)
        val storedEvidence = evidenceForWorkItem.first()
        assertNotNull(evidenceDao.getById(storedEvidence.id))
        assertEquals(evidence.id, storedEvidence.id)
        assertEquals(event.id, storedEvidence.eventId)
    }
}
