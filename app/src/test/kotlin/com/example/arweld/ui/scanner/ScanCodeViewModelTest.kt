package com.example.arweld.ui.scanner

import com.example.arweld.core.domain.logging.AppLogger
import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.model.WorkItemType
import com.example.arweld.core.domain.work.ResolveWorkItemByCodeUseCase
import com.example.arweld.feature.scanner.ui.ScanCodeResolutionState
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanCodeViewModelTest {

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
    fun `resolve success triggers navigation and resets state`() = scope.runTest {
        val viewModel = ScanCodeViewModel(
            resolveWorkItemByCode = ResolveWorkItemByCodeUseCase { code ->
                WorkItem(
                    id = "WORK-1",
                    code = code,
                    type = WorkItemType.PART,
                    description = "Test work item",
                    createdAt = 0L,
                )
            },
            appLogger = FakeAppLogger(),
        )

        var resolvedId: String? = null
        viewModel.resolveCode("CODE-123") { resolvedId = it }
        advanceUntilIdle()

        assertEquals("WORK-1", resolvedId)
        assertEquals(ScanCodeResolutionState.Idle, viewModel.resolutionState.value)
    }

    @Test
    fun `resolve missing code sets not found state`() = scope.runTest {
        val viewModel = ScanCodeViewModel(
            resolveWorkItemByCode = ResolveWorkItemByCodeUseCase { null },
            appLogger = FakeAppLogger(),
        )

        viewModel.resolveCode("MISSING") { }
        advanceUntilIdle()

        assertNull(viewModel.resolutionState.value as? ScanCodeResolutionState.Error)
        assertEquals(
            ScanCodeResolutionState.NotFound("MISSING"),
            viewModel.resolutionState.value,
        )
    }
}

private class FakeAppLogger : AppLogger {
    override fun logNavigation(route: String) = Unit
    override fun logLoginAttempt(userId: String) = Unit
    override fun logLoginSuccess(user: com.example.arweld.core.domain.model.User) = Unit
    override fun logRepositoryError(operation: String, throwable: Throwable) = Unit
    override fun logUnhandledError(throwable: Throwable) = Unit
}
