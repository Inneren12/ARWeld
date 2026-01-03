package com.example.arweld.core.domain.policy

import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.core.domain.evidence.EvidenceRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QcEvidencePolicyTest {

    private val evidenceRepository = FakeEvidenceRepository()
    private val policy = QcEvidencePolicy(evidenceRepository)

    @Test
    fun `policy fails when both evidence types are missing`() {
        val state = runBlockingEvaluate("work-1")

        assertFalse(state.satisfied)
        assertEquals(setOf(EvidenceKind.PHOTO, EvidenceKind.AR_SCREENSHOT), state.missing)
    }

    @Test
    fun `policy fails when one evidence type is missing`() {
        evidenceRepository.seedCounts(
            mapOf(EvidenceKind.PHOTO to 1)
        )

        val state = runBlockingEvaluate("work-2")

        assertFalse(state.satisfied)
        assertEquals(setOf(EvidenceKind.AR_SCREENSHOT), state.missing)
    }

    @Test
    fun `policy passes when both evidence types are present`() {
        evidenceRepository.seedCounts(
            mapOf(
                EvidenceKind.PHOTO to 2,
                EvidenceKind.AR_SCREENSHOT to 1,
            )
        )

        val state = runBlockingEvaluate("work-3")

        assertTrue(state.satisfied)
        assertTrue(state.missing.isEmpty())
    }

    private fun runBlockingEvaluate(workItemId: String): QcEvidencePolicy.PolicyState {
        return kotlinx.coroutines.runBlocking { policy.evaluate(workItemId) }
    }
}

private class FakeEvidenceRepository : EvidenceRepository {
    private var counts: Map<EvidenceKind, Int> = emptyMap()

    fun seedCounts(values: Map<EvidenceKind, Int>) {
        counts = values
    }

    override suspend fun saveEvidence(evidence: com.example.arweld.core.domain.evidence.Evidence) = Unit
    override suspend fun savePhoto(workItemId: String, eventId: String, file: java.io.File) =
        error("Not needed")
    override suspend fun saveArScreenshot(
        workItemId: String,
        eventId: String,
        uri: android.net.Uri,
        meta: com.example.arweld.core.domain.evidence.ArScreenshotMeta,
    ) = error("Not needed")
    override suspend fun saveAll(evidenceList: List<com.example.arweld.core.domain.evidence.Evidence>) = Unit
    override suspend fun getEvidenceForEvent(eventId: String) = emptyList<com.example.arweld.core.domain.evidence.Evidence>()
    override suspend fun getEvidenceForWorkItem(workItemId: String) =
        emptyList<com.example.arweld.core.domain.evidence.Evidence>()

    override suspend fun countsByKindForWorkItem(workItemId: String): Map<EvidenceKind, Int> = counts
}
