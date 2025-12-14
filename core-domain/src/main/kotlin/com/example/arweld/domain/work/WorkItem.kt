package com.example.arweld.domain.work

data class WorkItem(
    val id: String,
    val projectId: String,
    val zoneId: String?,
    val type: WorkItemType,
    val code: String?,
) {
    fun isPart(): Boolean = type == WorkItemType.PART
    fun isNode(): Boolean = type == WorkItemType.NODE
    fun isOperation(): Boolean = type == WorkItemType.OPERATION
}
