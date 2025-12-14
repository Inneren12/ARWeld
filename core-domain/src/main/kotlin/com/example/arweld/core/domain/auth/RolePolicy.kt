package com.example.arweld.core.domain.auth

import com.example.arweld.core.domain.model.Role

/**
 * Central policy for role-based permissions.
 *
 * Defines which roles have which permissions according to business rules:
 * - ASSEMBLER: can claim work, but cannot perform QC actions or view all work
 * - QC: can start QC, pass/fail QC decisions
 * - SUPERVISOR: can view all work, perform QC actions (pass/fail/start)
 * - DIRECTOR: full access to all permissions
 */
object RolePolicy {

    private val rolePermissions: Map<Role, Set<Permission>> = mapOf(
        Role.ASSEMBLER to setOf(
            Permission.CLAIM_WORK
        ),
        Role.QC to setOf(
            Permission.START_QC,
            Permission.PASS_QC,
            Permission.FAIL_QC,
            Permission.CLAIM_WORK  // QC can claim work for QC-only operations
        ),
        Role.SUPERVISOR to setOf(
            Permission.VIEW_ALL,
            Permission.START_QC,
            Permission.PASS_QC,
            Permission.FAIL_QC
        ),
        Role.DIRECTOR to setOf(
            // Director has all permissions
            Permission.CLAIM_WORK,
            Permission.START_QC,
            Permission.PASS_QC,
            Permission.FAIL_QC,
            Permission.VIEW_ALL
        )
    )

    /**
     * Checks if a given role has a specific permission.
     *
     * @param role The role to check
     * @param permission The permission to verify
     * @return true if the role has the permission, false otherwise
     */
    fun hasPermission(role: Role, permission: Permission): Boolean {
        return rolePermissions[role]?.contains(permission) ?: false
    }
}

/**
 * Extension function for convenient permission checking on Role.
 *
 * Usage: `role.hasPermission(Permission.PASS_QC)`
 *
 * @param permission The permission to check
 * @return true if this role has the permission, false otherwise
 */
fun Role.hasPermission(permission: Permission): Boolean {
    return RolePolicy.hasPermission(this, permission)
}
