package com.example.arweld.core.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.arweld.core.data.db.dao.SyncQueueDao
import com.example.arweld.core.data.db.entity.SyncQueueEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SyncQueueDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var syncQueueDao: SyncQueueDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
        syncQueueDao = database.syncQueueDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun enqueueEvent_tracksPendingAndErrorCounts() = runBlocking {
        val pending = SyncQueueEntity(
            id = "q1",
            type = "EVENT",
            eventType = "WORK_STARTED",
            workItemId = "w1",
            payloadJson = "{}",
            status = "PENDING",
            createdAt = 10L,
        )
        val error = SyncQueueEntity(
            id = "q2",
            type = "EVENT",
            eventType = "QC_FAILED_REWORK",
            workItemId = "w1",
            payloadJson = "{}",
            status = "ERROR",
            createdAt = 20L,
        )

        syncQueueDao.enqueueEvent(pending)
        syncQueueDao.enqueueEvent(error)

        assertEquals(1, syncQueueDao.getPendingCount())
        assertEquals(1, syncQueueDao.getErrorCount())
        assertEquals(listOf(pending), syncQueueDao.getPending(limit = 5))
        assertEquals(listOf(error), syncQueueDao.getErrors(limit = 5))
    }
}
