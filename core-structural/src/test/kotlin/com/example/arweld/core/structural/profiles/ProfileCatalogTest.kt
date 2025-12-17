package com.example.arweld.core.structural.profiles

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class ProfileCatalogTest {

    private val catalog = ProfileCatalog()

    @Test
    fun `finds channel by mixed case designation`() {
        val spec = catalog.findByDesignation("C200X17")

        assertThat(spec).isInstanceOf(ChannelSpec::class.java)
        assertThat(spec?.designation).isEqualTo("C200x17")
        assertThat(spec?.standard).isEqualTo(ProfileStandard.CSA)
    }

    @Test
    fun `resolves angle by canonical designation`() {
        val spec = catalog.findByDesignation("L51x38x6.4")

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
    }

    @Test
    fun `resolves W shape from catalog`() {
        val spec = catalog.findByDesignation("W310x39")

        assertThat(spec).isInstanceOf(WShapeSpec::class.java)
        assertThat(spec?.designation).isEqualTo("W310x39")
    }

    @Test
    fun `resolves alias for channel`() {
        val spec = catalog.findByDesignation("C 200 x 17")

        assertThat(spec).isInstanceOf(ChannelSpec::class.java)
        assertThat(spec?.designation).isEqualTo("C200x17")
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
}
