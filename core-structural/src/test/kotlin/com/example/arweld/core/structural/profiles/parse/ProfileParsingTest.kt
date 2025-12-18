package com.example.arweld.core.structural.profiles.parse

import com.example.arweld.core.structural.profiles.ProfileStandard
import com.example.arweld.core.structural.profiles.ProfileType
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class ProfileParsingTest {

    @Test
    fun `parses channel with mixed casing and whitespace`() {
        val parsed = parseProfileString(" C200X17 ")

        assertThat(parsed.type).isEqualTo(ProfileType.C)
        assertThat(parsed.designation).isEqualTo("C200x17")
        assertThat(parsed.standardHint).isEqualTo(ProfileStandard.CSA)
    }

    @Test
    fun `parses angle with decimal thickness`() {
        val parsed = parseProfileString("L51X38X6,4")

        assertThat(parsed.type).isEqualTo(ProfileType.L)
        assertThat(parsed.designation).isEqualTo("L51x38x6.4")
    }

    @Test
    fun `parses plate with multiplication symbol`() {
        val parsed = parseProfileString("pl 10×190")

        assertThat(parsed.type).isEqualTo(ProfileType.PL)
        assertThat(parsed.designation).isEqualTo("PL10x190")
        assertThat(parsed.standardHint).isNotNull()
    }

    @Test
    fun `parses plate with uppercase separators`() {
        val parsed = parseProfileString("PL6X114")

        assertThat(parsed.type).isEqualTo(ProfileType.PL)
        assertThat(parsed.designation).isEqualTo("PL6x114")
        assertThat(parsed.standardHint).isNotNull()
    }

    @Test
    fun `parses MC series and preserves prefix`() {
        val parsed = parseProfileString("MC250X33")

        assertThat(parsed.type).isEqualTo(ProfileType.C)
        assertThat(parsed.designation).isEqualTo("MC250x33")
        assertThat(parsed.standardHint).isEqualTo(ProfileStandard.CSA)
    }

    @Test
    fun `parses HSS with fractional thickness and AISC hint`() {
        val parsed = parseProfileString("HSS 6x6x3/8")

        assertThat(parsed.type).isEqualTo(ProfileType.HSS)
        assertThat(parsed.designation).isEqualTo("HSS 6x6x3/8")
        assertThat(parsed.standardHint).isEqualTo(ProfileStandard.AISC)
    }

    @Test
    fun `parses plate with extra dimension and ignores tail`() {
        val parsed = parseProfileString("PL10x190x5")

        assertThat(parsed.type).isEqualTo(ProfileType.PL)
        assertThat(parsed.designation).isEqualTo("PL10x190")
    }

    @Test
    fun `throws on unsupported profile`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            parseProfileString("ABC")
        }

        assertThat(exception.message).contains("Unsupported profile")
    }
}
