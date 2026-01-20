package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.EvidenceDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.feature.supervisor.export.EvidenceFileDescriptor
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class EvidenceCollector @Inject constructor(
    private val workItemDao: WorkItemDao,
    private val eventDao: EventDao,
    private val evidenceDao: EvidenceDao,
) {
    suspend fun collect(period: ExportPeriod): List<EvidenceFileDescriptor> = withContext(Dispatchers.IO) {
        val workItems = workItemDao.observeAll().first()
        val workItemIds = workItems.map { it.id }
        val eventEntities = if (workItemIds.isEmpty()) {
            emptyList()
        } else {
            workItemIds.chunked(CHUNK_SIZE).flatMap { chunk -> eventDao.getByWorkItemIds(chunk) }
        }
        val eventIdsInPeriod = eventEntities
            .filter { it.timestamp in period.startMillis..period.endMillis }
            .map { it.id }

        val evidenceEntities = if (eventIdsInPeriod.isEmpty()) {
            emptyList()
        } else {
            evidenceDao.listByEvents(eventIdsInPeriod)
        }

        evidenceEntities.map { evidence ->
            EvidenceFileDescriptor(
                evidenceId = evidence.id,
                workItemId = evidence.workItemId,
                kind = evidence.kind,
                uri = evidence.uri,
                createdAt = evidence.createdAt,
                sizeBytes = evidence.sizeBytes,
            )
        }.sortedWith(EvidenceFileDescriptor.ORDERING)
    }

    private companion object {
        private const val CHUNK_SIZE = 900
    }
}
