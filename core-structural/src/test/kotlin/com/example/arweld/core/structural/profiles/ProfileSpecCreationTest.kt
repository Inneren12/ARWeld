package com.example.arweld.core.structural.profiles

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProfileSpecCreationTest {

    @Test
    fun `creates W shape spec`() {
        val spec = WShapeSpec(
            designation = "W200x27",
            dMm = 200.0,
            bfMm = 150.0,
            twMm = 7.0,
            tfMm = 10.0
        )

        assertThat(spec.type).isEqualTo(ProfileType.W)
        assertThat(spec.standard).isEqualTo(ProfileStandard.CSA)
        assertThat(spec.aliases).isEmpty()
    }

    @Test
    fun `creates plate spec`() {
        val spec = PlateSpec(
            designation = "PL 12x300",
            tMm = 12.0,
            wMm = 300.0
        )

        assertThat(spec.type).isEqualTo(ProfileType.PL)
        assertThat(spec.standard).isEqualTo(ProfileStandard.CSA)
        assertThat(spec.massKgPerM).isGreaterThan(0.0)
    }

    @Test
    fun `computes plate mass`() {
        val spec = PlateSpec(
            designation = "PL10x190",
            tMm = 10.0,
            wMm = 190.0
        )

        assertThat(spec.areaMm2).isWithin(1e-6).of(1900.0)
        assertThat(spec.massKgPerM).isWithin(1e-3).of(14.915)
    }
}
