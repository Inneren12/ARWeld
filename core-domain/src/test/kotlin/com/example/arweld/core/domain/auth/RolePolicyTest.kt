package com.example.arweld.core.domain.auth

import com.example.arweld.core.domain.model.Role
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for RolePolicy.
 *
 * Tests verify that each role has the correct permissions according to business rules.
 */
class RolePolicyTest {

    // ASSEMBLER Tests
    @Test
    fun `ASSEMBLER has CLAIM_WORK permission`() {
        assertTrue(Role.ASSEMBLER.hasPermission(Permission.CLAIM_WORK))
    }

    @Test
    fun `ASSEMBLER does not have PASS_QC permission`() {
        assertFalse(Role.ASSEMBLER.hasPermission(Permission.PASS_QC))
    }

    @Test
    fun `ASSEMBLER does not have FAIL_QC permission`() {
        assertFalse(Role.ASSEMBLER.hasPermission(Permission.FAIL_QC))
    }

    @Test
    fun `ASSEMBLER does not have START_QC permission`() {
        assertFalse(Role.ASSEMBLER.hasPermission(Permission.START_QC))
    }

    @Test
    fun `ASSEMBLER does not have VIEW_ALL permission`() {
        assertFalse(Role.ASSEMBLER.hasPermission(Permission.VIEW_ALL))
    }

    // QC Tests
    @Test
    fun `QC has START_QC permission`() {
        assertTrue(Role.QC.hasPermission(Permission.START_QC))
    }

    @Test
    fun `QC has PASS_QC permission`() {
        assertTrue(Role.QC.hasPermission(Permission.PASS_QC))
    }

    @Test
    fun `QC has FAIL_QC permission`() {
        assertTrue(Role.QC.hasPermission(Permission.FAIL_QC))
    }

    @Test
    fun `QC has CLAIM_WORK permission`() {
        assertTrue(Role.QC.hasPermission(Permission.CLAIM_WORK))
    }

    @Test
    fun `QC does not have VIEW_ALL permission`() {
        assertFalse(Role.QC.hasPermission(Permission.VIEW_ALL))
    }

    // SUPERVISOR Tests
    @Test
    fun `SUPERVISOR has VIEW_ALL permission`() {
        assertTrue(Role.SUPERVISOR.hasPermission(Permission.VIEW_ALL))
    }

    @Test
    fun `SUPERVISOR has PASS_QC permission`() {
        assertTrue(Role.SUPERVISOR.hasPermission(Permission.PASS_QC))
    }

    @Test
    fun `SUPERVISOR has FAIL_QC permission`() {
        assertTrue(Role.SUPERVISOR.hasPermission(Permission.FAIL_QC))
    }

    @Test
    fun `SUPERVISOR has START_QC permission`() {
        assertTrue(Role.SUPERVISOR.hasPermission(Permission.START_QC))
    }

    @Test
    fun `SUPERVISOR does not have CLAIM_WORK permission`() {
        assertFalse(Role.SUPERVISOR.hasPermission(Permission.CLAIM_WORK))
    }

    // DIRECTOR Tests
    @Test
    fun `DIRECTOR has all permissions - CLAIM_WORK`() {
        assertTrue(Role.DIRECTOR.hasPermission(Permission.CLAIM_WORK))
    }

    @Test
    fun `DIRECTOR has all permissions - START_QC`() {
        assertTrue(Role.DIRECTOR.hasPermission(Permission.START_QC))
    }

    @Test
    fun `DIRECTOR has all permissions - PASS_QC`() {
        assertTrue(Role.DIRECTOR.hasPermission(Permission.PASS_QC))
    }

    @Test
    fun `DIRECTOR has all permissions - FAIL_QC`() {
        assertTrue(Role.DIRECTOR.hasPermission(Permission.FAIL_QC))
    }

    @Test
    fun `DIRECTOR has all permissions - VIEW_ALL`() {
        assertTrue(Role.DIRECTOR.hasPermission(Permission.VIEW_ALL))
    }

    // RolePolicy.hasPermission method tests
    @Test
    fun `RolePolicy hasPermission returns true for valid role-permission pair`() {
        assertTrue(RolePolicy.hasPermission(Role.ASSEMBLER, Permission.CLAIM_WORK))
    }

    @Test
    fun `RolePolicy hasPermission returns false for invalid role-permission pair`() {
        assertFalse(RolePolicy.hasPermission(Role.ASSEMBLER, Permission.PASS_QC))
    }
}
