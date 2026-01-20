package com.example.arweld.core.data.reporting

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.model.WorkItemType
import com.example.arweld.core.domain.reporting.QcOutcome
import com.example.arweld.core.domain.reporting.QcResult
import com.example.arweld.core.domain.reporting.ReportV1
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvSummaryBuilderTest {

    @Test
    fun `builds deterministic rows with summary fields`() {
        val report = ReportV1(
            generatedAt = 0L,
            workItems = listOf(
                WorkItem(
                    id = "B-200",
                    code = "B",
                    type = WorkItemType.PART,
                    description = "Second",
                    zone = "Zone-2",
                    nodeId = null,
                    createdAt = 0L,
                ),
                WorkItem(
                    id = "A-100",
                    code = "A",
                    type = WorkItemType.PART,
                    description = "First",
                    zone = "Zone-1",
                    nodeId = null,
                    createdAt = 0L,
                ),
            ),
            events = listOf(
                Event(
                    id = "evt-1",
                    workItemId = "A-100",
                    type = EventType.WORK_CLAIMED,
                    timestamp = 1_000L,
                    actorId = "user",
                    actorRole = Role.ASSEMBLER,
                    deviceId = "device",
                    payloadJson = null,
                ),
                Event(
                    id = "evt-2",
                    workItemId = "A-100",
                    type = EventType.QC_FAILED_REWORK,
                    timestamp = 2_000L,
                    actorId = "qc",
                    actorRole = Role.QC,
                    deviceId = "device",
                    payloadJson = "{\"reasons\":[\"POROSITY\",\"CRACK\",\"POROSITY\"]}",
                ),
                Event(
                    id = "evt-3",
                    workItemId = "B-200",
                    type = EventType.WORK_STARTED,
                    timestamp = 1_000L,
                    actorId = "user",
                    actorRole = Role.ASSEMBLER,
                    deviceId = "device",
                    payloadJson = null,
                ),
            ),
            qcResults = listOf(
                QcResult(
                    eventId = "evt-2",
                    workItemId = "A-100",
                    outcome = QcOutcome.FAILED_REWORK,
                    timestamp = 2_000L,
                    inspectorId = "qc",
                    inspectorRole = Role.QC,
                    deviceId = "device",
                    payloadJson = null,
                ),
            ),
        )

        val summary = CsvSummaryBuilder().build(report)

        assertEquals(listOf("A-100", "B-200"), summary.rows.map { it[0] })
        assertEquals("DONE_FAIL", summary.rows[0][2])
        assertEquals("false", summary.rows[0][3])
        assertEquals("1", summary.rows[0][4])
        assertEquals("POROSITY", summary.rows[0][5])
        assertEquals("1970-01-01T00:00:01Z", summary.rows[0][6])
        assertEquals("1970-01-01T00:00:02Z", summary.rows[0][7])
        assertEquals("1", summary.rows[0][8])
        assertEquals("IN_PROGRESS", summary.rows[1][2])
        assertEquals("", summary.rows[1][3])
        assertEquals("", summary.rows[1][7])
        assertEquals("", summary.rows[1][8])
    }
}
