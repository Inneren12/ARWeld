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

        assertThat(model.nodes).hasSize(2)
        assertThat(model.members).hasSize(1)
        assertThat(model.members.first().profile.designation).isEqualTo("W310x39")
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
    fun `loadModelFromJson fails on missing node reference`() {
        val invalidJson = """
            {
              "id": "bad_nodes_load",
              "units": "mm",
              "nodes": [{ "id": "N1", "x": 0.0, "y": 0.0, "z": 0.0 }],
              "members": [
                { "id": "M1", "kind": "BEAM", "profile": "W310x39", "nodeStartId": "N1", "nodeEndId": "N2" }
              ]
            }
        """.trimIndent()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            core.loadModelFromJson(invalidJson)
        }

        assertThat(exception.message).contains("missing nodeEndId")
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

        assertThrows(IllegalArgumentException::class.java) {
            ModelJsonParser.parse(invalidProfileJson).toDomain(catalog)
        }

        assertThrows(IllegalArgumentException::class.java) {
            core.loadModelFromJson(invalidProfileJson)
        }
    }

    @Test
    fun `orientation meta tolerates partial payload`() {
        val json = """
            {
              "id": "orientation_partial",
              "units": "mm",
              "nodes": [
                { "id": "N1", "x": 0.0, "y": 0.0, "z": 0.0 },
                { "id": "N2", "x": 1000.0, "y": 0.0, "z": 0.0 }
              ],
              "members": [
                {
                  "id": "M1",
                  "kind": "BEAM",
                  "profile": "W310x39",
                  "nodeStartId": "N1",
                  "nodeEndId": "N2",
                  "orientation": { "camberMm": 3.5 }
                }
              ]
            }
        """.trimIndent()

        val model = ModelJsonParser.parse(json).toDomain(catalog)

        assertThat(model.members.single().orientationMeta?.camberMm).isEqualTo(3.5)
        assertThat(model.members.single().orientationMeta?.rollAngleDeg).isNull()
    }

    @Test
    fun `plates are preserved in domain mapping`() {
        val json = """
            {
              "id": "plates_present",
              "units": "mm",
              "nodes": [
                { "id": "N1", "x": 0.0, "y": 0.0, "z": 0.0 },
                { "id": "N2", "x": 1000.0, "y": 0.0, "z": 0.0 }
              ],
              "members": [
                { "id": "M1", "kind": "BEAM", "profile": "W310x39", "nodeStartId": "N1", "nodeEndId": "N2" }
              ],
              "connections": [
                { "id": "C1", "memberIds": ["M1"], "plateIds": ["P1"] }
              ],
              "plates": [
                { "id": "P1", "thicknessMm": 12.0, "widthMm": 200.0, "lengthMm": 300.0 }
              ]
            }
        """.trimIndent()

        val model = ModelJsonParser.parse(json).toDomain(catalog)

        assertThat(model.plates).hasSize(1)
        assertThat(model.plates.single().id).isEqualTo("P1")
    }

    @Test
    fun `validate detects missing plate references`() {
        val json = """
            {
              "id": "missing_plate_refs",
              "units": "mm",
              "nodes": [
                { "id": "N1", "x": 0.0, "y": 0.0, "z": 0.0 },
                { "id": "N2", "x": 1000.0, "y": 0.0, "z": 0.0 }
              ],
              "members": [
                { "id": "M1", "kind": "BEAM", "profile": "W310x39", "nodeStartId": "N1", "nodeEndId": "N2" }
              ],
              "connections": [
                { "id": "C1", "memberIds": ["M1"], "plateIds": ["P404"] }
              ],
              "plates": []
            }
        """.trimIndent()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            core.loadModelFromJson(json)
        }

        assertThat(exception.message).contains("missing plates")
    }

    @Test
    fun `loadModelFromJson fails on duplicate connection ids`() {
        val json = """
            {
              "id": "duplicate_connections",
              "units": "mm",
              "nodes": [
                { "id": "N1", "x": 0.0, "y": 0.0, "z": 0.0 },
                { "id": "N2", "x": 1000.0, "y": 0.0, "z": 0.0 }
              ],
              "members": [
                { "id": "M1", "kind": "BEAM", "profile": "W310x39", "nodeStartId": "N1", "nodeEndId": "N2" }
              ],
              "connections": [
                { "id": "C1", "memberIds": ["M1"] },
                { "id": "C1", "memberIds": ["M1"] }
              ]
            }
        """.trimIndent()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            core.loadModelFromJson(json)
        }

        assertThat(exception.message).contains("Duplicate connection ids")
    }
}
