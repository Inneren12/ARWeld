package com.example.arweld.core.domain.event

import com.example.arweld.core.domain.model.Role
import org.junit.Assert.assertEquals
import org.junit.Test

class EventJsonTest {

    @Test
    fun `encode returns stable json`() {
        val event = Event(
            id = "event-1",
            workItemId = "work-1",
            type = EventType.WORK_STARTED,
            timestamp = 1_000L,
            actorId = "user-1",
            actorRole = Role.ASSEMBLER,
            deviceId = "device-1",
            payloadJson = "{\"note\":\"ok\"}",
        )

        val first = EventJson.encode(event)
        val second = EventJson.encode(event)

        val expected = """
            {"id":"event-1","workItemId":"work-1","type":"WORK_STARTED","timestamp":1000,"actorId":"user-1","actorRole":"ASSEMBLER","deviceId":"device-1","payloadJson":"{\"note\":\"ok\"}"}
        """.trimIndent()

        assertEquals(expected, first)
        assertEquals(first, second)
    }
}
