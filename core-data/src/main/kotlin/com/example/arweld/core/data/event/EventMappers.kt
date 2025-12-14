package com.example.arweld.core.data.event

import com.example.arweld.core.data.db.entity.EventEntity
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role

internal fun EventEntity.toDomain(): Event = Event(
    id = id,
    workItemId = workItemId,
    type = EventType.valueOf(type),
    timestamp = timestamp,
    actorId = actorId,
    actorRole = Role.valueOf(actorRole),
    deviceId = deviceId,
    payloadJson = payloadJson,
)

internal fun Event.toEntity(): EventEntity = EventEntity(
    id = id,
    workItemId = workItemId,
    type = type.name,
    timestamp = timestamp,
    actorId = actorId,
    actorRole = actorRole.name,
    deviceId = deviceId,
    payloadJson = payloadJson,
)
