package com.example.arweld.core.drawing2d.artifacts.io.v1

import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectTransactionV1Test {
    @Test
    fun `commit moves staging to final`() = runBlocking {
        val root = createTempDir(prefix = "artifacts-root")
        val transaction = ProjectTransactionV1(root, "project-123")
        val store = transaction.open()
        store.writeText(
            kind = ArtifactKindV1.MANIFEST_JSON,
            relPath = "manifest.json",
            text = "{}",
        )

        transaction.commit()

        val finalDir = File(root, "project-123")
        val stagingDir = File(File(root, ".staging"), "project-123")
        assertTrue(finalDir.exists())
        assertTrue(File(finalDir, "manifest.json").exists())
        assertFalse(stagingDir.exists())
    }

    @Test
    fun `rollback cleans staging after write failure`() {
        val root = createTempDir(prefix = "artifacts-root")
        val injector = CountingFaultInjector(limit = 1)
        val transaction = ProjectTransactionV1(
            root,
            "project-rollback",
            storeFactory = { dir -> FileArtifactStoreV1(dir, injector) },
        )
        val store = transaction.open()

        try {
            store.writeText(
                kind = ArtifactKindV1.MANIFEST_JSON,
                relPath = "manifest.json",
                text = "{}",
            )
            store.writeText(
                kind = ArtifactKindV1.CAPTURE_META,
                relPath = "capture.json",
                text = "{}",
            )
        } catch (_: IllegalStateException) {
            transaction.rollback()
        }

        val finalDir = File(root, "project-rollback")
        val stagingDir = File(File(root, ".staging"), "project-rollback")
        assertFalse(finalDir.exists())
        assertFalse(stagingDir.exists())
    }

    private class CountingFaultInjector(
        private val limit: Int,
    ) : ArtifactWriteFaultInjectorV1 {
        private var count = 0

        override fun onWrite(kind: ArtifactKindV1, relPath: String, byteCount: Int) {
            count += 1
            if (count > limit) {
                throw IllegalStateException("Injected failure after $limit writes.")
            }
        }
    }
}
