package com.example.arweld.core.drawing2d.artifacts.io.v1

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.core.drawing2d.artifacts.v1.ManifestV1
import com.example.arweld.core.drawing2d.artifacts.v1.canonicalize
import java.io.File

class ManifestWriterV1 {
    fun write(baseDir: File, manifest: ManifestV1): ArtifactEntryV1 {
        val store = FileArtifactStoreV1(baseDir)
        val canonicalManifest = manifest.canonicalize()
        val json = Drawing2DJson.encodeToString(canonicalManifest)
        return store.writeText(
            kind = ArtifactKindV1.MANIFEST_JSON,
            relPath = ProjectLayoutV1.MANIFEST_JSON,
            text = json,
            mime = "application/json"
        )
    }
}
