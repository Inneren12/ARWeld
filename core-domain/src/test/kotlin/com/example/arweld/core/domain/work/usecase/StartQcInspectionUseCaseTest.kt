package com.example.arweld.core.domain.work.usecase

import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.User
import com.example.arweld.core.domain.system.DeviceInfoProvider
import com.example.arweld.core.domain.system.TimeProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class StartQcInspectionUseCaseTest {

    private val qcUser = User(
        id = "qc-123",
        username = "qc.user",
        displayName = "QC User",
        role = Role.QC,
    )

    private val timeProvider = TimeProvider { 42_000L }
    private val deviceInfoProvider = DeviceInfoProvider { "device-test" }

    @Test
    fun `start inspection appends QC_STARTED event`() = runBlocking {
        val eventRepository = InMemoryEventRepository()
        val authRepository = StubAuthRepository(qcUser)
        val useCase = StartQcInspectionUseCase(
            eventRepository = eventRepository,
            authRepository = authRepository,
            timeProvider = timeProvider,
            deviceInfoProvider = deviceInfoProvider,
        )

        useCase("work-123")

        val events = eventRepository.getEventsForWorkItem("work-123")
        assertEquals(1, events.size)
        val event = events.first()
        assertEquals(EventType.QC_STARTED, event.type)
        assertEquals("work-123", event.workItemId)
        assertEquals(qcUser.id, event.actorId)
        assertEquals(Role.QC, event.actorRole)
        assertEquals(timeProvider.nowMillis(), event.timestamp)
        assertEquals(deviceInfoProvider.deviceId, event.deviceId)
    }
}

private class InMemoryEventRepository : EventRepository {
    private val events = mutableListOf<Event>()

    override suspend fun appendEvent(event: Event) {
        events.add(event)
    }

    override suspend fun appendEvents(events: List<Event>) {
        this.events.addAll(events)
    }

    override suspend fun getEventsForWorkItem(workItemId: String): List<Event> {
        return events
            .filter { it.workItemId == workItemId }
            .sortedWith(compareBy<Event> { it.timestamp }.thenBy { it.id })
    }
}

private class StubAuthRepository(private val user: User?) : AuthRepository {
    override suspend fun loginMock(role: Role): User {
        throw UnsupportedOperationException("Not used in test")
    }

    override suspend fun availableUsers(): List<User> = emptyList()

    override suspend fun loginWithUserId(userId: String): User {
        throw UnsupportedOperationException("Not used in test")
    }

    override suspend fun currentUser(): User? = user

    override suspend fun logout() { /* no-op */ }
}
