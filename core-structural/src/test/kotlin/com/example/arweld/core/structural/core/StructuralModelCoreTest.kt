package com.example.arweld.core.structural.core

import com.example.arweld.core.structural.model.Connection
import com.example.arweld.core.structural.model.Member
import com.example.arweld.core.structural.model.MemberKind
import com.example.arweld.core.structural.model.Node
import com.example.arweld.core.structural.model.StructuralModel
import com.example.arweld.core.structural.profiles.ProfileCatalog
import com.example.arweld.core.structural.profiles.ProfileStandard
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
        val profile = catalog.findByDesignation("W310x39", ProfileStandard.CSA)
            ?: error("Expected profile not found")

        val model = StructuralModel(
            id = "missing_plate_refs",
            nodes = listOf(
                Node("N1", 0.0, 0.0, 0.0),
                Node("N2", 1000.0, 0.0, 0.0)
            ),
            members = listOf(
                Member(
                    id = "M1",
                    kind = MemberKind.BEAM,
                    profile = profile,
                    nodeStartId = "N1",
                    nodeEndId = "N2"
                )
            ),
            connections = listOf(
                Connection(id = "C1", memberIds = listOf("M1"), plateIds = listOf("P404"))
            ),
            plates = emptyList()
        )

        val validation = core.validate(model)

        assertThat(validation.isValid).isFalse()
        assertThat(validation.errors.any { it.contains("missing plates") }).isTrue()
    }
}
