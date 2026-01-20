package com.example.arweld.core.domain.reporting

data class ReportPeriod(
    val startMillis: Long,
    val endMillis: Long,
) {
    fun contains(timestampMillis: Long): Boolean {
        return timestampMillis in startMillis..endMillis
    }
}
