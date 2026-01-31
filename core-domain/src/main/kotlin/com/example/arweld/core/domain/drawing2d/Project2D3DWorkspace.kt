package com.example.arweld.core.domain.drawing2d

data class Project2D3DWorkspace(
    val workspaceDirName: String = DEFAULT_WORKSPACE_DIR,
    val drawing2dJsonName: String = DRAWING2D_JSON_NAME,
    val overlayPreviewName: String = OVERLAY_PREVIEW_NAME,
    val underlayName: String? = null,
) {
    fun drawing2dJsonRelativePath(): String = pathFor(drawing2dJsonName)

    fun overlayPreviewRelativePath(): String = pathFor(overlayPreviewName)

    fun underlayRelativePath(): String? = underlayName?.let(::pathFor)

    fun workspaceSegments(): List<String> = workspaceDirName.split(PATH_SEPARATOR).filter { it.isNotBlank() }

    private fun pathFor(fileName: String): String = "$workspaceDirName$PATH_SEPARATOR$fileName"

    companion object {
        const val DEFAULT_WORKSPACE_DIR = "workspace/2d3d"
        const val DRAWING2D_JSON_NAME = "drawing2d.json"
        const val OVERLAY_PREVIEW_NAME = "overlay_preview.png"
        const val UNDERLAY_BASENAME = "underlay"
        private const val PATH_SEPARATOR = "/"

        fun defaultUnderlayName(extension: String?): String {
            val trimmed = extension?.trim()?.trimStart('.').orEmpty()
            return if (trimmed.isBlank()) {
                UNDERLAY_BASENAME
            } else {
                "$UNDERLAY_BASENAME.$trimmed"
            }
        }
    }
}
