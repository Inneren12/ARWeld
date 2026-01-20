package com.example.arweld.core.data.reporting

import com.example.arweld.core.domain.reporting.NodeFailStats
import com.example.arweld.core.domain.reporting.ProblematicNodeAggregation
import com.example.arweld.core.domain.reporting.ReportPeriod
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProblematicNodeAggregator @Inject constructor(
    private val reportProvider: ReportProvider,
) {
    suspend operator fun invoke(period: ReportPeriod): List<NodeFailStats> {
        val report = reportProvider.buildReport(period)
        return ProblematicNodeAggregation.aggregateFromReport(report)
    }
}
