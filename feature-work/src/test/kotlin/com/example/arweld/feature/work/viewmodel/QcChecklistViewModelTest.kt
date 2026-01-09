package com.example.arweld.feature.work.viewmodel

import android.net.Uri
import com.example.arweld.core.domain.evidence.ArScreenshotMeta
import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.core.domain.evidence.EvidenceRepository
import com.example.arweld.core.domain.policy.QcEvidencePolicy
import com.example.arweld.core.domain.work.model.QcCheckState
import com.example.arweld.core.domain.work.model.QcChecklistItem
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class QcChecklistViewModelTest {

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
    fun `policy gating reports missing evidence`() = scope.runTest {
        val evidenceRepository = FakeEvidenceRepository(
            counts = mapOf(
                EvidenceKind.PHOTO to 0,
                EvidenceKind.AR_SCREENSHOT to 0,
            ),
        )
        val viewModel = QcChecklistViewModel(
            itemsProvider = FakeChecklistItemsProvider(),
            qcEvidencePolicy = QcEvidencePolicy(evidenceRepository),
            evidenceRepository = evidenceRepository,
        )

        viewModel.initialize("work-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.policySatisfied)
        assertEquals(setOf(EvidenceKind.PHOTO, EvidenceKind.AR_SCREENSHOT), state.missingEvidence)
        assertEquals(0, state.evidenceCounts[EvidenceKind.PHOTO])
    }

    @Test
    fun `policy satisfied when required evidence present`() = scope.runTest {
        val evidenceRepository = FakeEvidenceRepository(
            counts = mapOf(
                EvidenceKind.PHOTO to 1,
                EvidenceKind.AR_SCREENSHOT to 1,
            ),
        )
        val viewModel = QcChecklistViewModel(
            itemsProvider = FakeChecklistItemsProvider(),
            qcEvidencePolicy = QcEvidencePolicy(evidenceRepository),
            evidenceRepository = evidenceRepository,
        )

        viewModel.initialize("work-2")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.policySatisfied)
        assertTrue(state.missingEvidence.isEmpty())
        assertEquals(1, state.evidenceCounts[EvidenceKind.PHOTO])
        assertEquals(1, state.evidenceCounts[EvidenceKind.AR_SCREENSHOT])
    }
}

private class FakeChecklistItemsProvider : QcChecklistItemsProvider {
    override fun defaultItems(): List<QcChecklistItem> {
        return listOf(
            QcChecklistItem(id = "geometry", state = QcCheckState.NA),
            QcChecklistItem(id = "completeness", state = QcCheckState.NA),
            QcChecklistItem(id = "fasteners", state = QcCheckState.NA),
            QcChecklistItem(id = "marking", state = QcCheckState.NA),
            QcChecklistItem(id = "cleanliness", state = QcCheckState.NA),
        )
    }
}

private class FakeEvidenceRepository(
    private val counts: Map<EvidenceKind, Int>,
) : EvidenceRepository {
    override suspend fun saveEvidence(evidence: Evidence) {
        throw UnsupportedOperationException()
    }

    override suspend fun savePhoto(workItemId: String, eventId: String, file: File): Evidence {
        throw UnsupportedOperationException()
    }

    override suspend fun saveArScreenshot(
        workItemId: String,
        eventId: String,
        uri: Uri,
        meta: ArScreenshotMeta,
    ): Evidence {
        throw UnsupportedOperationException()
    }

    override suspend fun saveFileAndRecord(
        workItemId: String,
        eventId: String,
        fileOrUri: Any,
        kind: EvidenceKind,
        arMeta: ArScreenshotMeta?,
    ): Evidence {
        throw UnsupportedOperationException()
    }

    override suspend fun saveAll(evidenceList: List<Evidence>) {
        throw UnsupportedOperationException()
    }

    override suspend fun getEvidenceForEvent(eventId: String): List<Evidence> {
        throw UnsupportedOperationException()
    }

    override suspend fun getEvidenceForWorkItem(workItemId: String): List<Evidence> {
        throw UnsupportedOperationException()
    }

    override suspend fun countsByKindForWorkItem(workItemId: String): Map<EvidenceKind, Int> {
        return counts
    }
}
