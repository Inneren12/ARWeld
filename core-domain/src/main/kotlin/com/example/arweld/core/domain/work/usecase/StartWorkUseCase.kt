package com.example.arweld.core.domain.work.usecase

/**
 * Records that work has begun for a claimed item.
 */
fun interface StartWorkUseCase {
    suspend operator fun invoke(workItemId: String)
}
