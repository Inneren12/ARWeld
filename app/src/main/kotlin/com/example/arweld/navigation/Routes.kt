package com.example.arweld.navigation

const val ROUTE_LOGIN = "login"
const val ROUTE_HOME = "home"
const val ROUTE_SCAN_CODE = "scan_code"
const val ROUTE_ASSEMBLER_QUEUE = "assembler_queue"
const val ROUTE_WORK_ITEM_SUMMARY = "work_item_summary"
const val ROUTE_TIMELINE = "timeline"

fun workItemSummaryRoute(workItemId: String? = null): String {
    return workItemId?.let { "$ROUTE_WORK_ITEM_SUMMARY?workItemId=$it" } ?: ROUTE_WORK_ITEM_SUMMARY
}
