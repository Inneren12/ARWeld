package com.example.arweld.core.data.repository

import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.db.entity.WorkItemEntity
import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.model.WorkItemType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WorkItemRepository using Room database.
 */
@Singleton
class WorkItemRepositoryImpl @Inject constructor(
    private val workItemDao: WorkItemDao
) : WorkItemRepository {

    override suspend fun insert(workItem: WorkItem) {
        workItemDao.insert(workItem.toEntity())
    }

    override suspend fun getById(id: String): WorkItem? {
        return workItemDao.getById(id)?.toDomain()
    }

    override suspend fun findByCode(code: String): WorkItem? {
        return workItemDao.getByCode(code)?.toDomain()
    }

    override fun observeAll(): Flow<List<WorkItem>> {
        return workItemDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun WorkItem.toEntity() = WorkItemEntity(
        id = id,
        projectId = "", // Project scoping will be added in a later sprint
        zoneId = zone,
        type = type.name,
        code = code,
        description = description,
        nodeId = nodeId,
        createdAt = createdAt,
    )

    private fun WorkItemEntity.toDomain() = WorkItem(
        id = id,
        code = code ?: "",
        type = WorkItemType.valueOf(type),
        description = description ?: "",
        zone = zoneId,
        nodeId = nodeId,
        createdAt = createdAt ?: 0L
    )
}
