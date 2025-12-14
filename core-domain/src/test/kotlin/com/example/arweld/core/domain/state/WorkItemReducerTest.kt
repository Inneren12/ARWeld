package com.example.arweld.core.domain.state

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkItemReducerTest {

    private fun event(
        type: EventType,
        actorId: String,
        actorRole: Role,
        timestamp: Long,
        idSuffix: String = type.name,
    ): Event {
        return Event(
            id = "$idSuffix-$timestamp",
            workItemId = "work-1",
            type = type,
            timestamp = timestamp,
            actorId = actorId,
            actorRole = actorRole,
            deviceId = "device-1",
            payloadJson = null,
        )
    }

    @Test
    fun `reduces empty list to initial state`() {
        val state = reduce(emptyList())

        assertEquals(WorkStatus.NEW, state.status)
        assertNull(state.lastEvent)
        assertNull(state.currentAssigneeId)
        assertEquals(QcStatus.NOT_STARTED, state.qcStatus)
    }

    @Test
    fun `simple claim sets in progress and assignee`() {
        val claimEvent = event(EventType.WORK_CLAIMED, actorId = "user-1", actorRole = Role.ASSEMBLER, timestamp = 1L)

        val state = reduce(listOf(claimEvent))

        assertEquals(WorkStatus.IN_PROGRESS, state.status)
        assertEquals("user-1", state.currentAssigneeId)
        assertEquals(claimEvent, state.lastEvent)
    }

    @Test
    fun `happy path progresses to approved`() {
        val claim = event(EventType.WORK_CLAIMED, actorId = "user-1", actorRole = Role.ASSEMBLER, timestamp = 1L)
        val readyForQc = event(EventType.WORK_READY_FOR_QC, actorId = "user-1", actorRole = Role.ASSEMBLER, timestamp = 2L)
        val qcStarted = event(EventType.QC_STARTED, actorId = "qc-1", actorRole = Role.QC, timestamp = 3L)
        val qcPassed = event(EventType.QC_PASSED, actorId = "qc-1", actorRole = Role.QC, timestamp = 4L)

        val state = reduce(listOf(claim, readyForQc, qcStarted, qcPassed))

        assertEquals(WorkStatus.APPROVED, state.status)
        assertEquals(QcStatus.PASSED, state.qcStatus)
        assertEquals("user-1", state.currentAssigneeId)
        assertEquals(qcPassed, state.lastEvent)
    }

    @Test
    fun `rework path flags rework required`() {
        val events = listOf(
            event(EventType.WORK_CLAIMED, actorId = "user-1", actorRole = Role.ASSEMBLER, timestamp = 1L),
            event(EventType.WORK_READY_FOR_QC, actorId = "user-1", actorRole = Role.ASSEMBLER, timestamp = 2L),
            event(EventType.QC_STARTED, actorId = "qc-1", actorRole = Role.QC, timestamp = 3L),
            event(EventType.QC_FAILED_REWORK, actorId = "qc-1", actorRole = Role.QC, timestamp = 4L),
        )

        val state = reduce(events)

        assertEquals(WorkStatus.REWORK_REQUIRED, state.status)
        assertEquals(QcStatus.REWORK_REQUIRED, state.qcStatus)
        assertEquals("user-1", state.currentAssigneeId)
        assertEquals(events.last(), state.lastEvent)
    }

    @Test
    fun `unsorted events are processed chronologically`() {
        val qcPassed = event(EventType.QC_PASSED, actorId = "qc-1", actorRole = Role.QC, timestamp = 40L)
        val qcStarted = event(EventType.QC_STARTED, actorId = "qc-1", actorRole = Role.QC, timestamp = 30L)
        val readyForQc = event(EventType.WORK_READY_FOR_QC, actorId = "user-1", actorRole = Role.ASSEMBLER, timestamp = 20L)
        val claimed = event(EventType.WORK_CLAIMED, actorId = "user-1", actorRole = Role.ASSEMBLER, timestamp = 10L)

        val state = reduce(listOf(qcPassed, qcStarted, readyForQc, claimed))

        assertEquals(WorkStatus.APPROVED, state.status)
        assertEquals(QcStatus.PASSED, state.qcStatus)
        assertEquals("user-1", state.currentAssigneeId)
        assertEquals(qcPassed, state.lastEvent)
    }
}
