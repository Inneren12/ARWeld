package com.example.arweld.core.structural.profiles

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProfileParsingTest {

    private val catalog = ProfileCatalog()

    @Test
    fun `parses W shape and finds profile`() {
        val parsed = parseProfileString("W310x39")

        assertThat(parsed.type).isEqualTo(ProfileType.W)
        assertThat(parsed.designation).isEqualTo("W310x39")
        assertThat(catalog.findByDesignation("W310x39")).isNotNull()
    }

    @Test
    fun `parses HSS shape with spaces`() {
        val parsed = parseProfileString("hss 6x6x3/8")

        assertThat(parsed.type).isEqualTo(ProfileType.HSS)
        assertThat(parsed.designation).isEqualTo("HSS 6x6x3/8")
        assertThat(catalog.findByDesignation("hss 6x6x3/8")).isNotNull()
    }

    @Test
    fun `parses plate shape`() {
        val parsed = parseProfileString("PL 10x250")

        assertThat(parsed.type).isEqualTo(ProfileType.PL)
        assertThat(parsed.designation).isEqualTo("PL 10x250")
        assertThat(catalog.findByDesignation("PL 10x250")).isNotNull()
    }

    @Test
    fun `unknown profile returns null`() {
        val lookup = catalog.findByDesignation("W999x999")

        assertThat(lookup).isNull()
    }
}
