package com.example.arweld.core.structural.core

import com.example.arweld.core.structural.profiles.ProfileCatalog
import com.example.arweld.core.structural.serialization.ModelJsonParser
import com.example.arweld.core.structural.serialization.toDomain
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class StructuralModelCoreTest {

    private val catalog = ProfileCatalog()
    private val core = DefaultStructuralModelCore(catalog)

    private fun readResource(path: String): String =
        this::class.java.getResource("/$path")?.readText()
            ?: error("Resource $path not found")

    @Test
    fun `loads example model and validates`() {
        val json = readResource("example_model.json")

        val model = core.loadModelFromJson(json)
        val validation = core.validate(model)

        assertThat(model.nodes).isNotEmpty()
        assertThat(model.members).isNotEmpty()
        assertThat(model.members.first().profileDesignation).isEqualTo("W310x39")
        assertThat(validation.isValid).isTrue()
    }

    @Test
    fun `validate catches missing node reference`() {
        val invalidJson = """
            {
              "id": "bad_nodes",
              "units": "mm",
              "nodes": [{ "id": "N1", "x": 0.0, "y": 0.0, "z": 0.0 }],
              "members": [
                { "id": "M1", "kind": "BEAM", "profile": "W310x39", "nodeStartId": "N1", "nodeEndId": "N2" }
              ]
            }
        """.trimIndent()

        val dto = ModelJsonParser.parse(invalidJson)
        val model = dto.toDomain(catalog)

        val validation = core.validate(model)
        assertThat(validation.isValid).isFalse()
        assertThat(validation.errors.any { it.contains("N2") }).isTrue()
    }

    @Test
    fun `loadModelFromJson fails on unknown profile`() {
        val invalidProfileJson = """
            {
              "id": "bad_profile",
              "units": "mm",
              "nodes": [{ "id": "N1", "x": 0.0, "y": 0.0, "z": 0.0 }, { "id": "N2", "x": 1000.0, "y": 0.0, "z": 0.0 }],
              "members": [
                { "id": "M1", "kind": "BEAM", "profile": "W999x999", "nodeStartId": "N1", "nodeEndId": "N2" }
              ]
            }
        """.trimIndent()

        val model = ModelJsonParser.parse(invalidProfileJson).toDomain(catalog)
        val validation = core.validate(model)

        assertThat(validation.isValid).isFalse()
        assertThat(validation.errors.any { it.contains("W999x999") }).isTrue()

        assertThrows(IllegalArgumentException::class.java) {
            core.loadModelFromJson(invalidProfileJson)
        }
    }
}
