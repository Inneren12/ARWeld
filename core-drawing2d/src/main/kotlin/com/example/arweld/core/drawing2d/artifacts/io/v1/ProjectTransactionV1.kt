package com.example.arweld.core.drawing2d.artifacts.io.v1

import java.io.File
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.logging.Level
import java.util.logging.Logger

class ProjectTransactionV1(
    private val artifactsRoot: File,
    private val projectId: String,
    private val policy: ArtifactsRootPolicyV1 = ArtifactsRootPolicyV1(),
    private val storeFactory: (File) -> ArtifactStoreV1 = ::FileArtifactStoreV1,
) {
    private val logger = Logger.getLogger(ProjectTransactionV1::class.java.name)

    val stagingDir: File = File(File(artifactsRoot, ".staging"), projectId)
    val finalDir: File = File(File(artifactsRoot, policy.subdir), projectId)

    fun open(): ArtifactStoreV1 {
        logger.info("ProjectTransactionV1 open projectId=$projectId staging=${stagingDir.path}")
        if (stagingDir.exists() && !stagingDir.deleteRecursively()) {
            throw IOException("Failed to clear staging directory: ${stagingDir.path}")
        }
        if (!stagingDir.exists() && !stagingDir.mkdirs()) {
            throw IOException("Failed to create staging directory: ${stagingDir.path}")
        }
        StagingLockV1.touch(stagingDir)
        if (finalDir.exists()) {
            copyDirectory(finalDir.toPath(), stagingDir.toPath())
        }
        return storeFactory(stagingDir)
    }

    suspend fun commit() {
        logger.info("ProjectTransactionV1 commit projectId=$projectId from=${stagingDir.path} to=${finalDir.path}")
        if (!stagingDir.exists()) {
            throw IOException("Staging directory missing: ${stagingDir.path}")
        }
        StagingLockV1.clear(stagingDir)
        finalDir.parentFile?.let { parent ->
            if (!parent.exists() && !parent.mkdirs()) {
                throw IOException("Failed to create final directory parent: ${parent.path}")
            }
        }
        try {
            Files.move(
                stagingDir.toPath(),
                finalDir.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (error: AtomicMoveNotSupportedException) {
            logger.log(
                Level.WARNING,
                "ATOMIC_MOVE not supported; falling back to non-atomic move for projectId=$projectId",
                error,
            )
            Files.move(
                stagingDir.toPath(),
                finalDir.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }

    fun rollback() {
        logger.info("ProjectTransactionV1 rollback projectId=$projectId staging=${stagingDir.path}")
        StagingLockV1.clear(stagingDir)
        if (stagingDir.exists() && !stagingDir.deleteRecursively()) {
            throw IOException("Failed to delete staging directory: ${stagingDir.path}")
        }
    }

    private fun copyDirectory(source: Path, target: Path) {
        Files.walk(source).use { stream ->
            stream.forEach { path ->
                val relative = source.relativize(path)
                val targetPath = target.resolve(relative)
                if (Files.isDirectory(path)) {
                    Files.createDirectories(targetPath)
                } else {
                    Files.copy(
                        path,
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES,
                    )
                }
            }
        }
    }
}
