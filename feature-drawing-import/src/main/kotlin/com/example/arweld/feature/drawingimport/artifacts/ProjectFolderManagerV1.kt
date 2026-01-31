package com.example.arweld.feature.drawingimport.artifacts

import android.content.Context
import com.example.arweld.core.drawing2d.artifacts.io.v1.ArtifactsRootPolicyV1
import com.example.arweld.core.drawing2d.artifacts.io.v1.RetentionPolicyV1
import com.example.arweld.core.drawing2d.artifacts.io.v1.SafeDeleteV1
import com.example.arweld.core.drawing2d.artifacts.io.v1.StagingLockV1
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

data class ProjectInfoV1(
    val projectId: String,
    val hasManifest: Boolean,
    val hasChecksums: Boolean,
    val sizeBytes: Long?,
    val lastModifiedUtc: Long?,
)

sealed class DeleteOutcomeV1 {
    data class Success(val projectId: String) : DeleteOutcomeV1()
    data class NotFound(val projectId: String) : DeleteOutcomeV1()
    data class Rejected(val projectId: String, val reason: String) : DeleteOutcomeV1()
    data class Failure(val projectId: String, val reason: String) : DeleteOutcomeV1()
}

data class CleanupReportV1(
    val stagingDeleted: List<String>,
    val stagingSkipped: List<String>,
    val retentionDeleted: List<String>,
    val retentionSkipped: List<String>,
) {
    val stagingDeletedCount: Int get() = stagingDeleted.size
    val retentionDeletedCount: Int get() = retentionDeleted.size
}

class ProjectFolderManagerV1(
    private val rootDir: File,
    private val policy: ArtifactsRootPolicyV1 = ArtifactsRootPolicyV1(),
    private val retention: RetentionPolicyV1 = RetentionPolicyV1(),
) {
    private val logger = Logger.getLogger(ProjectFolderManagerV1::class.java.name)

    constructor(
        context: Context,
        policy: ArtifactsRootPolicyV1 = ArtifactsRootPolicyV1(),
        retention: RetentionPolicyV1 = RetentionPolicyV1(),
    ) : this(
        rootDir = File(context.filesDir, "artifacts"),
        policy = policy,
        retention = retention,
    )

    fun artifactsRoot(): File {
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }
        return rootDir
    }

    fun finalDir(projectId: String): File {
        return File(File(artifactsRoot(), policy.subdir), projectId)
    }

    fun stagingDir(projectId: String): File {
        return File(File(artifactsRoot(), ".staging"), projectId)
    }

    fun listProjects(): List<ProjectInfoV1> {
        val projectsRoot = File(artifactsRoot(), policy.subdir)
        if (!projectsRoot.exists()) return emptyList()
        return projectsRoot.listFiles()
            ?.filter { it.isDirectory }
            ?.map { dir ->
                ProjectInfoV1(
                    projectId = dir.name,
                    hasManifest = File(dir, ProjectLayoutV1.MANIFEST_JSON).exists(),
                    hasChecksums = File(dir, ProjectLayoutV1.CHECKSUMS_SHA256).exists(),
                    sizeBytes = calculateSizeBytes(dir),
                    lastModifiedUtc = dir.lastModified().takeIf { it > 0L },
                )
            }
            ?.sortedBy { it.projectId }
            .orEmpty()
    }

    fun deleteProject(projectId: String): DeleteOutcomeV1 {
        val target = finalDir(projectId)
        if (!target.exists()) {
            return DeleteOutcomeV1.NotFound(projectId)
        }
        return try {
            SafeDeleteV1.deleteRecursivelySafe(artifactsRoot(), target)
            DeleteOutcomeV1.Success(projectId)
        } catch (error: IllegalArgumentException) {
            DeleteOutcomeV1.Rejected(projectId, error.message ?: "Rejected")
        } catch (error: Exception) {
            DeleteOutcomeV1.Failure(projectId, error.message ?: "Delete failed")
        }
    }

    fun cleanupOrphans(): CleanupReportV1 {
        val root = artifactsRoot()
        val nowMs = System.currentTimeMillis()
        val minAgeMs = TimeUnit.HOURS.toMillis(retention.minAgeHoursForDelete.coerceAtLeast(0).toLong())
        val stagingRoot = File(root, ".staging")
        val stagingDeleted = mutableListOf<String>()
        val stagingSkipped = mutableListOf<String>()
        if (stagingRoot.exists()) {
            stagingRoot.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
                val lockPresent = StagingLockV1.lockFile(dir).exists()
                val lastModified = dir.lastModified().takeIf { it > 0L }
                val isOld = lastModified?.let { nowMs - it >= minAgeMs } ?: false
                if (!lockPresent || isOld) {
                    SafeDeleteV1.deleteRecursivelySafe(root, dir)
                    stagingDeleted.add(relativePath(root, dir))
                } else {
                    stagingSkipped.add(relativePath(root, dir))
                }
            }
        }

        val retentionDeleted = mutableListOf<String>()
        val retentionSkipped = mutableListOf<String>()
        val projectsRoot = File(root, policy.subdir)
        val projects = projectsRoot.listFiles()?.filter { it.isDirectory }.orEmpty()
        val sortedByAge = projects.map { dir ->
            ProjectCandidate(dir, dir.lastModified().takeIf { it > 0L })
        }.sortedBy { it.lastModified ?: Long.MAX_VALUE }
        val keepCount = retention.keepLastN.coerceAtLeast(0)
        val candidates = if (sortedByAge.size > keepCount) {
            sortedByAge.dropLast(keepCount)
        } else {
            emptyList()
        }
        for (candidate in candidates) {
            val lastModified = candidate.lastModified
            val isOldEnough = lastModified?.let { nowMs - it >= minAgeMs } ?: false
            if (isOldEnough) {
                SafeDeleteV1.deleteRecursivelySafe(root, candidate.dir)
                retentionDeleted.add(relativePath(root, candidate.dir))
            } else {
                retentionSkipped.add(relativePath(root, candidate.dir))
            }
        }

        val report = CleanupReportV1(
            stagingDeleted = stagingDeleted,
            stagingSkipped = stagingSkipped,
            retentionDeleted = retentionDeleted,
            retentionSkipped = retentionSkipped,
        )
        logCleanup(report)
        return report
    }

    private fun calculateSizeBytes(dir: File): Long? {
        return try {
            Files.walk(dir.toPath()).use { stream ->
                stream.filter { Files.isRegularFile(it) }.mapToLong { Files.size(it) }.sum()
            }
        } catch (error: Exception) {
            null
        }
    }

    private fun relativePath(root: File, target: File): String {
        return try {
            root.canonicalFile.toPath().relativize(target.canonicalFile.toPath()).toString()
        } catch (error: Exception) {
            target.name
        }
    }

    private fun logCleanup(report: CleanupReportV1) {
        logger.info(
            "ProjectFolderManagerV1 cleanup " +
                "stagingDeleted=${report.stagingDeletedCount} " +
                "retentionDeleted=${report.retentionDeletedCount}",
        )
        if (report.stagingDeleted.isNotEmpty()) {
            logger.info("ProjectFolderManagerV1 stagingDeleted=${report.stagingDeleted}")
        }
        if (report.retentionDeleted.isNotEmpty()) {
            logger.info("ProjectFolderManagerV1 retentionDeleted=${report.retentionDeleted}")
        }
    }

    companion object {
        fun fromBaseRoot(
            baseRoot: File,
            policy: ArtifactsRootPolicyV1 = ArtifactsRootPolicyV1(),
            retention: RetentionPolicyV1 = RetentionPolicyV1(),
        ): ProjectFolderManagerV1 = ProjectFolderManagerV1(
            rootDir = baseRoot,
            policy = policy,
            retention = retention,
        )
    }

    private data class ProjectCandidate(
        val dir: File,
        val lastModified: Long?,
    )
}
