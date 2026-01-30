package com.example.arweld.core.drawing2d.validation

/**
 * Canonical ordering for Drawing2D violations to ensure deterministic output.
 *
 * Order:
 * 1) Severity (ERROR > WARN > INFO)
 * 2) Path (lexicographic)
 * 3) Code (lexicographic)
 */
object ViolationOrdering {
    val canonicalComparator: Comparator<ViolationV1> =
        compareBy<ViolationV1> { severityRank(it.severity) }
            .thenBy { it.path }
            .thenBy { it.code }

    private fun severityRank(severity: SeverityV1): Int =
        when (severity) {
            SeverityV1.ERROR -> 0
            SeverityV1.WARN -> 1
            SeverityV1.INFO -> 2
        }
}

fun List<ViolationV1>.canonicalSorted(): List<ViolationV1> =
    sortedWith(ViolationOrdering.canonicalComparator)
