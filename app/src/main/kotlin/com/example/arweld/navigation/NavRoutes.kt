package com.example.arweld.navigation

import android.net.Uri
import com.example.arweld.core.domain.state.WorkStatus

const val ROUTE_AUTH_GRAPH = "auth_graph"
const val ROUTE_MAIN_GRAPH = "main_graph"
const val ROUTE_SPLASH = "splash"
const val ROUTE_LOGIN = "login"
const val ROUTE_HOME = "home"
const val ROUTE_ASSEMBLER_QUEUE = "assembler_queue"
const val ROUTE_QC_QUEUE = "qc_queue"
const val ROUTE_QC_CHECKLIST = "qc_checklist"
const val ROUTE_QC_START = "qc_start"
const val ROUTE_QC_PASS_CONFIRM = "qc_pass_confirm"
const val ROUTE_QC_FAIL_REASON = "qc_fail_reason"
const val ROUTE_SCAN_CODE = "scan_code"
const val ROUTE_DRAWING_IMPORT = "drawing_import"
const val ROUTE_MANUAL_EDITOR = "manual_editor"
const val ROUTE_WORK_ITEM_SUMMARY = "work_item_summary"
const val ROUTE_TIMELINE = "timeline"
const val ROUTE_AR_VIEW = "ar_view"
const val ROUTE_SUPERVISOR_DASHBOARD = "supervisor_dashboard"
const val ROUTE_SUPERVISOR_WORK_LIST = "supervisor_work_list"
const val ROUTE_WORK_ITEM_DETAIL = "work_item_detail"
const val ROUTE_EXPORT_CENTER = "export_center"
const val ROUTE_REPORTS = "reports"
const val ROUTE_OFFLINE_QUEUE = "offline_queue"

fun workItemSummaryRoute(workItemId: String): String =
    "$ROUTE_WORK_ITEM_SUMMARY?workItemId=${Uri.encode(workItemId)}"

fun qcStartRoute(workItemId: String, code: String? = null): String {
    val codeParam = code?.let { "&code=${Uri.encode(it)}" } ?: ""
    return "$ROUTE_QC_START?workItemId=${Uri.encode(workItemId)}$codeParam"
}

fun qcChecklistRoute(workItemId: String, code: String? = null): String {
    val codeParam = code?.let { "&code=${Uri.encode(it)}" } ?: ""
    return "$ROUTE_QC_CHECKLIST?workItemId=${Uri.encode(workItemId)}$codeParam"
}

fun qcPassConfirmRoute(
    workItemId: String,
    checklist: String,
    code: String? = null,
): String {
    val codeParam = code?.let { "&code=${Uri.encode(it)}" } ?: ""
    val checklistParam = Uri.encode(checklist)
    return "$ROUTE_QC_PASS_CONFIRM?workItemId=${Uri.encode(workItemId)}$codeParam&checklist=$checklistParam"
}

fun qcFailReasonRoute(
    workItemId: String,
    checklist: String,
    code: String? = null,
): String {
    val codeParam = code?.let { "&code=${Uri.encode(it)}" } ?: ""
    val checklistParam = Uri.encode(checklist)
    return "$ROUTE_QC_FAIL_REASON?workItemId=${Uri.encode(workItemId)}$codeParam&checklist=$checklistParam"
}

fun arViewRoute(workItemId: String): String =
    "$ROUTE_AR_VIEW?workItemId=${Uri.encode(workItemId)}"

fun workItemDetailRoute(workItemId: String): String =
    "$ROUTE_WORK_ITEM_DETAIL/$workItemId"

fun supervisorWorkListRoute(status: WorkStatus? = null): String {
    val statusParam = status?.let { "?status=${Uri.encode(it.name)}" } ?: ""
    return "$ROUTE_SUPERVISOR_WORK_LIST$statusParam"
}
