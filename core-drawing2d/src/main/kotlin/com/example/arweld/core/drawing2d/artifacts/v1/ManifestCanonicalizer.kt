package com.example.arweld.core.drawing2d.artifacts.v1

/**
 * Canonicalizes a manifest by sorting artifacts deterministically.
 *
 * Sorting order:
 * 1) Artifact kind ordinal
 * 2) relPath lexicographic (no path normalization performed here)
 *
 * Note: relPath normalization (e.g., converting path separators to '/')
 * should happen at write-time, not during canonicalization.
 */
fun ManifestV1.canonicalize(): ManifestV1 {
    val sortedArtifacts = artifacts.sortedWith(
        compareBy<ArtifactEntryV1> { it.kind.ordinal }
            .thenBy { it.relPath }
    )
    return copy(artifacts = sortedArtifacts)
}
