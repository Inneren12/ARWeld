package com.example.arweld.core.domain.work.usecase

/**
 * Claims a work item for the current assembler.
 */
fun interface ClaimWorkUseCase {
    suspend operator fun invoke(workItemId: String)
}
