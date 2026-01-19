package com.example.arweld.feature.supervisor.ui

import com.example.arweld.core.domain.model.Role
import com.example.arweld.feature.supervisor.model.TimelineEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineListTest {

    @Test
    fun `orders timeline by timestamp then event id`() {
        val entries = listOf(
            TimelineEntry(
                eventId = "e-3",
                timestamp = 2000L,
                eventType = "WORK_STARTED",
                eventDescription = "",
                actorId = "u1",
                actorName = "User",
                actorRole = Role.ASSEMBLER,
                payloadSummary = null
            ),
            TimelineEntry(
                eventId = "e-1",
                timestamp = 1000L,
                eventType = "WORK_CLAIMED",
                eventDescription = "",
                actorId = "u1",
                actorName = "User",
                actorRole = Role.ASSEMBLER,
                payloadSummary = null
            ),
            TimelineEntry(
                eventId = "e-2",
                timestamp = 2000L,
                eventType = "WORK_READY_FOR_QC",
                eventDescription = "",
                actorId = "u1",
                actorName = "User",
                actorRole = Role.ASSEMBLER,
                payloadSummary = null
            )
        )

        val ordered = orderTimelineEntries(entries)

        assertEquals(listOf("e-1", "e-2", "e-3"), ordered.map { it.eventId })
    }
}
