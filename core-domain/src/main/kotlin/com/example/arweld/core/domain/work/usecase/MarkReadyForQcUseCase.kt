package com.example.arweld.core.domain.work.usecase

/**
 * Marks that the assembler considers the work ready for quality control.
 */
fun interface MarkReadyForQcUseCase {
    suspend operator fun invoke(workItemId: String)
}
