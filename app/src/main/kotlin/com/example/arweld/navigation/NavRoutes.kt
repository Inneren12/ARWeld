package com.example.arweld.navigation

import android.net.Uri

const val ROUTE_SPLASH = "splash"
const val ROUTE_LOGIN = "login"
const val ROUTE_HOME = "home"
const val ROUTE_ASSEMBLER_QUEUE = "assembler_queue"
const val ROUTE_QC_QUEUE = "qc_queue"
const val ROUTE_QC_CHECKLIST = "qc_checklist"
const val ROUTE_QC_START = "qc_start"
const val ROUTE_SCAN_CODE = "scan_code"
const val ROUTE_WORK_ITEM_SUMMARY = "work_item_summary"
const val ROUTE_TIMELINE = "timeline"
const val ROUTE_AR_VIEW = "ar_view"
const val ROUTE_SUPERVISOR_DASHBOARD = "supervisor_dashboard"
const val ROUTE_WORK_ITEM_DETAIL = "work_item_detail"

fun workItemSummaryRoute(workItemId: String): String =
    "$ROUTE_WORK_ITEM_SUMMARY?workItemId=${Uri.encode(workItemId)}"

fun qcStartRoute(workItemId: String): String =
    "$ROUTE_QC_START?workItemId=${Uri.encode(workItemId)}"

fun qcChecklistRoute(workItemId: String): String =
    "$ROUTE_QC_CHECKLIST?workItemId=${Uri.encode(workItemId)}"

fun arViewRoute(workItemId: String): String =
    "$ROUTE_AR_VIEW?workItemId=${Uri.encode(workItemId)}"

fun workItemDetailRoute(workItemId: String): String =
