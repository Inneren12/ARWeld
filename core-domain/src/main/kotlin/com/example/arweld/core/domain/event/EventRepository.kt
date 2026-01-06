package com.example.arweld.core.domain.event

/**
 * Domain-facing repository for working with Events.
 * Hides persistence details (Room) from callers.
 */
interface EventRepository {
    /**
     * Append a single event to the event log.
     */
    suspend fun appendEvent(event: Event)

    /**
     * Append multiple events in a batch.
     */
    suspend fun appendEvents(events: List<Event>)

    /**
     * Fetch all events for a WorkItem ordered by timestamp.
     */
    suspend fun getEventsForWorkItem(workItemId: String): List<Event>

    /**
     * Returns the most recent events performed by a given user ordered by recency.
     */
    suspend fun getLastEventsByUser(userId: String): List<Event>
}
