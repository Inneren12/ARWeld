package com.example.arweld.domain.work

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class WorkItemTest {

    @Test
    fun `should instantiate work item with all fields`() {
        val workItem = WorkItem(
            id = "item-1",
            projectId = "project-1",
            zoneId = "zone-7",
            type = WorkItemType.NODE,
            code = "QR-123",
        )

        assertEquals("item-1", workItem.id)
        assertEquals("project-1", workItem.projectId)
        assertEquals("zone-7", workItem.zoneId)
        assertEquals(WorkItemType.NODE, workItem.type)
        assertEquals("QR-123", workItem.code)
        assertEquals(true, workItem.isNode())
        assertEquals(false, workItem.isPart())
        assertEquals(false, workItem.isOperation())
    }

    @Test
    fun `data class equality should use all properties`() {
        val base = WorkItem(
            id = "item-1",
            projectId = "project-1",
            zoneId = null,
            type = WorkItemType.PART,
            code = null,
        )

        val same = WorkItem(
            id = "item-1",
            projectId = "project-1",
            zoneId = null,
            type = WorkItemType.PART,
            code = null,
        )

        val differentType = base.copy(type = WorkItemType.OPERATION)

        assertEquals(base, same)
        assertEquals(base.hashCode(), same.hashCode())
        assertNotEquals(base, differentType)
    }
}
