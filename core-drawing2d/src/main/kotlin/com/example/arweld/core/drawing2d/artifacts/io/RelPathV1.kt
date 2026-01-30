package com.example.arweld.core.drawing2d.artifacts.io

import java.nio.file.Paths

object RelPathV1 {
    fun normalizeOrThrow(relPath: String): String {
        val trimmed = relPath.trim()
        require(trimmed.isNotEmpty()) { "relPath must not be blank" }
        require(!trimmed.startsWith("/")) { "relPath must be relative" }
        require(!trimmed.contains('\\')) { "relPath must use '/' separators" }
        require(!trimmed.matches(Regex("^[A-Za-z]:.*"))) { "relPath must not include drive letters" }

        val collapsed = trimmed.replace(Regex("/+"), "/").trimEnd('/')
        require(collapsed.isNotEmpty()) { "relPath must not be a directory" }

        val segments = collapsed.split("/")
        require(segments.none { it == ".." }) { "relPath must not contain '..' segments" }
        require(segments.none { it.isEmpty() }) { "relPath must not contain empty segments" }

        val path = Paths.get(collapsed)
        require(!path.isAbsolute) { "relPath must be relative" }

        return collapsed
    }
}
