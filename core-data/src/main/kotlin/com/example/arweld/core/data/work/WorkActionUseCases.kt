package com.example.arweld.core.data.work

import android.os.Build
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.work.usecase.ClaimWorkUseCase
import com.example.arweld.core.domain.work.usecase.MarkReadyForQcUseCase
import com.example.arweld.core.domain.work.usecase.StartWorkUseCase
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val UNKNOWN_DEVICE_ID = "unknown-device"

private fun resolveDeviceId(): String = Build.MODEL.ifBlank { UNKNOWN_DEVICE_ID }

private fun buildEvent(
    workItemId: String,
    type: EventType,
    userProvider: suspend () -> com.example.arweld.core.domain.model.User?,
    eventRepository: EventRepository,
) = suspend {
    val user = userProvider() ?: error("Not logged in")
    val event = Event(
        id = UUID.randomUUID().toString(),
        workItemId = workItemId,
        type = type,
        timestamp = System.currentTimeMillis(),
        actorId = user.id,
        actorRole = user.role,
        deviceId = resolveDeviceId(),
        payloadJson = null,
    )
    eventRepository.appendEvent(event)
}

@Singleton
class ClaimWorkUseCaseImpl @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
) : ClaimWorkUseCase {

    override suspend fun invoke(workItemId: String) {
        buildEvent(
            workItemId = workItemId,
            type = EventType.WORK_CLAIMED,
            userProvider = { authRepository.currentUser() },
            eventRepository = eventRepository,
        ).invoke()
    }
}

@Singleton
class StartWorkUseCaseImpl @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
) : StartWorkUseCase {

    override suspend fun invoke(workItemId: String) {
        buildEvent(
            workItemId = workItemId,
            type = EventType.WORK_STARTED,
            userProvider = { authRepository.currentUser() },
            eventRepository = eventRepository,
        ).invoke()
    }
}

@Singleton
class MarkReadyForQcUseCaseImpl @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
) : MarkReadyForQcUseCase {

    override suspend fun invoke(workItemId: String) {
        buildEvent(
            workItemId = workItemId,
            type = EventType.WORK_READY_FOR_QC,
            userProvider = { authRepository.currentUser() },
            eventRepository = eventRepository,
        ).invoke()
    }
}
