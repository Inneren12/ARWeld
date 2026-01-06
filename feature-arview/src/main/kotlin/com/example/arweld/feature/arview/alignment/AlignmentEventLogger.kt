package com.example.arweld.feature.arview.alignment

import android.os.Build
import android.util.Log
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.spatial.Pose3D
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val UNKNOWN_DEVICE_ID = "unknown-device"

@Singleton
class AlignmentEventLogger @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
) {
    private val json: Json = Json { encodeDefaults = false }

    suspend fun logMarkerAlignment(workItemId: String?, markerId: String, transform: Pose3D) {
        logAlignment(
            workItemId = workItemId,
            method = "marker",
            markerIds = listOf(markerId),
            numPoints = null,
            transform = transform,
        )
    }

    suspend fun logManualAlignment(workItemId: String?, numPoints: Int, transform: Pose3D) {
        logAlignment(
            workItemId = workItemId,
            method = "manual",
            markerIds = emptyList(),
            numPoints = numPoints,
            transform = transform,
        )
    }

    private suspend fun logAlignment(
        workItemId: String?,
        method: String,
        markerIds: List<String>,
        numPoints: Int?,
        transform: Pose3D,
    ) {
        if (workItemId.isNullOrBlank()) {
            Log.w(TAG, "Skipping AR_ALIGNMENT_SET: missing workItemId")
            return
        }

        val user = authRepository.currentUser()
        if (user == null) {
            Log.w(TAG, "Skipping AR_ALIGNMENT_SET: no active user")
            return
        }

        val timestamp = System.currentTimeMillis()
        val payload = ArAlignmentPayload(
            method = method,
            markerIds = markerIds,
            numPoints = numPoints,
            alignmentScore = null,
            timestamp = timestamp,
            worldPosition = transform.toPayloadPosition(),
            worldRotationEuler = transform.toPayloadEuler(),
        )

        val event = Event(
            id = UUID.randomUUID().toString(),
            workItemId = workItemId,
            type = EventType.AR_ALIGNMENT_SET,
            timestamp = timestamp,
            actorId = user.id,
            actorRole = user.role,
            deviceId = resolveDeviceId(),
            payloadJson = json.encodeToString(payload),
        )

        runCatching { eventRepository.appendEvent(event) }
            .onFailure { error -> Log.w(TAG, "Failed to append alignment event", error) }
    }

    private fun resolveDeviceId(): String = Build.MODEL.ifBlank { UNKNOWN_DEVICE_ID }

    companion object {
        private const val TAG = "AlignmentEventLogger"
    }
}
