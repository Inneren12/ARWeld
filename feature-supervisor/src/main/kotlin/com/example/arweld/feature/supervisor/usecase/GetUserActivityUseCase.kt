package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.event.toDomain
import com.example.arweld.core.domain.event.Event
import com.example.arweld.feature.supervisor.model.UserActivity
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Derives "who does what" from the event log.
 * Shows the last action of each active user and their current work item (if applicable).
 */
class GetUserActivityUseCase @Inject constructor(
    private val userDao: UserDao,
    private val eventDao: EventDao,
    private val workItemDao: WorkItemDao
) {
    suspend operator fun invoke(): List<UserActivity> {
        // Derive activity from the full event log (no userDao.observeAll()).
        val allWorkItems = workItemDao.observeAll().first()
        val workItemIds = allWorkItems.map { it.id }

        // Batch load all events (avoid N+1, avoid SQLite bind limit)
        val allEventEntities = if (workItemIds.isEmpty()) {
            emptyList()
        } else {
            workItemIds.chunked(900).flatMap { chunk ->
                eventDao.getByWorkItemIds(chunk)
            }
        }
        val allEvents: List<Event> = allEventEntities.map { it.toDomain() }
        if (allEvents.isEmpty()) return emptyList()

        // Last event per actor (stable tie-breaker by id)
        val lastEventByActor: List<Pair<String, Event>> = allEvents
            .groupBy { it.actorId }
            .mapNotNull { (actorId, events) ->
                val last = events.maxWithOrNull(compareBy<Event> { it.timestamp }.thenBy { it.id })
                    ?: return@mapNotNull null
                actorId to last
            }

        val activities = mutableListOf<UserActivity>()
        for ((actorId, lastEvent) in lastEventByActor) {
            val userEntity = userDao.getById(actorId) ?: continue

            val workItemCode = workItemDao.getById(lastEvent.workItemId)?.code

            activities += UserActivity(
                userId = userEntity.id,
                userName = userEntity.userNameCompat(fallback = userEntity.id),
                role = userEntity.userRoleCompat(),
                currentWorkItemId = lastEvent.workItemId,
                currentWorkItemCode = workItemCode,
                lastActionType = lastEvent.type.name,
                lastActionTimeMs = lastEvent.timestamp
            )
        }

        return activities.sortedByDescending { it.lastActionTimeMs }
    }
}
