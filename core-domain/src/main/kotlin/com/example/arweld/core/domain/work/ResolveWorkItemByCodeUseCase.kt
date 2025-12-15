package com.example.arweld.core.domain.work

import com.example.arweld.core.domain.model.WorkItem

/**
 * Resolves a scanned barcode/QR/NFC code into a [WorkItem].
 */
fun interface ResolveWorkItemByCodeUseCase {
    suspend operator fun invoke(code: String): WorkItem?
}
