package com.example.arweld.feature.supervisor.export

data class EvidenceFileDescriptor(
    val evidenceId: String?,
    val workItemId: String?,
    val kind: String?,
    val uri: String,
    val createdAt: Long,
    val sizeBytes: Long,
) {
    companion object {
        val ORDERING: Comparator<EvidenceFileDescriptor> = compareBy<EvidenceFileDescriptor> {
            it.workItemId.orEmpty()
        }.thenBy {
            it.evidenceId.orEmpty()
        }.thenBy {
            it.kind.orEmpty()
        }.thenBy {
            it.createdAt
        }.thenBy {
            it.uri
        }
    }
}

data class MissingEvidenceFile(
    val descriptor: EvidenceFileDescriptor,
    val expectedPath: String,
    val reason: String,
)

data class EvidenceZipWriteResult(
    val fileCount: Int,
    val bytesWritten: Long,
    val missingFiles: List<MissingEvidenceFile>,
)
