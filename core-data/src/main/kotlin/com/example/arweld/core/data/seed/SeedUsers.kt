package com.example.arweld.core.data.seed

import com.example.arweld.core.data.db.entity.UserEntity

/**
 * Mock user records used to seed the local database for MVP flows.
 */
object SeedUsers {
    val users: List<UserEntity> = listOf(
        UserEntity(
            id = "u-asm-1",
            name = "Assembler 1",
            role = "ASSEMBLER",
            lastSeenAt = null,
        ),
        UserEntity(
            id = "u-asm-2",
            name = "Assembler 2",
            role = "ASSEMBLER",
            lastSeenAt = null,
        ),
        UserEntity(
            id = "u-qc-1",
            name = "QC 1",
            role = "QC",
            lastSeenAt = null,
        ),
        UserEntity(
            id = "u-qc-2",
            name = "QC 2",
            role = "QC",
            lastSeenAt = null,
        ),
        UserEntity(
            id = "u-sup-1",
            name = "Supervisor 1",
            role = "SUPERVISOR",
            lastSeenAt = null,
        ),
        UserEntity(
            id = "u-dir-1",
            name = "Director 1",
            role = "DIRECTOR",
            lastSeenAt = null,
        ),
    )
}
