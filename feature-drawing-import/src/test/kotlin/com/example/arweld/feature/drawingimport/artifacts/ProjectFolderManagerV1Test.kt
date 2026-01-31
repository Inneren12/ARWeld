package com.example.arweld.feature.drawingimport.artifacts

import com.example.arweld.core.drawing2d.artifacts.io.v1.ArtifactsRootPolicyV1
import com.example.arweld.core.drawing2d.artifacts.io.v1.RetentionPolicyV1
import com.example.arweld.core.drawing2d.artifacts.io.v1.StagingLockV1
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProjectFolderManagerV1Test {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `cleanupOrphans deletes only eligible staging dirs`() {
        val root = tempFolder.newFolder("artifacts-root")
        val manager = ProjectFolderManagerV1(
            rootDir = root,
            retention = RetentionPolicyV1(keepLastN = 999, minAgeHoursForDelete = 999),
        )
        val staleStaging = File(File(root, ".staging"), "stale")
        val keepStaging = File(File(root, ".staging"), "keep")
        staleStaging.mkdirs()
        keepStaging.mkdirs()
        StagingLockV1.touch(keepStaging)

        val report = manager.cleanupOrphans()

        assertFalse(staleStaging.exists())
        assertTrue(keepStaging.exists())
        assertEquals(1, report.stagingDeletedCount)
    }

    @Test
    fun `deleteProject rejects paths outside root`() {
        val root = tempFolder.newFolder("artifacts-root")
        val policy = ArtifactsRootPolicyV1(subdir = "../escape")
        val escapeRoot = File(root.parentFile, "escape")
        val escapeProject = File(escapeRoot, "project-1")
        escapeProject.mkdirs()
        val manager = ProjectFolderManagerV1(
            rootDir = root,
            policy = policy,
        )

        val outcome = manager.deleteProject("project-1")

        assertTrue(outcome is DeleteOutcomeV1.Rejected)
        assertTrue(escapeProject.exists())
    }

    @Test
    fun `retention deletes oldest projects beyond keepLastN`() {
        val root = tempFolder.newFolder("artifacts-root")
        val projectsRoot = File(root, "projects")
        val oldProject = File(projectsRoot, "old")
        val midProject = File(projectsRoot, "mid")
        val newProject = File(projectsRoot, "new")
        oldProject.mkdirs()
        midProject.mkdirs()
        newProject.mkdirs()
        oldProject.setLastModified(1_000L)
        midProject.setLastModified(2_000L)
        newProject.setLastModified(3_000L)

        val manager = ProjectFolderManagerV1(
            rootDir = root,
            retention = RetentionPolicyV1(keepLastN = 1, minAgeHoursForDelete = 0),
        )

        val report = manager.cleanupOrphans()

        assertFalse(oldProject.exists())
        assertFalse(midProject.exists())
        assertTrue(newProject.exists())
        assertEquals(2, report.retentionDeletedCount)
    }
}
