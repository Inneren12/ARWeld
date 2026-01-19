package com.example.arweld.core.domain.model

import kotlinx.serialization.Serializable

/**
 * User roles in the ARWeld system.
 * Each role has different permissions and workflows.
 */
@Serializable
enum class Role {
    ASSEMBLER,
    QC,
    SUPERVISOR,
    DIRECTOR
}
