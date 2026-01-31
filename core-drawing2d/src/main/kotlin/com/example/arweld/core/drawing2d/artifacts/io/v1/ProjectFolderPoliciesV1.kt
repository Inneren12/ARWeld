package com.example.arweld.core.drawing2d.artifacts.io.v1

import java.io.File

data class ArtifactsRootPolicyV1(
    val subdir: String = "projects",
)

data class RetentionPolicyV1(
    val keepLastN: Int = 20,
    val minAgeHoursForDelete: Int = 24,
)

object StagingLockV1 {
    const val FILE_NAME: String = ".lock"

    fun lockFile(stagingDir: File): File = File(stagingDir, FILE_NAME)

    fun touch(stagingDir: File) {
        val file = lockFile(stagingDir)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.writeText("lock")
        }
        file.setLastModified(System.currentTimeMillis())
    }

    fun clear(stagingDir: File) {
        val file = lockFile(stagingDir)
        if (file.exists()) {
            file.delete()
        }
    }
}

object SafeDeleteV1 {
    fun deleteRecursivelySafe(root: File, target: File) {
        val rootPath = root.canonicalFile.toPath()
        val targetPath = target.canonicalFile.toPath()
        require(targetPath.startsWith(rootPath)) {
            "Refusing to delete outside root: target=${target.path} root=${root.path}"
        }
        if (target.exists()) {
            target.deleteRecursively()
        }
    }
}
