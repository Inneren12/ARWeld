package com.example.arweld.core.structural.profiles.parse

import com.example.arweld.core.structural.profiles.ProfileCatalog
import com.example.arweld.core.structural.profiles.ProfileType
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class ProfileParsingTest {

    private val catalog = ProfileCatalog()

    @Test
    fun `parses channel with mixed casing and whitespace`() {
        val parsed = parseProfileString(" C200X17 ")

        assertThat(parsed.type).isEqualTo(ProfileType.C)
        assertThat(parsed.designation).isEqualTo("C200x17")
        assertThat(parsed.standardHint).isNotNull()
    }

    @Test
    fun `parses angle with decimal thickness`() {
        val parsed = parseProfileString("L51X38X6.4")

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
    fun `throws on unsupported profile`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            parseProfileString("ABC")
        }

        assertThat(exception.message).contains("Unsupported profile")
    }
}
