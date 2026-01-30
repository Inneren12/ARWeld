package com.example.arweld.core.drawing2d.crypto

import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import org.junit.Test

class Sha256V1Test {

    @Test
    fun `hashBytes matches empty vector`() {
        val hash = Sha256V1.hashBytes(ByteArray(0))
        assertThat(hash).isEqualTo(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        )
        assertThat(hash.length).isEqualTo(64)
        assertThat(hash).matches("^[0-9a-f]{64}$")
    }

    @Test
    fun `hashBytes matches abc vector`() {
        val hash = Sha256V1.hashBytes("abc".toByteArray())
        assertThat(hash).isEqualTo(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        )
    }

    @Test
    fun `hashBytes matches quick brown fox vector`() {
        val hash = Sha256V1.hashBytes("The quick brown fox jumps over the lazy dog".toByteArray())
        assertThat(hash).isEqualTo(
            "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592"
        )
    }

    @Test
    fun `hashStream matches hashBytes`() {
        val data = "streaming check".toByteArray()
        val hash = Sha256V1.hashStream(ByteArrayInputStream(data))
        assertThat(hash).isEqualTo(Sha256V1.hashBytes(data))
    }

    @Test
    fun `bytesToHexLower formats lowercase`() {
        val hash = Sha256V1.bytesToHexLower(byteArrayOf(0x00, 0x0f, 0x10, 0x7f, 0x80.toByte(), 0xff.toByte()))
        assertThat(hash).isEqualTo("000f107f80ff")
        assertThat(hash).matches("^[0-9a-f]+$")
    }
}
