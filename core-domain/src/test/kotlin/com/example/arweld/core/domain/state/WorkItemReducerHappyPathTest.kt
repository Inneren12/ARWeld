package com.example.arweld.core.domain.state

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkItemReducerHappyPathTest {

    @Test
    fun reduce_happyPath_assemblerToQcPass() {
        val events = listOf(
            Event(
                id = "e1",
                workItemId = "w1",
                type = EventType.WORK_CLAIMED,
                timestamp = 1L,
                actorId = "assembler-1",
                actorRole = Role.ASSEMBLER,
                deviceId = "device-1",
                payloadJson = "{}",
            ),
            Event(
                id = "e2",
                workItemId = "w1",
                type = EventType.WORK_READY_FOR_QC,
                timestamp = 2L,
                actorId = "assembler-1",
                actorRole = Role.ASSEMBLER,
                deviceId = "device-1",
                payloadJson = "{}",
            ),
            Event(
                id = "e3",
                workItemId = "w1",
                type = EventType.QC_STARTED,
                timestamp = 3L,
                actorId = "qc-1",
                actorRole = Role.QC,
                deviceId = "device-1",
                payloadJson = "{}",
            ),
            Event(
                id = "e4",
                workItemId = "w1",
                type = EventType.QC_PASSED,
                timestamp = 4L,
                actorId = "qc-1",
                actorRole = Role.QC,
                deviceId = "device-1",
                payloadJson = "{}",
            ),
        )

        val state = reduce(events)

        assertEquals(WorkStatus.APPROVED, state.status)
        assertEquals(QcStatus.PASSED, state.qcStatus)
        assertEquals("assembler-1", state.currentAssigneeId)
        assertEquals(EventType.QC_PASSED, state.lastEvent?.type)
    }
}
