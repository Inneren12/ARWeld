package com.example.arweld.core.drawing2d

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for core-drawing2d module scaffold.
 */
class CoreDrawing2DModuleTest {

    @Test
    fun `schema version is 1`() {
        assertThat(Drawing2DContract.DRAWING2D_SCHEMA_VERSION).isEqualTo(1)
    }

    @Test
    fun `contract object loads successfully`() {
        // Verify that the contract object can be accessed without exceptions
        val contract = Drawing2DContract
        assertThat(contract).isNotNull()
    }
}
