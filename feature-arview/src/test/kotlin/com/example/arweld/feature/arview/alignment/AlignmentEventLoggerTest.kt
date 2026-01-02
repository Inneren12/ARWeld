package com.example.arweld.feature.arview.alignment

import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.User
import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AlignmentEventLoggerTest {

    @Test
    fun `logs alignment event on success`() = runTest {
        val eventRepository = FakeEventRepository()
        val authRepository = FakeAuthRepository()
        val logger = AlignmentEventLogger(eventRepository, authRepository)

        logger.logManualAlignment(
            workItemId = "work-123",
            numPoints = 3,
            transform = Pose3D(Vector3(0.0, 0.0, 0.0), Quaternion.Identity),
        )

        val appended = eventRepository.appendedEvents.single()
        assertEquals(EventType.AR_ALIGNMENT_SET, appended.type)
    }
}

private class FakeEventRepository : EventRepository {
    val appendedEvents = mutableListOf<Event>()
    override suspend fun appendEvent(event: Event) {
        appendedEvents.add(event)
    }

    override suspend fun appendEvents(events: List<Event>) {
        appendedEvents.addAll(events)
    }

    override suspend fun getEventsForWorkItem(workItemId: String): List<Event> = emptyList()
}

private class FakeAuthRepository : AuthRepository {
    private val user = User(id = "user-1", name = "Test User", role = Role.ASSEMBLER)
    override suspend fun loginMock(role: Role): User = user
    override suspend fun availableUsers(): List<User> = listOf(user)
    override suspend fun loginWithUserId(userId: String): User = user
    override suspend fun currentUser(): User = user
    override suspend fun logout() {}
}
