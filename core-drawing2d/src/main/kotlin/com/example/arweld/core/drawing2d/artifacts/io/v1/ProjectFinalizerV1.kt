package com.example.arweld.core.drawing2d.artifacts.io.v1

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.core.drawing2d.artifacts.v1.ManifestV1
import com.example.arweld.core.drawing2d.artifacts.v1.ProjectCompleteMarkerV1
import com.example.arweld.core.drawing2d.crypto.Sha256V1
import java.io.File

class ProjectFinalizerV1(
    private val manifestWriter: ManifestWriterV1 = ManifestWriterV1(),
    private val checksumsWriter: ChecksumsWriterV1 = ChecksumsWriterV1(),
    private val storeFactory: (File) -> ArtifactStoreV1 = ::FileArtifactStoreV1,
) {
    fun finalize(projectDir: File, artifacts: List<ArtifactEntryV1>): FinalizeOutcomeV1 {
        val baseArtifacts = artifacts
            .filterNot { it.kind == ArtifactKindV1.MANIFEST_JSON || it.kind == ArtifactKindV1.CHECKSUMS_SHA256 }
            .filterNot { it.relPath == ProjectLayoutV1.PROJECT_COMPLETE_JSON }
        val dedupeResult = dedupeByRelPath(baseArtifacts)
        val deduped = when (dedupeResult) {
            is DedupeResult.Success -> dedupeResult.artifacts
            is DedupeResult.Duplicate -> {
                return FinalizeOutcomeV1.Failure(
                    FinalizeFailureV1(
                        code = FinalizeFailureCodeV1.DUPLICATE_RELPATH,
                        relPath = dedupeResult.relPath,
                    ),
                )
            }
        }
        val store = storeFactory(projectDir)
        val markerEntry = writeMarker(store, projectDir.name)
        val artifactsForManifest = deduped + markerEntry
        val manifestEntry = manifestWriter.write(
            projectDir,
            ManifestV1(
                projectId = projectDir.name,
                artifacts = artifactsForManifest,
            ),
        )
        val artifactsForChecksums = artifactsForManifest + manifestEntry
        val checksumsEntry = checksumsWriter.write(projectDir, artifactsForChecksums)
        val finalArtifacts = artifactsForChecksums + checksumsEntry
        val verificationFailure = verifyArtifacts(projectDir, finalArtifacts)
        return verificationFailure ?: FinalizeOutcomeV1.Success(finalArtifacts)
    }

    private fun writeMarker(store: ArtifactStoreV1, projectId: String): ArtifactEntryV1 {
        val marker = ProjectCompleteMarkerV1(projectId = projectId)
        val json = Drawing2DJson.encodeToString(marker)
        return store.writeText(
            kind = ArtifactKindV1.PROJECT_COMPLETE,
            relPath = ProjectLayoutV1.PROJECT_COMPLETE_JSON,
            text = json,
            mime = "application/json",
        )
    }

    private fun dedupeByRelPath(artifacts: List<ArtifactEntryV1>): DedupeResult {
        val seen = LinkedHashMap<String, ArtifactEntryV1>(artifacts.size)
        for (entry in artifacts) {
            if (seen.containsKey(entry.relPath)) {
                return DedupeResult.Duplicate(entry.relPath)
            }
            seen[entry.relPath] = entry
        }
        return DedupeResult.Success(seen.values.toList())
    }

    private fun verifyArtifacts(projectDir: File, artifacts: List<ArtifactEntryV1>): FinalizeOutcomeV1? {
        for (entry in artifacts) {
            val file = File(projectDir, entry.relPath)
            if (!file.exists()) {
                return FinalizeOutcomeV1.Failure(
                    FinalizeFailureV1(
                        code = FinalizeFailureCodeV1.MISSING_FILE,
                        relPath = entry.relPath,
                    ),
                )
            }
            val actualSha = Sha256V1.hashFile(file.toPath())
            if (actualSha != entry.sha256) {
                return FinalizeOutcomeV1.Failure(
                    FinalizeFailureV1(
                        code = FinalizeFailureCodeV1.SHA_MISMATCH,
                        relPath = entry.relPath,
                    ),
                )
            }
        }
        return null
    }

    private sealed class DedupeResult {
        data class Success(val artifacts: List<ArtifactEntryV1>) : DedupeResult()
        data class Duplicate(val relPath: String) : DedupeResult()
    }
}

enum class FinalizeFailureCodeV1 {
    MISSING_FILE,
    SHA_MISMATCH,
    DUPLICATE_RELPATH,
}

data class FinalizeFailureV1(
    val code: FinalizeFailureCodeV1,
    val relPath: String? = null,
)

sealed class FinalizeOutcomeV1 {
    data class Success(val artifacts: List<ArtifactEntryV1>) : FinalizeOutcomeV1()
    data class Failure(val failure: FinalizeFailureV1) : FinalizeOutcomeV1()
}
