package com.example.arweld.core.domain.structural

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProfileCatalogNormalizationTest {

    @Test
    fun `normalizeProfileRef canonicalizes W designations`() {
        assertThat(normalizeProfileRef(" W310 X 39 ")).isEqualTo("W310x39")
    }

    @Test
    fun `normalizeProfileRef canonicalizes HSS decimals`() {
        assertThat(normalizeProfileRef("HSS 203x203x6,4")).isEqualTo("HSS 203x203x6.4")
    }

    @Test
    fun `normalizeProfileRef canonicalizes plates`() {
        assertThat(normalizeProfileRef("PL 10×250")).isEqualTo("PL 10x250")
    }

    @Test
    fun `normalizeProfileRef returns null for unsupported inputs`() {
        assertThat(normalizeProfileRef("unknown")).isNull()
    }
}
