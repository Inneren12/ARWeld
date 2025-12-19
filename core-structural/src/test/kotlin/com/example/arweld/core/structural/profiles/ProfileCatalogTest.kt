package com.example.arweld.core.structural.profiles

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class ProfileCatalogTest {

    private val catalog = ProfileCatalog()

    @Test
    fun `finds channel by mixed case designation`() {
        val spec = catalog.findByDesignation("C200X20")

        assertThat(spec).isInstanceOf(ChannelSpec::class.java)
        assertThat(spec?.designation).isEqualTo("C200x20")
        assertThat(spec?.standard).isEqualTo(ProfileStandard.CSA)
    }

    @Test
    fun `resolves angle by canonical designation`() {
        val spec = catalog.findByDesignation("L4x4x3/8")

        assertThat(spec).isInstanceOf(AngleSpec::class.java)
        assertThat(spec?.type).isEqualTo(ProfileType.L)
    }

    @Test
    fun `resolves parameteric plate`() {
        val spec = catalog.findByDesignation("PL10x190")

        assertThat(spec).isInstanceOf(PlateSpec::class.java)
        val plate = spec as PlateSpec
        assertThat(plate.tMm).isEqualTo(10.0)
        assertThat(plate.wMm).isEqualTo(190.0)
        assertThat(plate.designation).isEqualTo("PL 10x190")
    }

    @Test
    fun `resolves W shape from catalog`() {
        val spec = catalog.findByDesignation("w310x39")

        assertThat(spec).isInstanceOf(WShapeSpec::class.java)
        assertThat(spec?.designation).isEqualTo("W310x39")
    }

    @Test
    fun `resolves alias for channel`() {
        val spec = catalog.findByDesignation("C 200 x 20")

        assertThat(spec).isInstanceOf(ChannelSpec::class.java)
        assertThat(spec?.designation).isEqualTo("C200x20")
    }

    @Test
    fun `returns null for unknown designation`() {
        val spec = catalog.findByDesignation("UNKNOWN")

        assertThat(spec).isNull()
    }

    @Test
    fun `requireByDesignation throws for unknown`() {
        assertThrows(IllegalStateException::class.java) {
            catalog.requireByDesignation("W999x999")
        }
    }

    @Test
    fun `throws on alias collision`() {
        val loader = object : CatalogResourceLoader {
            override fun loadCatalogResources(): List<CatalogResource> = listOf(
                CatalogResource(
                    name = "one.json",
                    content = """
                        {"standard":"CSA","type":"W","items":[{"designation":"W100x10","aliases":["DUP"],"dMm":100,"bfMm":100,"twMm":6,"tfMm":8}]}
                    """.trimIndent()
                ),
                CatalogResource(
                    name = "two.json",
                    content = """
                        {"standard":"CSA","type":"W","items":[{"designation":"W110x12","aliases":["DUP"],"dMm":110,"bfMm":105,"twMm":6,"tfMm":9}]}
                    """.trimIndent()
                )
            )
        }

        assertThrows(IllegalStateException::class.java) {
            ProfileCatalog(resourceLoader = loader, resourceName = "").listStandards()
        }
    }
}
