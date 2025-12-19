package com.example.arweld.core.structural.profiles.parse

import com.example.arweld.core.structural.profiles.ProfileStandard
import com.example.arweld.core.structural.profiles.ProfileType
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class ProfileParsingTest {

    @Test
    fun `parses W designation with normalization`() {
        val parsed = parseProfileString(" w310X39 ")

        assertThat(parsed.type).isEqualTo(ProfileType.W)
        assertThat(parsed.designation).isEqualTo("W310x39")
        assertThat(parsed.standardHint).isNull()
        assertThat(parsed.raw).isEqualTo(" w310X39 ")
    }

    @Test
    fun `parses HSS designation with fraction`() {
        val parsed = parseProfileString("hss 6X6x3/8")

        assertThat(parsed.type).isEqualTo(ProfileType.HSS)
        assertThat(parsed.designation).isEqualTo("HSS 6x6x3/8")
        assertThat(parsed.standardHint).isEqualTo(ProfileStandard.AISC)
    }

    @Test
    fun `parses PL designation with normalization`() {
        val parsed = parseProfileString("PL10x250")

        assertThat(parsed.type).isEqualTo(ProfileType.PL)
        assertThat(parsed.designation).isEqualTo("PL 10x250")
    }

    @Test
    fun `parses channel designation`() {
        val parsed = parseProfileString("c200X20")

        assertThat(parsed.type).isEqualTo(ProfileType.C)
        assertThat(parsed.designation).isEqualTo("C200x20")
    }

    @Test
    fun `parses angle designation`() {
        val parsed = parseProfileString("L4x4x3/8")

        assertThat(parsed.type).isEqualTo(ProfileType.L)
        assertThat(parsed.designation).isEqualTo("L4x4x3/8")
        assertThat(parsed.standardHint).isEqualTo(ProfileStandard.AISC)
    }

    @Test
    fun `rejects unsupported profile strings`() {
        assertThrows(IllegalArgumentException::class.java) {
            parseProfileString("HSS6x6")
        }
        assertThrows(IllegalArgumentException::class.java) {
            parseProfileString("PL10")
        }
        assertThrows(IllegalArgumentException::class.java) {
            parseProfileString("X123")
        }
    }
}
