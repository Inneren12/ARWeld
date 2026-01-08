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
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AlignmentEventLoggerTest {

    @Test
    fun `logs alignment event on success`() = runTest {
        val eventRepository = mockk<EventRepository>(relaxed = true)
        val authRepository = mockk<AuthRepository>(relaxed = true)
        val user = mockk<User>(relaxed = true)

        // AlignmentEventLogger обычно берет текущего юзера отсюда
        coEvery { authRepository.currentUser() } returns user

        // На всякий случай фиксируем нужные поля (если логгер их использует)
        every { user.id } returns "user-1"
        every { user.role } returns Role.ASSEMBLER
        // если эти поля есть в модели:
        every { user.username } returns "test-user"
        every { user.displayName } returns "Test User"
        every { user.deviceId } returns "test-device-1"

        val eventSlot = slot<Event>()
        val eventsSlot = slot<List<Event>>()
        coEvery { eventRepository.appendEvent(capture(eventSlot)) } returns Unit
        coEvery { eventRepository.appendEvents(capture(eventsSlot)) } returns Unit

        val logger = AlignmentEventLogger(eventRepository, authRepository)

        logger.logManualAlignment(
            workItemId = "work-123",
            numPoints = 3,
            transform = Pose3D(Vector3(0.0, 0.0, 0.0), Quaternion.Identity),
        )

        val appended: Event = when {
            eventSlot.isCaptured -> eventSlot.captured
            eventsSlot.isCaptured -> eventsSlot.captured.first()
            else -> error("No event appended")
        }
        assertEquals(EventType.AR_ALIGNMENT_SET, appended.type)
    }
}

