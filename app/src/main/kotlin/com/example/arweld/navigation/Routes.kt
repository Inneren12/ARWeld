package com.example.arweld.navigation

const val ROUTE_LOGIN = "login"
const val ROUTE_HOME = "home"
const val ROUTE_SCAN_CODE = "scan_code"
const val ROUTE_ASSEMBLER_QUEUE = "assembler_queue"
const val ROUTE_QC_QUEUE = "qc_queue"
const val ROUTE_QC_START = "qc_start"
const val ROUTE_QC_CHECKLIST = "qc_checklist"
const val ROUTE_WORK_ITEM_SUMMARY = "work_item_summary"
const val ROUTE_TIMELINE = "timeline"
const val ROUTE_AR_VIEW = "ar_view"

fun workItemSummaryRoute(workItemId: String? = null): String {
    return workItemId?.let { "$ROUTE_WORK_ITEM_SUMMARY?workItemId=$it" } ?: ROUTE_WORK_ITEM_SUMMARY
}

fun arViewRoute(workItemId: String? = null): String {
    return workItemId?.let { "$ROUTE_AR_VIEW?workItemId=$it" } ?: ROUTE_AR_VIEW
}

fun qcStartRoute(workItemId: String): String {
    return "$ROUTE_QC_START?workItemId=$workItemId"
}

fun qcChecklistRoute(workItemId: String): String {
    return "$ROUTE_QC_CHECKLIST?workItemId=$workItemId"
}
