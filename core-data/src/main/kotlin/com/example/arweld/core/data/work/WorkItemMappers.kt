package com.example.arweld.core.data.work

import com.example.arweld.core.data.db.entity.WorkItemEntity
import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.model.WorkItemType

internal fun WorkItem.toEntity(): WorkItemEntity = WorkItemEntity(
    id = id,
    projectId = "", // Project scoping will be added in a later sprint
    zoneId = zone,
    type = type.name,
    code = code,
    description = description,
    nodeId = nodeId,
    createdAt = createdAt,
)

internal fun WorkItemEntity.toDomain(): WorkItem = WorkItem(
    id = id,
    code = code ?: "",
    type = WorkItemType.valueOf(type),
    description = description ?: "",
    zone = zoneId,
    nodeId = nodeId,
    createdAt = createdAt ?: 0L,
)
