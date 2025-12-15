package com.example.arweld.core.data.work

import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.work.ResolveWorkItemByCodeUseCase
import com.example.arweld.core.domain.work.WorkRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResolveWorkItemByCodeUseCaseImpl @Inject constructor(
    private val workRepository: WorkRepository,
) : ResolveWorkItemByCodeUseCase {
    override suspend fun invoke(code: String): WorkItem? {
        return workRepository.getWorkItemByCode(code)
    }
}
