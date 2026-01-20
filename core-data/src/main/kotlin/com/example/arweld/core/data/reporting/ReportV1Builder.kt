package com.example.arweld.core.data.reporting

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.event.toDomain
import com.example.arweld.core.data.work.toDomain
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.reporting.FailReasonAggregation
import com.example.arweld.core.domain.reporting.QcOutcome
import com.example.arweld.core.domain.reporting.QcResult
import com.example.arweld.core.domain.reporting.ReportV1
import com.example.arweld.core.domain.system.TimeProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ReportV1Builder @Inject constructor(
    private val workItemDao: WorkItemDao,
    private val eventDao: EventDao,
    private val timeProvider: TimeProvider,
) {
    suspend fun build(): ReportV1 {
        val workItems = workItemDao.observeAll().first().map { it.toDomain() }.sortedBy { it.id }
        val workItemIds = workItems.map { it.id }
        val events = workItemIds.chunked(CHUNK_SIZE).flatMap { chunk ->
            eventDao.getByWorkItemIds(chunk)
        }.map { it.toDomain() }
            .sortedWith(compareBy({ it.timestamp }, { it.id }))
        val qcResults = events.mapNotNull { event ->
            when (event.type) {
                EventType.QC_PASSED -> QcOutcome.PASSED
                EventType.QC_FAILED_REWORK -> QcOutcome.FAILED_REWORK
                else -> null
            }?.let { outcome ->
                QcResult(
                    eventId = event.id,
                    workItemId = event.workItemId,
                    outcome = outcome,
                    timestamp = event.timestamp,
                    inspectorId = event.actorId,
                    inspectorRole = event.actorRole,
                    deviceId = event.deviceId,
                    payloadJson = event.payloadJson,
                )
            }
        }.sortedWith(compareBy({ it.timestamp }, { it.eventId }))
        val topFailReasons = FailReasonAggregation.aggregateFromEvents(events)

        return ReportV1(
            reportVersion = 1,
            generatedAt = timeProvider.nowMillis(),
            workItems = workItems,
            events = events,
            qcResults = qcResults,
            topFailReasons = topFailReasons,
        )
    }

    private companion object {
        const val CHUNK_SIZE = 900
    }
}
