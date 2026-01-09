package com.example.arweld.feature.supervisor.export

import org.junit.Assert.assertEquals
import org.junit.Test

class JsonExporterGoldenTest {

    @Test
    fun `exports deterministic json`() {
        val report = ExportReport(
            metadata = ExportMetadata(
                exportId = "exp-20250101-120000",
                exportedAt = "2025-01-01T12:00:00Z",
                exportedBy = "supervisor@example.com",
                periodStart = "2025-01-01T06:00:00Z",
                periodEnd = "2025-01-01T14:00:00Z",
            ),
            workItems = listOf(
                ExportWorkItem(
                    id = "wi-1234",
                    code = "QR-ABC-001",
                    description = "Weld assembly A1",
                    status = "APPROVED",
                    zone = "Zone-A",
                    nodeId = "Node-A1",
                    events = listOf(
                        ExportEvent(
                            id = "evt-001",
                            type = "WORK_CLAIMED",
                            actorId = "user-1",
                            actorName = "John Smith",
                            actorRole = "ASSEMBLER",
                            timestamp = "2025-01-01T09:30:00Z",
                            payloadJson = null,
                        ),
                        ExportEvent(
                            id = "evt-002",
                            type = "QC_PASSED",
                            actorId = "user-2",
                            actorName = "Jane Doe",
                            actorRole = "QC",
                            timestamp = "2025-01-01T11:42:00Z",
                            payloadJson = "{\"notes\":\"Excellent quality\"}",
                        ),
                    ),
                    evidence = listOf(
                        ExportEvidence(
                            id = "evi-001",
                            eventId = "evt-002",
                            kind = "AR_SCREENSHOT",
                            filePath = "evidence/evi-001_image.png",
                            fileHash = "a1b2c3d4e5f6",
                            capturedAt = "2025-01-01T11:40:35Z",
                            sizeBytes = 2048,
                            uri = "file:///evidence/evi-001_image.png",
                        ),
                    ),
                ),
            ),
            summary = ExportSummary(
                totalWorkItems = 1,
                passed = 1,
                failed = 0,
                qcPassRate = 1.0,
            ),
            reporting = ExportReporting(
                shiftCounts = listOf(
                    ShiftReportEntry(
                        label = "Shift 06:00-14:00",
                        total = 1,
                        passed = 1,
                        failed = 0,
                    ),
                ),
                topFailReasons = listOf(
                    FailReasonEntry(reason = "POROSITY", count = 2),
                ),
                problematicNodes = listOf(
                    NodeIssueEntry(nodeId = "Node-A1", failures = 1, totalItems = 3),
                ),
            ),
        )

        val expected = readResource("golden/export_report.json")
        val actual = JsonExporter().exportToString(report)

        assertEquals(expected.trim(), actual.trim())
    }

    private fun readResource(path: String): String {
        val url = javaClass.classLoader?.getResource(path)
            ?: error("Missing resource: $path")
        return url.readText()
    }
}
