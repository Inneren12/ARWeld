package com.example.arweld.core.domain.event

import com.example.arweld.core.domain.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventTest {

    @Test
    fun `event stores provided values`() {
        val event = Event(
            id = "event-1",
            workItemId = "work-123",
            type = EventType.QC_STARTED,
            timestamp = 1_733_000_000_000,
            actorId = "user-7",
            actorRole = Role.QC,
            deviceId = "device-42",
            payloadJson = "{\"note\":\"alignment set\"}"
        )

        assertEquals("event-1", event.id)
        assertEquals("work-123", event.workItemId)
        assertEquals(EventType.QC_STARTED, event.type)
        assertEquals(1_733_000_000_000, event.timestamp)
        assertEquals("user-7", event.actorId)
        assertEquals(Role.QC, event.actorRole)
        assertEquals("device-42", event.deviceId)
        assertEquals("{\"note\":\"alignment set\"}", event.payloadJson)
    }

    @Test
    fun `isQcEvent helper recognizes qc actions`() {
        val qcEvent = Event(
            id = "event-2",
            workItemId = "work-123",
            type = EventType.QC_PASSED,
            timestamp = 1L,
            actorId = "qc-1",
            actorRole = Role.QC,
            deviceId = "device-1",
            payloadJson = null
        )

        val nonQcEvent = Event(
            id = "event-3",
            workItemId = "work-123",
            type = EventType.WORK_STARTED,
            timestamp = 2L,
            actorId = "assembler-1",
            actorRole = Role.ASSEMBLER,
            deviceId = "device-2",
            payloadJson = null
        )

        assertTrue(qcEvent.isQcEvent())
        assertFalse(nonQcEvent.isQcEvent())
    }
}
