package com.example.arweld.core.data.reporting

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvWriterTest {

    @Test
    fun `escapes commas quotes and newlines`() {
        val writer = CsvWriter()
        val header = listOf("id", "note")
        val rows = listOf(
            listOf("1", "Hello, \"world\"\nline2"),
        )

        val actual = writer.write(header, rows)

        val expected = buildString {
            appendLine("id,note")
            appendLine("1,\"Hello, \"\"world\"\"\nline2\"")
        }

        assertEquals(expected, actual)
    }
}
