package com.example.arweld.feature.drawingeditor.diagnostics

import com.example.arweld.core.domain.diagnostics.ArTelemetrySnapshot
import com.example.arweld.core.domain.diagnostics.DeviceHealthSnapshot
import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import com.example.arweld.core.domain.diagnostics.DiagnosticsSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EditorDiagnosticsLoggerTest {

    private data class RecordedEvent(
        val name: String,
        val attributes: Map<String, String>,
    )

    private val recordedEvents = mutableListOf<RecordedEvent>()

    private val fakeRecorder = object : DiagnosticsRecorder {
        override fun recordEvent(name: String, attributes: Map<String, String>) {
            recordedEvents.add(RecordedEvent(name, attributes))
        }
        override fun updateArTelemetry(snapshot: ArTelemetrySnapshot) {}
        override fun updateDeviceHealth(snapshot: DeviceHealthSnapshot) {}
        override fun snapshot(maxEvents: Int): DiagnosticsSnapshot {
            return DiagnosticsSnapshot(emptyList(), null, null)
        }
    }

    private lateinit var logger: EditorDiagnosticsLogger

    @Before
    fun setUp() {
        recordedEvents.clear()
        logger = EditorDiagnosticsLogger(fakeRecorder)
    }

    @Test
    fun `all events have expected names`() {
        assertEquals("editor_opened", EditorEvent.EDITOR_OPENED.eventName)
        assertEquals("editor_drawing_saved", EditorEvent.DRAWING_SAVED.eventName)
        assertEquals("editor_tool_changed", EditorEvent.TOOL_CHANGED.eventName)
        assertEquals("editor_node_added", EditorEvent.NODE_ADDED.eventName)
        assertEquals("editor_node_moved", EditorEvent.NODE_MOVED.eventName)
        assertEquals("editor_member_added", EditorEvent.MEMBER_ADDED.eventName)
        assertEquals("editor_scale_set", EditorEvent.SCALE_SET.eventName)
    }

    @Test
    fun `all events have expected phases`() {
        assertEquals("lifecycle", EditorEvent.EDITOR_OPENED.phase)
        assertEquals("lifecycle", EditorEvent.DRAWING_SAVED.phase)
        assertEquals("tool", EditorEvent.TOOL_CHANGED.phase)
        assertEquals("node", EditorEvent.NODE_ADDED.phase)
        assertEquals("node", EditorEvent.NODE_MOVED.phase)
        assertEquals("member", EditorEvent.MEMBER_ADDED.phase)
        assertEquals("scale", EditorEvent.SCALE_SET.phase)
    }

    @Test
    fun `logEditorOpened emits event with correct attributes`() {
        logger.logEditorOpened(projectId = "proj-123")

        assertEquals(1, recordedEvents.size)
        val event = recordedEvents[0]
        assertEquals("editor_opened", event.name)
        assertEquals("drawing_editor", event.attributes["feature"])
        assertEquals("lifecycle", event.attributes["phase"])
        assertEquals("proj-123", event.attributes["projectId"])
    }

    @Test
    fun `logEditorOpened without projectId omits projectId attribute`() {
        logger.logEditorOpened()

        assertEquals(1, recordedEvents.size)
        val event = recordedEvents[0]
        assertEquals("editor_opened", event.name)
        assertNull(event.attributes["projectId"])
    }

    @Test
    fun `logDrawingSaved emits event with summary attributes`() {
        logger.logDrawingSaved(
            projectId = "proj-456",
            nodeCount = 10,
            memberCount = 5,
            hasScale = true,
        )

        assertEquals(1, recordedEvents.size)
        val event = recordedEvents[0]
        assertEquals("editor_drawing_saved", event.name)
        assertEquals("drawing_editor", event.attributes["feature"])
        assertEquals("lifecycle", event.attributes["phase"])
        assertEquals("proj-456", event.attributes["projectId"])
        assertEquals("10", event.attributes["nodeCount"])
        assertEquals("5", event.attributes["memberCount"])
        assertEquals("true", event.attributes["hasScale"])
    }

    @Test
    fun `logToolChanged emits event with tool names`() {
        logger.logToolChanged(
            projectId = "proj-789",
            tool = "NODE",
            previousTool = "SELECT",
        )

        assertEquals(1, recordedEvents.size)
        val event = recordedEvents[0]
        assertEquals("editor_tool_changed", event.name)
        assertEquals("drawing_editor", event.attributes["feature"])
        assertEquals("tool", event.attributes["phase"])
        assertEquals("NODE", event.attributes["tool"])
        assertEquals("SELECT", event.attributes["previousTool"])
    }

    @Test
    fun `logToolChanged without previousTool omits previousTool attribute`() {
        logger.logToolChanged(tool = "MEMBER")

        assertEquals(1, recordedEvents.size)
        val event = recordedEvents[0]
        assertNull(event.attributes["previousTool"])
        assertEquals("MEMBER", event.attributes["tool"])
    }

    @Test
    fun `logNodeAdded emits event with node coordinates`() {
        logger.logNodeAdded(
            projectId = "proj-001",
            nodeId = "N42",
            x = 123.45,
            y = 678.90,
        )

        assertEquals(1, recordedEvents.size)
        val event = recordedEvents[0]
        assertEquals("editor_node_added", event.name)
        assertEquals("drawing_editor", event.attributes["feature"])
        assertEquals("node", event.attributes["phase"])
        assertEquals("N42", event.attributes["nodeId"])
        assertEquals("123.45", event.attributes["x"])
        assertEquals("678.9", event.attributes["y"])
    }

    @Test
    fun `logNodeMoved emits event with from and to coordinates`() {
        logger.logNodeMoved(
            projectId = "proj-002",
            nodeId = "N99",
            fromX = 10.0,
            fromY = 20.0,
            toX = 30.0,
            toY = 40.0,
        )

        assertEquals(1, recordedEvents.size)
        val event = recordedEvents[0]
        assertEquals("editor_node_moved", event.name)
        assertEquals("drawing_editor", event.attributes["feature"])
        assertEquals("node", event.attributes["phase"])
        assertEquals("N99", event.attributes["nodeId"])
        assertEquals("10.0", event.attributes["fromX"])
        assertEquals("20.0", event.attributes["fromY"])
        assertEquals("30.0", event.attributes["toX"])
        assertEquals("40.0", event.attributes["toY"])
    }

    @Test
    fun `logMemberAdded emits event with member and node ids`() {
        logger.logMemberAdded(
            projectId = "proj-003",
            memberId = "M1",
            aNodeId = "N1",
            bNodeId = "N2",
        )

        assertEquals(1, recordedEvents.size)
        val event = recordedEvents[0]
        assertEquals("editor_member_added", event.name)
        assertEquals("drawing_editor", event.attributes["feature"])
        assertEquals("member", event.attributes["phase"])
        assertEquals("M1", event.attributes["memberId"])
        assertEquals("N1", event.attributes["aNodeId"])
        assertEquals("N2", event.attributes["bNodeId"])
    }

    @Test
    fun `logScaleSet emits event with scale info`() {
        logger.logScaleSet(
            projectId = "proj-004",
            realWorldLength = 1500.0,
            unit = "mm",
        )

        assertEquals(1, recordedEvents.size)
        val event = recordedEvents[0]
        assertEquals("editor_scale_set", event.name)
        assertEquals("drawing_editor", event.attributes["feature"])
        assertEquals("scale", event.attributes["phase"])
        assertEquals("1500.0", event.attributes["realWorldLength"])
        assertEquals("mm", event.attributes["unit"])
    }

    @Test
    fun `log with extras merges additional attributes`() {
        logger.log(
            event = EditorEvent.EDITOR_OPENED,
            projectId = "proj-005",
            extras = mapOf("customKey" to "customValue", "anotherKey" to "anotherValue"),
        )

        assertEquals(1, recordedEvents.size)
        val event = recordedEvents[0]
        assertEquals("drawing_editor", event.attributes["feature"])
        assertEquals("customValue", event.attributes["customKey"])
        assertEquals("anotherValue", event.attributes["anotherKey"])
    }

    @Test
    fun `event names are stable and deterministic`() {
        // Ensure event names don't change unexpectedly
        val expectedNames = listOf(
            "editor_opened",
            "editor_drawing_saved",
            "editor_tool_changed",
            "editor_node_added",
            "editor_node_moved",
            "editor_member_added",
            "editor_scale_set",
        )

        val actualNames = EditorEvent.entries.map { it.eventName }

        assertEquals(expectedNames.sorted(), actualNames.sorted())
    }

    @Test
    fun `all events include feature attribute`() {
        EditorEvent.entries.forEach { event ->
            logger.log(event)
        }

        assertEquals(EditorEvent.entries.size, recordedEvents.size)
        recordedEvents.forEach { event ->
            assertTrue("Event ${event.name} missing feature attribute", event.attributes.containsKey("feature"))
            assertEquals(EDITOR_FEATURE, event.attributes["feature"])
        }
    }

    @Test
    fun `all events include phase attribute`() {
        EditorEvent.entries.forEach { event ->
            logger.log(event)
        }

        assertEquals(EditorEvent.entries.size, recordedEvents.size)
        recordedEvents.forEach { event ->
            assertTrue("Event ${event.name} missing phase attribute", event.attributes.containsKey("phase"))
        }
    }
}
