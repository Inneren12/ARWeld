package com.example.arweld.core.domain.auth

import com.example.arweld.core.domain.model.Role
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RolePolicyTest {

    @Test
    fun `qc can pass but assembler cannot`() {
        assertThat(Role.QC.hasPermission(Permission.PASS_QC)).isTrue()
        assertThat(Role.ASSEMBLER.hasPermission(Permission.PASS_QC)).isFalse()
    }

    @Test
    fun `director retains all permissions`() {
        Permission.values().forEach { permission ->
            assertThat(Role.DIRECTOR.hasPermission(permission)).isTrue()
        }
    }
}
