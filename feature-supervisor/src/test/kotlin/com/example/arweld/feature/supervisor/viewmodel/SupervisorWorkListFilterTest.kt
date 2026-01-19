package com.example.arweld.feature.supervisor.viewmodel

import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.feature.supervisor.model.SupervisorWorkItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class SupervisorWorkListFilterTest {

    @Test
    fun `filters by status zone assignee and search`() {
        val items = listOf(
            SupervisorWorkItem(
                workItemId = "wi-1",
                code = "W-001",
                description = "Pipe weld",
                zoneId = "Z1",
                status = WorkStatus.IN_PROGRESS,
                lastChangedAt = 10_000L,
                assigneeId = "u1",
                assigneeName = "Avery"
            ),
            SupervisorWorkItem(
                workItemId = "wi-2",
                code = "W-002",
                description = "Valve install",
                zoneId = "Z2",
                status = WorkStatus.READY_FOR_QC,
                lastChangedAt = 20_000L,
                assigneeId = "u2",
                assigneeName = "Sam"
            )
        )

        val filters = WorkListFilters(
            searchQuery = "W-001",
            status = WorkStatus.IN_PROGRESS,
            zoneId = "Z1",
            assigneeId = "u1",
            dateRange = WorkListDateRange.ALL
        )

        val filtered = applyWorkListFilters(items, filters, nowMs = 50_000L)

        assertEquals(listOf("wi-1"), filtered.map { it.workItemId })
    }

    @Test
    fun `filters by date range and sorts by last change desc`() {
        val now = TimeUnit.DAYS.toMillis(2)
        val items = listOf(
            SupervisorWorkItem(
                workItemId = "wi-1",
                code = "W-001",
                description = "Alpha",
                zoneId = "Z1",
                status = WorkStatus.NEW,
                lastChangedAt = now - TimeUnit.HOURS.toMillis(3),
                assigneeId = null,
                assigneeName = null
            ),
            SupervisorWorkItem(
                workItemId = "wi-2",
                code = "W-002",
                description = "Beta",
                zoneId = "Z1",
                status = WorkStatus.NEW,
                lastChangedAt = now - TimeUnit.HOURS.toMillis(1),
                assigneeId = null,
                assigneeName = null
            ),
            SupervisorWorkItem(
                workItemId = "wi-3",
                code = "W-003",
                description = "Gamma",
                zoneId = "Z1",
                status = WorkStatus.NEW,
                lastChangedAt = now - TimeUnit.HOURS.toMillis(2),
                assigneeId = null,
                assigneeName = null
            )
        )
        val filters = WorkListFilters(dateRange = WorkListDateRange.LAST_24_HOURS)

        val filtered = applyWorkListFilters(items, filters, nowMs = now)

        assertEquals(listOf("wi-2", "wi-3", "wi-1"), filtered.map { it.workItemId })
    }

    @Test
    fun `sorts by last change ascending`() {
        val now = TimeUnit.DAYS.toMillis(2)
        val items = listOf(
            SupervisorWorkItem(
                workItemId = "wi-1",
                code = "W-001",
                description = "Alpha",
                zoneId = "Z1",
                status = WorkStatus.NEW,
                lastChangedAt = now - TimeUnit.HOURS.toMillis(3),
                assigneeId = null,
                assigneeName = null
            ),
            SupervisorWorkItem(
                workItemId = "wi-2",
                code = "W-002",
                description = "Beta",
                zoneId = "Z1",
                status = WorkStatus.NEW,
                lastChangedAt = now - TimeUnit.HOURS.toMillis(1),
                assigneeId = null,
                assigneeName = null
            ),
            SupervisorWorkItem(
                workItemId = "wi-3",
                code = "W-003",
                description = "Gamma",
                zoneId = "Z1",
                status = WorkStatus.NEW,
                lastChangedAt = now - TimeUnit.HOURS.toMillis(2),
                assigneeId = null,
                assigneeName = null
            )
        )
        val filters = WorkListFilters(sortOrder = WorkListSortOrder.LAST_CHANGED_ASC)

        val filtered = applyWorkListFilters(items, filters, nowMs = now)

        assertEquals(listOf("wi-1", "wi-3", "wi-2"), filtered.map { it.workItemId })
    }

    @Test
    fun `filters by date range excludes older entries`() {
        val now = TimeUnit.DAYS.toMillis(10)
        val items = listOf(
            SupervisorWorkItem(
                workItemId = "wi-1",
                code = "W-001",
                description = "Alpha",
                zoneId = "Z1",
                status = WorkStatus.NEW,
                lastChangedAt = now - TimeUnit.DAYS.toMillis(9),
                assigneeId = null,
                assigneeName = null
            ),
            SupervisorWorkItem(
                workItemId = "wi-2",
                code = "W-002",
                description = "Beta",
                zoneId = "Z1",
                status = WorkStatus.NEW,
                lastChangedAt = now - TimeUnit.DAYS.toMillis(2),
                assigneeId = null,
                assigneeName = null
            )
        )
        val filters = WorkListFilters(dateRange = WorkListDateRange.LAST_7_DAYS)

        val filtered = applyWorkListFilters(items, filters, nowMs = now)

        assertEquals(listOf("wi-2"), filtered.map { it.workItemId })
    }

    @Test
    fun `filters by status zone and assignee independently`() {
        val items = listOf(
            SupervisorWorkItem(
                workItemId = "wi-1",
                code = "W-001",
                description = "Alpha",
                zoneId = "Z1",
                status = WorkStatus.IN_PROGRESS,
                lastChangedAt = 1_000L,
                assigneeId = "u1",
                assigneeName = "Avery"
            ),
            SupervisorWorkItem(
                workItemId = "wi-2",
                code = "W-002",
                description = "Beta",
                zoneId = "Z2",
                status = WorkStatus.READY_FOR_QC,
                lastChangedAt = 2_000L,
                assigneeId = "u2",
                assigneeName = "Sam"
            )
        )

        val filters = WorkListFilters(
            status = WorkStatus.IN_PROGRESS,
            zoneId = "Z1",
            assigneeId = "u1"
        )

        val filtered = applyWorkListFilters(items, filters, nowMs = 5_000L)

        assertEquals(listOf("wi-1"), filtered.map { it.workItemId })
    }

    @Test
    fun `search matches by code or work item id`() {
        val items = listOf(
            SupervisorWorkItem(
                workItemId = "work-123",
                code = "ARWELD-001",
                description = "Alpha",
                zoneId = "Z1",
                status = WorkStatus.NEW,
                lastChangedAt = 1_000L,
                assigneeId = null,
                assigneeName = null
            ),
            SupervisorWorkItem(
                workItemId = "work-456",
                code = "ARWELD-XYZ",
                description = "Beta",
                zoneId = "Z2",
                status = WorkStatus.NEW,
                lastChangedAt = 2_000L,
                assigneeId = null,
                assigneeName = null
            )
        )

        val codeFilters = WorkListFilters(searchQuery = "arweld-001")
        val idFilters = WorkListFilters(searchQuery = "WORK-456")

        val byCode = applyWorkListFilters(items, codeFilters, nowMs = 5_000L)
        val byId = applyWorkListFilters(items, idFilters, nowMs = 5_000L)

        assertEquals(listOf("work-123"), byCode.map { it.workItemId })
        assertEquals(listOf("work-456"), byId.map { it.workItemId })
    }
}
