package com.example.arweld.core.drawing2d.validation

import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.v1.Drawing2D
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DrawingValidatorGoldenTest {

    private val validator = DrawingValidatorV1()

    @Test
    fun `valid minimal fixture yields no violations`() {
        val drawing = loadFixture("drawing2d/v1/valid_minimal.json")

        val violations = validator.validate(drawing)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `valid full fixture yields no violations`() {
        val drawing = loadFixture("drawing2d/v1/valid_full.json")

        val violations = validator.validate(drawing)

        assertThat(violations).isEmpty()
    }

    private fun loadFixture(path: String): Drawing2D {
        val json = requireNotNull(javaClass.classLoader?.getResource(path)) {
            "Fixture not found: $path"
        }.readText()
        return Drawing2DJson.decodeFromString(json)
    }
}
