package com.example.arweld.feature.work.viewmodel

import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.model.User
import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.model.WorkItemType
import com.example.arweld.core.domain.state.QcStatus
import com.example.arweld.core.domain.state.WorkItemState
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.core.domain.work.WorkRepository
import com.example.arweld.core.domain.work.usecase.ClaimWorkUseCase
import com.example.arweld.core.domain.work.usecase.MarkReadyForQcUseCase
import com.example.arweld.core.domain.work.usecase.StartWorkUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkItemSummaryViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load by code uses canonical work item id for state and actions`() = scope.runTest {
        val repository = RecordingWorkRepository()
        val claimUseCase = RecordingUseCase()
        val startUseCase = RecordingUseCase()
        val readyForQcUseCase = RecordingUseCase()
        val viewModel = WorkItemSummaryViewModel(
            authRepository = FakeAuthRepository(),
            workRepository = repository,
            claimWorkUseCase = ClaimWorkUseCase { claimUseCase.record(it) },
            startWorkUseCase = StartWorkUseCase { startUseCase.record(it) },
            markReadyForQcUseCase = MarkReadyForQcUseCase { readyForQcUseCase.record(it) },
        )

        viewModel.load("CODE-123")
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNotNull(uiState.workItem)
        assertEquals("WID-1", uiState.workItem?.id)
        assertEquals(listOf("WID-1"), repository.stateRequests)

        viewModel.onClaimWork()
        advanceUntilIdle()

        assertEquals(listOf("WID-1"), claimUseCase.invocations)
        assertEquals(listOf("WID-1", "WID-1"), repository.stateRequests)

        viewModel.onStartWork()
        viewModel.onMarkReadyForQc()
        advanceUntilIdle()

        assertEquals(listOf("WID-1", "WID-1", "WID-1", "WID-1"), repository.stateRequests)
        assertEquals(listOf("WID-1"), startUseCase.invocations)
        assertEquals(listOf("WID-1"), readyForQcUseCase.invocations)
    }
}

private class RecordingWorkRepository : WorkRepository {
    val stateRequests = mutableListOf<String>()

    override suspend fun getByCode(code: String): WorkItem? {
        return if (code == "CODE-123") {
            WorkItem(
                id = "WID-1",
                code = code,
                type = WorkItemType.PART,
                description = "Test work item",
                createdAt = 0L,
            )
        } else {
            null
        }
    }

    override suspend fun getById(id: String): WorkItem? = null

    override suspend fun getWorkItemState(workItemId: String): WorkItemState {
        stateRequests += workItemId
        return WorkItemState(
            status = WorkStatus.NEW,
            lastEvent = null,
            currentAssigneeId = null,
            qcStatus = QcStatus.NOT_STARTED,
            readyForQcSince = null,
        )
    }

    override suspend fun listByStatus(status: WorkStatus) = emptyList<WorkItemState>()

    override suspend fun listMyQueue(userId: String) = emptyList<WorkItemState>()

    override suspend fun listQcQueue() = emptyList<WorkItemState>()
}

private class FakeAuthRepository : AuthRepository {
    override suspend fun loginMock(role: com.example.arweld.core.domain.model.Role): User {
        throw UnsupportedOperationException()
    }

    override suspend fun availableUsers(): List<User> = emptyList()

    override suspend fun loginWithUserId(userId: String): User {
        throw UnsupportedOperationException()
    }

    override suspend fun currentUser(): User? = null

    override suspend fun logout() {
        // no-op
    }
}

private class RecordingUseCase {
    val invocations = mutableListOf<String>()

    fun record(workItemId: String) {
        invocations += workItemId
    }
}
