package com.example.arweld.core.data.reporting

import com.example.arweld.core.domain.reporting.FailReasonAggregation
import com.example.arweld.core.domain.reporting.FailReasonCount
import com.example.arweld.core.domain.reporting.ReportPeriod
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FailReasonAggregator @Inject constructor(
    private val reportProvider: ReportProvider,
) {
    suspend operator fun invoke(period: ReportPeriod): List<FailReasonCount> {
        val report = reportProvider.buildReport(period)
        return FailReasonAggregation.aggregateFromEvents(report.events)
    }
}
