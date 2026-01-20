package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.EvidenceDao
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.event.toDomain
import com.example.arweld.core.data.evidence.toDomain
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.core.domain.state.reduce
import com.example.arweld.core.domain.system.TimeProvider
import com.example.arweld.feature.supervisor.export.CsvExporter
import com.example.arweld.feature.supervisor.export.ExportEvidence
import com.example.arweld.feature.supervisor.export.ExportEvent
import com.example.arweld.feature.supervisor.export.ExportMetadata
import com.example.arweld.feature.supervisor.export.ExportReport
import com.example.arweld.feature.supervisor.export.ExportReporting
import com.example.arweld.feature.supervisor.export.ExportSummary
import com.example.arweld.feature.supervisor.export.ExportWorkItem
import com.example.arweld.feature.supervisor.export.FailReasonEntry
import com.example.arweld.feature.supervisor.export.JsonExporter
import com.example.arweld.core.data.reporting.ExportedFileReference
import com.example.arweld.core.data.reporting.ManifestBuilder
import com.example.arweld.core.domain.reporting.ExportManifestV1Json
import com.example.arweld.core.domain.reporting.ReportPeriod
import com.example.arweld.feature.supervisor.export.NodeIssueEntry
import com.example.arweld.feature.supervisor.export.ShiftReportEntry
import com.example.arweld.feature.supervisor.export.ZipPackager
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class ExportOptions(
    val includeCsv: Boolean = true,
    val includeZip: Boolean = true,
    val includeManifest: Boolean = true,
)

data class ExportPeriod(
    val startMillis: Long,
    val endMillis: Long,
    val label: String,
)

data class ExportResult(
    val exportDir: File,
    val jsonFile: File,
    val csvFile: File?,
    val manifestFile: File?,
    val zipFile: File?,
)

class ExportReportUseCase @Inject constructor(
    private val workItemDao: WorkItemDao,
    private val eventDao: EventDao,
    private val evidenceDao: EvidenceDao,
    private val userDao: UserDao,
    private val authRepository: AuthRepository,
    private val timeProvider: TimeProvider,
    private val jsonExporter: JsonExporter = JsonExporter(),
    private val csvExporter: CsvExporter = CsvExporter(),
    private val manifestBuilder: ManifestBuilder,
    private val zipPackager: ZipPackager = ZipPackager(),
) {

    suspend operator fun invoke(
        period: ExportPeriod,
        options: ExportOptions,
        outputRoot: File,
    ): ExportResult {
        val user = authRepository.currentUser()
        val exportedBy = user?.displayName ?: user?.id ?: "unknown"
        val exportId = buildExportId(timeProvider.nowMillis())
        val exportDir = File(outputRoot, exportId).apply { mkdirs() }
        val evidenceDir = File(exportDir, "evidence").apply { mkdirs() }

        val workItems = workItemDao.observeAll().first()
        val workItemIds = workItems.map { it.id }
        val eventEntities = if (workItemIds.isEmpty()) {
            emptyList()
        } else {
            workItemIds.chunked(900).flatMap { chunk -> eventDao.getByWorkItemIds(chunk) }
        }
        val events = eventEntities.map { it.toDomain() }
        val eventsByWorkItem = events.groupBy { it.workItemId }
        val eventsInPeriod = events.filter { it.timestamp in period.startMillis..period.endMillis }
        val eventIdsInPeriod = eventsInPeriod.map { it.id }
        val evidenceInPeriod = if (eventIdsInPeriod.isEmpty()) {
            emptyList()
        } else {
            evidenceDao.listByEvents(eventIdsInPeriod).map { it.toDomain() }
        }
        val evidenceByWorkItem = evidenceInPeriod.groupBy { it.workItemId }
        val usersById = userDao.getAll().associateBy { it.id }

        val includedWorkItems = workItems.filter { workItem ->
            val hasEvents = eventsByWorkItem[workItem.id].orEmpty().any {
                it.timestamp in period.startMillis..period.endMillis
            }
            val createdAt = workItem.createdAt ?: 0L
            hasEvents || createdAt in period.startMillis..period.endMillis
        }

        val exportWorkItems = includedWorkItems.map { workItem ->
            val allEvents = eventsByWorkItem[workItem.id].orEmpty()
            val eventsForStatus = allEvents.filter { it.timestamp <= period.endMillis }
            val state = reduce(eventsForStatus)
            val itemEvents = allEvents
                .filter { it.timestamp in period.startMillis..period.endMillis }
                .sortedWith(compareBy<Event> { it.timestamp }.thenBy { it.id })
                .map { event ->
                    ExportEvent(
                        id = event.id,
                        type = event.type.name,
                        actorId = event.actorId,
                        actorName = usersById[event.actorId]?.userNameCompat(fallback = event.actorId),
                        actorRole = event.actorRole.name,
                        timestamp = formatInstant(event.timestamp),
                        payloadJson = event.payloadJson,
                    )
                }
            val itemEvidence = evidenceByWorkItem[workItem.id].orEmpty()
                .sortedWith(compareBy<Evidence> { it.createdAt }.thenBy { it.id })
                .map { evidence ->
                    val filePath = copyEvidenceIfExists(evidence, evidenceDir)
                    ExportEvidence(
                        id = evidence.id,
                        eventId = evidence.eventId,
                        kind = evidence.kind.name,
                        filePath = filePath,
                        fileHash = evidence.sha256,
                        capturedAt = formatInstant(evidence.createdAt),
                        sizeBytes = evidence.sizeBytes,
                        uri = evidence.uri,
                    )
                }

            ExportWorkItem(
                id = workItem.id,
                code = workItem.code ?: "",
                description = workItem.description ?: "",
                status = state.status.name,
                zone = workItem.zoneId,
                nodeId = workItem.nodeId,
                events = itemEvents,
                evidence = itemEvidence,
            )
        }.sortedBy { it.id }

        val summary = buildSummary(exportWorkItems)
        val reporting = buildReporting(exportWorkItems, eventsInPeriod, includedWorkItems.map { it.nodeId })
        val report = ExportReport(
            metadata = ExportMetadata(
                exportId = exportId,
                exportedAt = formatInstant(timeProvider.nowMillis()),
                exportedBy = exportedBy,
                periodStart = formatInstant(period.startMillis),
                periodEnd = formatInstant(period.endMillis),
            ),
            workItems = exportWorkItems,
            summary = summary,
            reporting = reporting,
        )

        val jsonFile = File(exportDir, "export.json").also { jsonExporter.exportToFile(report, it) }
        val csvFile = if (options.includeCsv) {
            File(exportDir, "export.csv").also { csvExporter.exportToFile(report, it) }
        } else {
            null
        }

        val zipFile = if (options.includeZip) {
            File(exportDir, "evidence.zip").also { zipFile ->
                val zipTargets = listOfNotNull(jsonFile, csvFile)
                val evidenceFiles = evidenceDir.walkTopDown().filter { it.isFile }.toList()
                zipPackager.zipFiles(zipFile, zipTargets + evidenceFiles, exportDir)
            }
        } else {
            null
        }

        val manifestFile = if (options.includeManifest) {
            val warnings = buildManifestWarnings(csvFile, zipFile)
            val manifest = manifestBuilder.build(
                period = ReportPeriod(startMillis = period.startMillis, endMillis = period.endMillis),
                exportedFiles = listOfNotNull(
                    ExportedFileReference(file = jsonFile),
                    csvFile?.let { ExportedFileReference(file = it) },
                    zipFile?.let { ExportedFileReference(file = it) },
                ),
                warnings = warnings,
            )
            File(exportDir, buildManifestFileName(period)).also { manifestFile ->
                manifestFile.writeText(ExportManifestV1Json.encode(manifest))
            }
        } else {
            null
        }

        return ExportResult(
            exportDir = exportDir,
            jsonFile = jsonFile,
            csvFile = csvFile,
            manifestFile = manifestFile,
            zipFile = zipFile,
        )
    }

    private fun buildSummary(items: List<ExportWorkItem>): ExportSummary {
        val passed = items.count { it.status == WorkStatus.APPROVED.name }
        val failed = items.count { it.status == WorkStatus.REWORK_REQUIRED.name }
        val total = items.size
        val passRate = if (total == 0) 0.0 else passed.toDouble() / total.toDouble()
        return ExportSummary(
            totalWorkItems = total,
            passed = passed,
            failed = failed,
            qcPassRate = passRate,
        )
    }

    private fun buildReporting(
        items: List<ExportWorkItem>,
        eventsInPeriod: List<Event>,
        nodeIds: List<String?>,
    ): ExportReporting {
        val qcEvents = eventsInPeriod.filter { it.type == EventType.QC_PASSED || it.type == EventType.QC_FAILED_REWORK }
        val shiftCounts = qcEvents.groupBy { shiftLabel(it.timestamp) }
            .map { (label, events) ->
                val passed = events.count { it.type == EventType.QC_PASSED }
                val failed = events.count { it.type == EventType.QC_FAILED_REWORK }
                ShiftReportEntry(
                    label = label,
                    total = events.size,
                    passed = passed,
                    failed = failed,
                )
            }
            .sortedBy { it.label }

        val failReasons = eventsInPeriod.filter { it.type == EventType.QC_FAILED_REWORK }
            .flatMap { event ->
                parseFailReasons(event.payloadJson)
            }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { (reason, count) -> FailReasonEntry(reason = reason, count = count) }

        val nodeIdCounts = nodeIds.filterNotNull().groupingBy { it }.eachCount()
        val nodeFailures = items.filter { it.status == WorkStatus.REWORK_REQUIRED.name }
            .mapNotNull { it.nodeId }
            .groupingBy { it }
            .eachCount()

        val problematicNodes = nodeFailures.entries.map { (nodeId, failures) ->
            NodeIssueEntry(
                nodeId = nodeId,
                failures = failures,
                totalItems = nodeIdCounts[nodeId] ?: failures,
            )
        }.sortedWith(compareByDescending<NodeIssueEntry> { it.failures }.thenBy { it.nodeId })

        return ExportReporting(
            shiftCounts = shiftCounts,
            topFailReasons = failReasons,
            problematicNodes = problematicNodes,
        )
    }

    private fun shiftLabel(timestamp: Long): String {
        val hour = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).hour
        return when (hour) {
            in 6 until 14 -> "Shift 06:00-14:00"
            in 14 until 22 -> "Shift 14:00-22:00"
            else -> "Shift 22:00-06:00"
        }
    }

    private fun parseFailReasons(payloadJson: String?): List<String> {
        if (payloadJson.isNullOrBlank()) return emptyList()
        return runCatching {
            val json = Json { ignoreUnknownKeys = true }
            val element = json.decodeFromString<JsonElement>(payloadJson)
            element.jsonObject["reasons"]
                ?.jsonArray
                ?.mapNotNull { it.jsonPrimitive.contentOrNull }
                .orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun copyEvidenceIfExists(evidence: Evidence, evidenceDir: File): String? {
        val sourceFile = resolveEvidenceFile(evidence.uri) ?: return null
        if (!sourceFile.exists()) return null
        val fileName = "${evidence.id}_${sourceFile.name}"
        val destination = File(evidenceDir, fileName)
        sourceFile.copyTo(destination, overwrite = true)
        return "evidence/$fileName"
    }

    private fun resolveEvidenceFile(uriString: String): File? {
        return runCatching {
            val uri = java.net.URI(uriString)
            when (uri.scheme) {
                null -> File(uriString)
                "file" -> File(uri)
                else -> null
            }
        }.getOrNull()
    }

    private fun buildExportId(timestampMillis: Long): String {
        val instant = Instant.ofEpochMilli(timestampMillis)
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC)
        return "exp-${formatter.format(instant)}"
    }

    private fun formatInstant(timestampMillis: Long): String {
        return Instant.ofEpochMilli(timestampMillis).toString()
    }

    private fun buildManifestFileName(period: ExportPeriod): String {
        val date = Instant.ofEpochMilli(period.endMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        return "arweld-manifest-v1-${MANIFEST_DATE_FORMAT.format(date)}.json"
    }

    private fun buildManifestWarnings(csvFile: File?, zipFile: File?): List<String> {
        val warnings = mutableListOf<String>()
        if (csvFile == null) {
            warnings.add("Summary CSV not exported.")
        }
        if (zipFile == null) {
            warnings.add("Evidence zip not exported.")
        }
        return warnings
    }

    private companion object {
        private val MANIFEST_DATE_FORMAT = DateTimeFormatter.ISO_DATE.withZone(ZoneOffset.UTC)
    }
}
