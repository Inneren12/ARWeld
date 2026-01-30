package com.example.arweld.core.drawing2d.validation

import com.example.arweld.core.drawing2d.Drawing2DContract
import com.example.arweld.core.drawing2d.v1.AttachmentRefV1
import com.example.arweld.core.drawing2d.v1.Drawing2D
import com.example.arweld.core.drawing2d.v1.LayerV1
import com.example.arweld.core.drawing2d.v1.entities.EntityV1
import com.example.arweld.core.drawing2d.v1.entities.GroupV1
import com.example.arweld.core.drawing2d.v1.entities.TagV1
import com.example.arweld.core.drawing2d.v1.patch.DrawingPatchEvent

/**
 * Structural validator for Drawing2D schema v1.
 */
class DrawingValidatorV1 {

    fun validate(
        drawing: Drawing2D,
        patches: List<DrawingPatchEvent> = emptyList()
    ): List<ViolationV1> {
        val violations = mutableListOf<ViolationV1>()

        validateBasics(drawing, violations)
        validateLayers(drawing.layers, violations)
        validateEntities(drawing, violations)
        validateReferences(drawing, violations)
        GeometryRulesV1.validate(drawing, violations)
        validateAttachments(drawing.attachments, violations)
        validatePatches(drawing, patches, violations)

        return violations.canonicalSorted()
    }

    private fun validateBasics(drawing: Drawing2D, violations: MutableList<ViolationV1>) {
        if (drawing.schemaVersion != Drawing2DContract.DRAWING2D_SCHEMA_VERSION) {
            violations.addError(
                code = CodesV1.SCHEMA_VERSION_MISMATCH,
                path = PathV1.field(PathV1.root, "schemaVersion"),
                message = "schemaVersion must be ${Drawing2DContract.DRAWING2D_SCHEMA_VERSION}"
            )
        }

        if (drawing.drawingId.isBlank()) {
            violations.addError(
                code = CodesV1.DRAWING_ID_BLANK,
                path = PathV1.field(PathV1.root, "drawingId"),
                message = "drawingId must be non-blank"
            )
        }

        if (drawing.rev < 0) {
            violations.addError(
                code = CodesV1.REV_NEGATIVE,
                path = PathV1.field(PathV1.root, "rev"),
                message = "rev must be non-negative"
            )
        }

        if (drawing.page.widthPx <= 0) {
            violations.addError(
                code = CodesV1.PAGE_WIDTH_INVALID,
                path = PathV1.field(PathV1.field(PathV1.root, "page"), "widthPx"),
                message = "page.widthPx must be > 0"
            )
        }

        if (drawing.page.heightPx <= 0) {
            violations.addError(
                code = CodesV1.PAGE_HEIGHT_INVALID,
                path = PathV1.field(PathV1.field(PathV1.root, "page"), "heightPx"),
                message = "page.heightPx must be > 0"
            )
        }
    }

    private fun validateLayers(layers: List<LayerV1>, violations: MutableList<ViolationV1>) {
        if (layers.isEmpty()) {
            violations.addError(
                code = CodesV1.LAYERS_EMPTY,
                path = PathV1.field(PathV1.root, "layers"),
                message = "layers must not be empty"
            )
        }

        val seenIds = mutableSetOf<String>()
        layers.forEach { layer ->
            if (!seenIds.add(layer.id)) {
                violations.addError(
                    code = CodesV1.LAYER_ID_DUPLICATE,
                    path = PathV1.idSelector(PathV1.root, "layers", layer.id),
                    message = "layer id must be unique",
                    refs = listOf(layer.id)
                )
            }

            if (layer.order < 0) {
                violations.addError(
                    code = CodesV1.LAYER_ORDER_NEGATIVE,
                    path = PathV1.field(
                        PathV1.idSelector(PathV1.root, "layers", layer.id),
                        "order"
                    ),
                    message = "layer order must be non-negative",
                    refs = listOf(layer.id)
                )
            }
        }

        val orderGroups = layers.groupBy { it.order }.filterValues { it.size > 1 }
        orderGroups.values.flatten().forEach { layer ->
            violations.addWarn(
                code = CodesV1.LAYER_ORDER_DUPLICATE,
                path = PathV1.field(
                    PathV1.idSelector(PathV1.root, "layers", layer.id),
                    "order"
                ),
                message = "layer order values should be unique",
                refs = listOf(layer.id)
            )
        }
    }

    private fun validateEntities(drawing: Drawing2D, violations: MutableList<ViolationV1>) {
        val seenEntityIds = mutableSetOf<String>()
        drawing.entities.forEach { entity ->
            if (!seenEntityIds.add(entity.id)) {
                violations.addError(
                    code = CodesV1.ENTITY_ID_DUPLICATE,
                    path = PathV1.idSelector(PathV1.root, "entities", entity.id),
                    message = "entity id must be unique",
                    refs = listOf(entity.id)
                )
            }
        }

        val layerIds = drawing.layers.map { it.id }.toSet()
        drawing.entities.forEach { entity ->
            if (entity.layerId !in layerIds) {
                violations.addError(
                    code = CodesV1.ENTITY_LAYER_UNKNOWN,
                    path = PathV1.field(
                        PathV1.idSelector(PathV1.root, "entities", entity.id),
                        "layerId"
                    ),
                    message = "entity.layerId must refer to an existing layer",
                    refs = listOf(entity.layerId)
                )
            }
        }
    }

    private fun validateReferences(drawing: Drawing2D, violations: MutableList<ViolationV1>) {
        val entityIds = drawing.entities.map { it.id }.toSet()

        drawing.entities.forEach { entity ->
            when (entity) {
                is GroupV1 -> {
                    val missing = entity.members.filterNot { it in entityIds }
                    if (missing.isNotEmpty()) {
                        violations.addError(
                            code = CodesV1.GROUP_MEMBER_UNKNOWN,
                            path = PathV1.field(
                                PathV1.idSelector(PathV1.root, "entities", entity.id),
                                "members"
                            ),
                            message = "group members must reference existing entity ids",
                            refs = missing
                        )
                    }
                }

                is TagV1 -> {
                    if (entity.targetId !in entityIds) {
                        violations.addError(
                            code = CodesV1.TAG_TARGET_UNKNOWN,
                            path = PathV1.field(
                                PathV1.idSelector(PathV1.root, "entities", entity.id),
                                "targetId"
                            ),
                            message = "tag targetId must reference an existing entity id",
                            refs = listOf(entity.targetId)
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    private fun validateAttachments(
        attachments: List<AttachmentRefV1>,
        violations: MutableList<ViolationV1>
    ) {
        attachments.forEachIndexed { index, attachment ->
            val attachmentPath = PathV1.index(PathV1.root, "attachments", index)
            if (!isValidRelPath(attachment.relPath)) {
                violations.addError(
                    code = CodesV1.ATTACHMENT_PATH_INVALID,
                    path = PathV1.field(attachmentPath, "relPath"),
                    message = "attachment.relPath must be a relative path without .. or backslashes",
                    refs = listOf(attachment.relPath)
                )
            }

            val sha256 = attachment.sha256
            if (sha256 != null && !isValidSha256(sha256)) {
                violations.addWarn(
                    code = CodesV1.ATTACHMENT_SHA256_INVALID,
                    path = PathV1.field(attachmentPath, "sha256"),
                    message = "attachment.sha256 should be a 64-character hex string",
                    refs = listOf(sha256)
                )
            }
        }
    }

    private fun validatePatches(
        drawing: Drawing2D,
        patches: List<DrawingPatchEvent>,
        violations: MutableList<ViolationV1>
    ) {
        patches.forEach { patch ->
            val patchPath = PathV1.idSelector(PathV1.root, "patches", patch.eventId)

            if (patch.schemaVersion != Drawing2DContract.DRAWING2D_SCHEMA_VERSION) {
                violations.addError(
                    code = CodesV1.PATCH_SCHEMA_VERSION_MISMATCH,
                    path = PathV1.field(patchPath, "schemaVersion"),
                    message = "patch schemaVersion must be ${Drawing2DContract.DRAWING2D_SCHEMA_VERSION}"
                )
            }

            if (patch.drawingId != drawing.drawingId) {
                violations.addError(
                    code = CodesV1.PATCH_DRAWING_ID_MISMATCH,
                    path = PathV1.field(patchPath, "drawingId"),
                    message = "patch drawingId must match drawing.drawingId",
                    refs = listOf(patch.drawingId)
                )
            }

            if (patch.baseRev > drawing.rev) {
                violations.addError(
                    code = CodesV1.PATCH_BASE_REV_INVALID,
                    path = PathV1.field(patchPath, "baseRev"),
                    message = "patch baseRev must be <= drawing.rev",
                    refs = listOf(patch.baseRev.toString())
                )
            }
        }
    }

    private fun isValidRelPath(relPath: String): Boolean {
        if (relPath.isBlank()) {
            return false
        }
        if (relPath.startsWith("/") || relPath.startsWith("\\")) {
            return false
        }
        if (relPath.contains("..")) {
            return false
        }
        if (relPath.contains("\\")) {
            return false
        }
        if (relPath.matches(Regex("^[A-Za-z]:"))) {
            return false
        }
        return true
    }

    private fun isValidSha256(value: String): Boolean {
        return value.matches(Regex("^[0-9a-fA-F]{64}$"))
    }
}
