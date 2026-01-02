package com.example.arweld.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.arweld.core.data.db.AppDatabase
import com.example.arweld.core.data.file.computeSha256
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.User
import com.example.arweld.core.domain.system.DeviceInfoProvider
import com.example.arweld.core.domain.system.TimeProvider
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

        eventRepository = EventRepositoryImpl(database.eventDao())
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
        assertNotNull(database.evidenceDao().getById(evidence.id))

        val events = eventRepository.getEventsForWorkItem(WORK_ITEM_ID)
        assertEquals(1, events.size)
        val capturedEvent = events.first()
        assertEquals(EventType.EVIDENCE_CAPTURED, capturedEvent.type)
        assertEquals(FIXED_TIME, capturedEvent.timestamp)
        assertTrue(requireNotNull(capturedEvent.payloadJson).contains(evidence.id))
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
