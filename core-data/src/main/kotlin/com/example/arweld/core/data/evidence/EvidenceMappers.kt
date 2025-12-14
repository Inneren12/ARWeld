package com.example.arweld.core.data.evidence

import com.example.arweld.core.data.db.entity.EvidenceEntity
import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.evidence.EvidenceKind

internal fun EvidenceEntity.toDomain(): Evidence = Evidence(
    id = id,
    eventId = eventId,
    kind = EvidenceKind.valueOf(kind),
    uri = uri,
    sha256 = sha256,
    metaJson = metaJson,
    createdAt = createdAt,
)

internal fun Evidence.toEntity(): EvidenceEntity = EvidenceEntity(
    id = id,
    eventId = eventId,
    kind = kind.name,
    uri = uri,
    sha256 = sha256,
    metaJson = metaJson,
    createdAt = createdAt,
)
