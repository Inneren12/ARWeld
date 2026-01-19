package com.example.arweld.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.arweld.core.data.db.AppDatabase
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: EventRepositoryImpl

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

        repository = EventRepositoryImpl(
            database = database,
            eventDao = database.eventDao(),
            syncQueueDao = database.syncQueueDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun appendAndFetchEventsForWorkItem_preservesOrderAndFields() = runBlocking {
        val initial = Event(
            id = "e1",
            workItemId = "w1",
            type = EventType.WORK_STARTED,
            timestamp = 1_000L,
            actorId = "userA",
            actorRole = Role.ASSEMBLER,
            deviceId = "device1",
            payloadJson = "{\"note\":\"started\"}",
        )
        val qcStarted = Event(
            id = "e2",
            workItemId = "w1",
            type = EventType.QC_STARTED,
            timestamp = 2_000L,
            actorId = "userB",
            actorRole = Role.QC,
            deviceId = "device2",
            payloadJson = null,
        )
        val otherWorkItem = Event(
            id = "e3",
            workItemId = "w2",
            type = EventType.ISSUE_CREATED,
            timestamp = 500L,
            actorId = "userC",
            actorRole = Role.SUPERVISOR,
            deviceId = "device3",
            payloadJson = null,
        )

        repository.appendEvent(initial)
        repository.appendEvents(listOf(qcStarted, otherWorkItem))

        val eventsForW1 = repository.getEventsForWorkItem("w1")

        assertEquals(listOf(initial, qcStarted), eventsForW1)
    }
}
