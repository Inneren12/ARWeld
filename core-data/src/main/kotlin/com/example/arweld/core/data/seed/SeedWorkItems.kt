package com.example.arweld.core.data.seed

import com.example.arweld.core.data.db.entity.WorkItemEntity
import com.example.arweld.core.domain.model.WorkItemType

/**
 * Mock WorkItem records used to seed the local database for MVP flows.
 */
object SeedWorkItems {
    val items: List<WorkItemEntity> = listOf(
        WorkItemEntity(
            id = "W-001",
            projectId = "P-1",
            zoneId = "ZONE-A",
            type = WorkItemType.PART.name,
            code = "ARWELD-W-001",
            description = "Base plate alignment",
            nodeId = "NODE-BASE",
            createdAt = 1_733_000_000_000,
        ),
        WorkItemEntity(
            id = "W-002",
            projectId = "P-1",
            zoneId = "ZONE-B",
            type = WorkItemType.NODE.name,
            code = "ARWELD-W-002",
            description = "Column flange weld",
            nodeId = "NODE-FLANGE",
            createdAt = 1_733_000_100_000,
        ),
        WorkItemEntity(
            id = "W-003",
            projectId = "P-1",
            zoneId = null,
            type = WorkItemType.OPERATION.name,
            code = "ARWELD-W-003",
            description = "Paint prep",
            nodeId = null,
            createdAt = 1_733_000_200_000,
        ),
        WorkItemEntity(
            id = "W-004",
            projectId = "P-1",
            zoneId = "ZONE-C",
            type = WorkItemType.PART.name,
            code = "ARWELD-W-004",
            description = "Bracket tack weld",
            nodeId = "NODE-BRACKET",
            createdAt = 1_733_000_300_000,
        ),
    )
}
