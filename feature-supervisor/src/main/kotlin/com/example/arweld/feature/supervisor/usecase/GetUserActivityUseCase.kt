package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.event.toDomain
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
        // Get all users
        val allUsers = userDao.observeAll().first()

        // For each user, get their last event
        val activities = allUsers.mapNotNull { userEntity ->
            // Get only the last event for this user (LIMIT 1, avoids pulling large lists)
            val lastEventEntity = eventDao.getLastEventByUser(userEntity.id)

            if (lastEventEntity == null) {
                // User has no activity
                null
            } else {
                // Convert to domain event
                val lastEvent = lastEventEntity.toDomain()

                // Get work item code if available
                val workItemCode = lastEvent.workItemId.let { workItemId ->
                    workItemDao.getById(workItemId)?.code
                }

                UserActivity(
                    userId = userEntity.id,
                    userName = userEntity.displayName,
                    role = userEntity.role,
                    currentWorkItemId = lastEvent.workItemId,
                    currentWorkItemCode = workItemCode,
                    lastActionType = lastEvent.type.name,
                    lastActionTimeMs = lastEvent.timestamp
                )
            }
        }

        // Sort by last action time descending (most recent first)
        return activities.sortedByDescending { it.lastActionTimeMs }
    }
}
