package com.example.arweld.core.domain.evidence

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EvidenceTest {

    @Test
    fun `can construct evidence for each kind`() {
        val kinds = EvidenceKind.values().toList()

        val evidences = kinds.mapIndexed { index, kind ->
            Evidence(
                id = "id-$index",
                eventId = "event-$index",
                kind = kind,
                uri = "file://evidence/$index",
                sha256 = "hash-$index",
                metaJson = "{\"index\":$index}",
                createdAt = 1_700_000_000_000L + index,
            )
        }

        kinds.zip(evidences).forEach { (kind, evidence) ->
            assertEquals(kind, evidence.kind)
            assertEquals("event-${kinds.indexOf(kind)}", evidence.eventId)
        }
    }

    @Test
    fun `isVisual flags imagery and video evidence`() {
        val photo = Evidence(
            id = "1",
            eventId = "e1",
            kind = EvidenceKind.PHOTO,
            uri = "file://photo",
            sha256 = "hash1",
            metaJson = null,
            createdAt = 1L,
        )
        val measurement = photo.copy(kind = EvidenceKind.MEASUREMENT)

        assertTrue(photo.isVisual())
        assertFalse(measurement.isVisual())
    }
}
