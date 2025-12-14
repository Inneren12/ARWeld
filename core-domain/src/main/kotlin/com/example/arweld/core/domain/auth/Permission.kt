package com.example.arweld.core.domain.auth

/**
 * Permissions in the ARWeld system.
 * Defines specific actions that can be performed by users with appropriate roles.
 */
enum class Permission {
    CLAIM_WORK,
    START_QC,
    PASS_QC,
    FAIL_QC,
    VIEW_ALL
    // TODO: extend later (e.g. MANAGE_USERS, CONFIGURE_QC_POLICY, etc.)
}
