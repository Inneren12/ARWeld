package com.example.arweld.feature.drawingeditor.diagnostics

import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder

/**
 * Feature identifier for editor diagnostics events.
 */
const val EDITOR_FEATURE = "drawing_editor"

/**
 * Enumeration of all diagnostic events emitted by the 2D3D manual editor.
 *
 * Each event has:
 * - [eventName]: stable identifier used in diagnostics export
 * - [phase]: logical grouping (lifecycle, tool, node, member, scale)
 */
enum class EditorEvent(val eventName: String, val phase: String) {
    /** Emitted when the editor screen is opened. */
    EDITOR_OPENED("editor_opened", "lifecycle"),

    /** Emitted when the drawing is saved. */
    DRAWING_SAVED("editor_drawing_saved", "lifecycle"),

    /** Emitted when the user switches tools (SELECT, SCALE, NODE, MEMBER). */
    TOOL_CHANGED("editor_tool_changed", "tool"),

    /** Emitted when a new node is added to the drawing. */
    NODE_ADDED("editor_node_added", "node"),

    /** Emitted when an existing node is moved. */
    NODE_MOVED("editor_node_moved", "node"),

    /** Emitted when a new member (connection between nodes) is added. */
    MEMBER_ADDED("editor_member_added", "member"),

    /** Emitted when scale calibration is set. */
    SCALE_SET("editor_scale_set", "scale"),
}

/**
 * Logging helper for 2D3D editor diagnostic events.
 *
 * Wraps [DiagnosticsRecorder] with structured event logging that includes
 * consistent attributes (feature, phase, projectId) for all editor events.
 *
 * Usage:
 * ```kotlin
 * editorDiagnosticsLogger.log(
 *     event = EditorEvent.TOOL_CHANGED,
 *     projectId = "proj-123",
 *     extras = mapOf("tool" to "NODE", "previousTool" to "SELECT")
 * )
 * ```
 */
class EditorDiagnosticsLogger(
    private val diagnosticsRecorder: DiagnosticsRecorder,
) {
    /**
     * Logs an editor diagnostic event with structured attributes.
     *
     * @param event The type of editor event being logged.
     * @param projectId Optional project identifier (no PII).
     * @param extras Additional key-value attributes for the event.
     */
    fun log(
        event: EditorEvent,
        projectId: String? = null,
        extras: Map<String, String> = emptyMap(),
    ) {
        val attributes = buildMap {
            put("feature", EDITOR_FEATURE)
            put("phase", event.phase)
            projectId?.let { put("projectId", it) }
            putAll(extras)
        }
        diagnosticsRecorder.recordEvent(event.eventName, attributes)
    }

    /**
     * Logs [EditorEvent.EDITOR_OPENED].
     */
    fun logEditorOpened(projectId: String? = null) {
        log(EditorEvent.EDITOR_OPENED, projectId)
    }

    /**
     * Logs [EditorEvent.DRAWING_SAVED].
     *
     * @param nodeCount Number of nodes in the saved drawing.
     * @param memberCount Number of members in the saved drawing.
     * @param hasScale Whether scale calibration is set.
     */
    fun logDrawingSaved(
        projectId: String? = null,
        nodeCount: Int,
        memberCount: Int,
        hasScale: Boolean,
    ) {
        log(
            EditorEvent.DRAWING_SAVED,
            projectId,
            mapOf(
                "nodeCount" to nodeCount.toString(),
                "memberCount" to memberCount.toString(),
                "hasScale" to hasScale.toString(),
            ),
        )
    }

    /**
     * Logs [EditorEvent.TOOL_CHANGED].
     *
     * @param tool The newly selected tool name.
     * @param previousTool The previously selected tool name (optional).
     */
    fun logToolChanged(
        projectId: String? = null,
        tool: String,
        previousTool: String? = null,
    ) {
        log(
            EditorEvent.TOOL_CHANGED,
            projectId,
            buildMap {
                put("tool", tool)
                previousTool?.let { put("previousTool", it) }
            },
        )
    }

    /**
     * Logs [EditorEvent.NODE_ADDED].
     *
     * @param nodeId The identifier of the added node.
     * @param x The x-coordinate of the node.
     * @param y The y-coordinate of the node.
     */
    fun logNodeAdded(
        projectId: String? = null,
        nodeId: String,
        x: Double,
        y: Double,
    ) {
        log(
            EditorEvent.NODE_ADDED,
            projectId,
            mapOf(
                "nodeId" to nodeId,
                "x" to x.toString(),
                "y" to y.toString(),
            ),
        )
    }

    /**
     * Logs [EditorEvent.NODE_MOVED].
     *
     * @param nodeId The identifier of the moved node.
     * @param fromX The original x-coordinate.
     * @param fromY The original y-coordinate.
     * @param toX The new x-coordinate.
     * @param toY The new y-coordinate.
     */
    fun logNodeMoved(
        projectId: String? = null,
        nodeId: String,
        fromX: Double,
        fromY: Double,
        toX: Double,
        toY: Double,
    ) {
        log(
            EditorEvent.NODE_MOVED,
            projectId,
            mapOf(
                "nodeId" to nodeId,
                "fromX" to fromX.toString(),
                "fromY" to fromY.toString(),
                "toX" to toX.toString(),
                "toY" to toY.toString(),
            ),
        )
    }

    /**
     * Logs [EditorEvent.MEMBER_ADDED].
     *
     * @param memberId The identifier of the added member.
     * @param aNodeId The identifier of the first connected node.
     * @param bNodeId The identifier of the second connected node.
     */
    fun logMemberAdded(
        projectId: String? = null,
        memberId: String,
        aNodeId: String,
        bNodeId: String,
    ) {
        log(
            EditorEvent.MEMBER_ADDED,
            projectId,
            mapOf(
                "memberId" to memberId,
                "aNodeId" to aNodeId,
                "bNodeId" to bNodeId,
            ),
        )
    }

    /**
     * Logs [EditorEvent.SCALE_SET].
     *
     * @param realWorldLength The real-world length value for scale calibration.
     * @param unit The unit of measurement (e.g., "mm", "in").
     */
    fun logScaleSet(
        projectId: String? = null,
        realWorldLength: Double,
        unit: String,
    ) {
        log(
            EditorEvent.SCALE_SET,
            projectId,
            mapOf(
                "realWorldLength" to realWorldLength.toString(),
                "unit" to unit,
            ),
        )
    }
}
