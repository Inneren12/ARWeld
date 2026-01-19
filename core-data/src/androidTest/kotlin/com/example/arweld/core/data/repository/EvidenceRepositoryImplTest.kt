package com.example.arweld.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.core.net.toUri
import com.example.arweld.core.data.db.AppDatabase
import com.example.arweld.core.data.file.computeSha256
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.evidence.ArScreenshotMeta
import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.User
import com.example.arweld.core.domain.system.DeviceInfoProvider
import com.example.arweld.core.domain.system.TimeProvider
import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EvidenceRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var eventRepository: EventRepository
    private lateinit var evidenceRepository: EvidenceRepositoryImpl

    private val timeProvider = TimeProvider { FIXED_TIME }
    private val deviceInfoProvider = DeviceInfoProvider { "device-test" }
    private val authRepository: AuthRepository = FakeAuthRepository()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        eventRepository = EventRepositoryImpl(
            database = database,
            eventDao = database.eventDao(),
            syncQueueDao = database.syncQueueDao(),
        )
        evidenceRepository = EvidenceRepositoryImpl(
            evidenceDao = database.evidenceDao(),
            eventRepository = eventRepository,
            authRepository = authRepository,
            deviceInfoProvider = deviceInfoProvider,
            timeProvider = timeProvider,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun savePhoto_storesHashAndAppendsCapturedEvent() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val photoFile = File(context.cacheDir, "evidence_photo.jpg").apply {
            writeText("photo-bytes")
        }

        val evidence = evidenceRepository.savePhoto(
            workItemId = WORK_ITEM_ID,
            eventId = QC_EVENT_ID,
            file = photoFile,
        )

        assertEquals(WORK_ITEM_ID, evidence.workItemId)
        assertEquals(QC_EVENT_ID, evidence.eventId)
        assertEquals(photoFile.length(), evidence.sizeBytes)
        assertEquals(computeSha256(photoFile), evidence.sha256)
        val stored = database.evidenceDao().getById(evidence.id)
        assertNotNull(stored)
        assertEquals(WORK_ITEM_ID, stored?.workItemId)
        assertEquals(photoFile.length(), stored?.sizeBytes)

        val events = eventRepository.getEventsForWorkItem(WORK_ITEM_ID)
        assertEquals(1, events.size)
        val capturedEvent = events.first()
        assertEquals(EventType.EVIDENCE_CAPTURED, capturedEvent.type)
        assertEquals(FIXED_TIME, capturedEvent.timestamp)
        assertTrue(requireNotNull(capturedEvent.payloadJson).contains(evidence.id))
    }

    @Test
    fun saveArScreenshot_hashesFileAndAppendsMetaToEvent() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val screenshotFile = File(context.cacheDir, "ar_screenshot.png").apply {
            writeText("ar-screenshot-bytes")
        }
        val meta = ArScreenshotMeta(
            markerIds = listOf("1", "2", "3"),
            trackingState = "TRACKING",
            alignmentQualityScore = 0.9f,
            distanceToMarker = null,
            timestamp = FIXED_TIME,
        )

        val evidence = evidenceRepository.saveArScreenshot(
            workItemId = WORK_ITEM_ID,
            eventId = QC_EVENT_ID,
            uri = screenshotFile.toUri(),
            meta = meta,
        )

        assertEquals(WORK_ITEM_ID, evidence.workItemId)
        assertEquals(QC_EVENT_ID, evidence.eventId)
        assertEquals(EvidenceKind.AR_SCREENSHOT, evidence.kind)
        assertEquals(screenshotFile.length(), evidence.sizeBytes)
        assertEquals(computeSha256(screenshotFile), evidence.sha256)
        assertEquals(Json.encodeToString(meta), evidence.metaJson)
        assertNotNull(database.evidenceDao().getById(evidence.id))

        val events = eventRepository.getEventsForWorkItem(WORK_ITEM_ID)
        assertEquals(1, events.size)
        val capturedEvent = events.first()
        assertEquals(EventType.EVIDENCE_CAPTURED, capturedEvent.type)
        val payload = requireNotNull(capturedEvent.payloadJson)
        assertTrue(payload.contains("\"AR_SCREENSHOT\""))
        assertTrue(payload.contains("trackingQuality"))
    }

    private companion object {
        const val WORK_ITEM_ID = "work-1"
        const val QC_EVENT_ID = "qc-start-event"
        const val FIXED_TIME = 1_000L
    }
}

private class FakeAuthRepository : AuthRepository {
    private val user = User(
        id = "qc-user",
        username = "qc@example.com",
        displayName = "QC Inspector",
        role = Role.QC,
    )

    override suspend fun loginMock(role: Role): User = user.copy(role = role)

    override suspend fun availableUsers(): List<User> = listOf(user)

    override suspend fun loginWithUserId(userId: String): User = user

    override suspend fun currentUser(): User? = user

    override suspend fun logout() {}
}
