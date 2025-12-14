package com.example.arweld.core.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.arweld.core.data.db.AppDatabase
import com.example.arweld.core.data.db.entity.EventEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDaoInstrumentedTest {

    private lateinit var db: AppDatabase
    private lateinit var eventDao: EventDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        eventDao = db.eventDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndReadEvent() = runBlocking {
        val entity = EventEntity(
            id = "e1",
            workItemId = "w1",
            type = "QC_PASSED",
            timestamp = 123L,
            actorId = "user-1",
            actorRole = "QC",
            deviceId = "device-1",
            payloadJson = "{\"test\":true}"
        )

        eventDao.insert(entity)

        val events = eventDao.getByWorkItemId("w1")

        assertEquals(1, events.size)
        val stored = events.first()
        assertEquals("e1", stored.id)
        assertEquals("w1", stored.workItemId)
        assertEquals("QC_PASSED", stored.type)
        assertEquals(123L, stored.timestamp)
        assertEquals("user-1", stored.actorId)
        assertEquals("QC", stored.actorRole)
        assertEquals("device-1", stored.deviceId)
        assertEquals("{\"test\":true}", stored.payloadJson)
    }
}
