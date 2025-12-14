package com.example.arweld.core.domain.state

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkItemReducerHappyPathTest {

    private val workItemId = "work-item-123"
    private val assemblerId = "assembler-1"
    private val qcId = "qc-1"
    private val deviceId = "device-1"

    @Test
    fun reduce_happyPath_assemblerToQcPass() {
        val events = listOf(
            Event(
                id = "evt-1",
                workItemId = workItemId,
                type = EventType.WORK_CLAIMED,
                timestamp = 1L,
                actorId = assemblerId,
                actorRole = Role.ASSEMBLER,
                deviceId = deviceId,
            ),
            Event(
                id = "evt-2",
                workItemId = workItemId,
                type = EventType.WORK_READY_FOR_QC,
                timestamp = 2L,
                actorId = assemblerId,
                actorRole = Role.ASSEMBLER,
                deviceId = deviceId,
            ),
            Event(
                id = "evt-3",
                workItemId = workItemId,
                type = EventType.QC_STARTED,
                timestamp = 3L,
                actorId = qcId,
                actorRole = Role.QC,
                deviceId = deviceId,
            ),
            Event(
                id = "evt-4",
                workItemId = workItemId,
                type = EventType.QC_PASSED,
                timestamp = 4L,
                actorId = qcId,
                actorRole = Role.QC,
                deviceId = deviceId,
            ),
        )

        val state = reduce(events)

        assertEquals(WorkStatus.APPROVED, state.status)
        assertEquals(QcStatus.PASSED, state.qcStatus)
        assertEquals(EventType.QC_PASSED, state.lastEvent?.type)
        assertEquals(assemblerId, state.currentAssigneeId)
    }

    @Test
    fun reduce_reworkFlow_failThenReadyThenPass() {
        val events = listOf(
            Event(
                id = "evt-1",
                workItemId = workItemId,
                type = EventType.WORK_CLAIMED,
                timestamp = 1L,
                actorId = assemblerId,
                actorRole = Role.ASSEMBLER,
                deviceId = deviceId,
            ),
            Event(
                id = "evt-2",
                workItemId = workItemId,
                type = EventType.WORK_READY_FOR_QC,
                timestamp = 2L,
                actorId = assemblerId,
                actorRole = Role.ASSEMBLER,
                deviceId = deviceId,
            ),
            Event(
                id = "evt-3",
                workItemId = workItemId,
                type = EventType.QC_STARTED,
                timestamp = 3L,
                actorId = qcId,
                actorRole = Role.QC,
                deviceId = deviceId,
            ),
            Event(
                id = "evt-4",
                workItemId = workItemId,
                type = EventType.QC_FAILED_REWORK,
                timestamp = 4L,
                actorId = qcId,
                actorRole = Role.QC,
                deviceId = deviceId,
            ),
            Event(
                id = "evt-5",
                workItemId = workItemId,
                type = EventType.WORK_CLAIMED,
                timestamp = 5L,
                actorId = assemblerId,
                actorRole = Role.ASSEMBLER,
                deviceId = deviceId,
            ),
            Event(
                id = "evt-6",
                workItemId = workItemId,
                type = EventType.WORK_READY_FOR_QC,
                timestamp = 6L,
                actorId = assemblerId,
                actorRole = Role.ASSEMBLER,
                deviceId = deviceId,
            ),
            Event(
                id = "evt-7",
                workItemId = workItemId,
                type = EventType.QC_STARTED,
                timestamp = 7L,
                actorId = qcId,
                actorRole = Role.QC,
                deviceId = deviceId,
            ),
            Event(
                id = "evt-8",
                workItemId = workItemId,
                type = EventType.QC_PASSED,
                timestamp = 8L,
                actorId = qcId,
                actorRole = Role.QC,
                deviceId = deviceId,
            ),
        )

        val reworkState = reduce(events.take(4))
        assertEquals(WorkStatus.REWORK_REQUIRED, reworkState.status)
        assertEquals(QcStatus.REWORK_REQUIRED, reworkState.qcStatus)
        assertEquals(EventType.QC_FAILED_REWORK, reworkState.lastEvent?.type)

        val finalState = reduce(events)
        assertEquals(WorkStatus.APPROVED, finalState.status)
        assertEquals(QcStatus.PASSED, finalState.qcStatus)
        assertEquals(EventType.QC_PASSED, finalState.lastEvent?.type)
    }
}
