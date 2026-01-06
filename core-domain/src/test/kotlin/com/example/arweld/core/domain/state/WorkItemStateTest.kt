package com.example.arweld.core.domain.state

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WorkItemStateTest {

    private fun event(id: String, workItemId: String, type: EventType, timestamp: Long): Event =
        Event(
            id = id,
            workItemId = workItemId,
            type = type,
            timestamp = timestamp,
            actorId = "user-1",
            actorRole = Role.ASSEMBLER,
            deviceId = "device-1",
            payloadJson = null,
        )

    @Test
    fun `happy path progresses to approved`() {
        val workId = "W-1"
        val events = listOf(
            event("1", workId, EventType.WORK_CLAIMED, 1L),
            event("2", workId, EventType.WORK_STARTED, 2L),
            event("3", workId, EventType.WORK_READY_FOR_QC, 3L),
            event("4", workId, EventType.QC_STARTED, 4L),
            event("5", workId, EventType.QC_PASSED, 5L),
        )

        val state = reduce(events)

        assertThat(state.status).isEqualTo(WorkStatus.APPROVED)
        assertThat(state.qcStatus).isEqualTo(QcStatus.PASSED)
        assertThat(state.readyForQcSince).isNull()
        assertThat(state.currentAssigneeId).isEqualTo("user-1")
        assertThat(state.lastEvent?.id).isEqualTo("5")
    }

    @Test
    fun `fail then rework then ready then pass`() {
        val workId = "W-2"
        val events = listOf(
            event("1", workId, EventType.WORK_CLAIMED, 1L),
            event("2", workId, EventType.WORK_STARTED, 2L),
            event("3", workId, EventType.WORK_READY_FOR_QC, 3L),
            event("4", workId, EventType.QC_STARTED, 4L),
            event("5", workId, EventType.QC_FAILED_REWORK, 5L),
            event("6", workId, EventType.REWORK_STARTED, 6L),
            event("7", workId, EventType.WORK_READY_FOR_QC, 7L),
            event("8", workId, EventType.QC_STARTED, 8L),
            event("9", workId, EventType.QC_PASSED, 9L),
        )

        val state = reduce(events)

        assertThat(state.status).isEqualTo(WorkStatus.APPROVED)
        assertThat(state.qcStatus).isEqualTo(QcStatus.PASSED)
        assertThat(state.readyForQcSince).isNull()
        assertThat(state.lastEvent?.id).isEqualTo("9")
    }
}
