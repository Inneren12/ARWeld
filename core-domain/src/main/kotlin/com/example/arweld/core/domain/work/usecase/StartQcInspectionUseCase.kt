package com.example.arweld.core.domain.work.usecase

import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.system.DeviceInfoProvider
import com.example.arweld.core.domain.system.TimeProvider
import java.util.UUID

/**
 * Begins a QC inspection for the given work item by emitting a QC_STARTED event.
 */
class StartQcInspectionUseCase(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
    private val timeProvider: TimeProvider,
    private val deviceInfoProvider: DeviceInfoProvider,
) {

    suspend operator fun invoke(workItemId: String) {
        val inspector = authRepository.currentUser() ?: error("User must be logged in")
        require(inspector.role == Role.QC) { "Only QC inspectors can start QC" }

        val event = Event(
            id = UUID.randomUUID().toString(),
            workItemId = workItemId,
            type = EventType.QC_STARTED,
            timestamp = timeProvider.nowMillis(),
            actorId = inspector.id,
            actorRole = inspector.role,
            deviceId = deviceInfoProvider.deviceId,
            payloadJson = null,
        )

        eventRepository.appendEvent(event)
    }
}
