package com.example.arweld.core.data.structural

import com.example.arweld.core.domain.structural.ProfileItem
import com.example.arweld.core.structural.profiles.CatalogResource
import com.example.arweld.core.structural.profiles.CatalogResourceLoader
import com.example.arweld.core.structural.profiles.AngleSpec
import com.example.arweld.core.structural.profiles.ChannelSpec
import com.example.arweld.core.structural.profiles.HssSpec
import com.example.arweld.core.structural.profiles.PlateSpec
import com.example.arweld.core.structural.profiles.ProfileCatalog
import com.example.arweld.core.structural.profiles.ProfileStandard
import com.example.arweld.core.structural.profiles.WShapeSpec
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProfileCatalogQueryImplTest {

    @Test
    fun `listAll orders profiles by type then profileRef`() = runTest {
        val query = ProfileCatalogQueryImpl(
            catalog = testCatalog(),
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val results = query.listAll().map(ProfileItem::profileRef)

        assertThat(results).containsExactly(
            "W310x39",
            "HSS 203x203x6.4",
            "C200x17",
            "L50x50x6",
            "PL 10x250"
        ).inOrder()
    }

    @Test
    fun `search keeps deterministic ordering and limit`() = runTest {
        val query = ProfileCatalogQueryImpl(
            catalog = testCatalog(),
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val results = query.search(query = "x", limit = 3).map(ProfileItem::profileRef)

        assertThat(results).containsExactly(
            "W310x39",
            "HSS 203x203x6.4",
            "C200x17"
        ).inOrder()
    }

    @Test
    fun `lookup hits and misses`() = runTest {
        val query = ProfileCatalogQueryImpl(
            catalog = testCatalog(),
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        assertThat(query.lookup("w310x39")?.profileRef).isEqualTo("W310x39")
        assertThat(query.lookup("unknown")).isNull()
    }

    private fun testCatalog(): ProfileCatalog {
        val loader = object : CatalogResourceLoader {
            override fun loadCatalogResources(): List<CatalogResource> = emptyList()
        }
        return ProfileCatalog(
            resourceLoader = loader,
            resourceName = "",
            extraProfiles = listOf(
                WShapeSpec(
                    designation = "W310x39",
                    standard = ProfileStandard.CSA,
                    dMm = 310.0,
                    bfMm = 165.0,
                    twMm = 7.1,
                    tfMm = 10.7
                ),
                HssSpec(
                    designation = "HSS 203x203x6.4",
                    standard = ProfileStandard.CSA,
                    hMm = 203.0,
                    bMm = 203.0,
                    tMm = 6.4
                ),
                ChannelSpec(
                    designation = "C200x17",
                    standard = ProfileStandard.CSA,
                    dMm = 200.0,
                    bfMm = 75.0,
                    twMm = 6.0,
                    tfMm = 9.0
                ),
                AngleSpec(
                    designation = "L50x50x6",
                    standard = ProfileStandard.CSA,
                    leg1Mm = 50.0,
                    leg2Mm = 50.0,
                    tMm = 6.0
                ),
                PlateSpec(
                    designation = "PL 10x250",
                    standard = ProfileStandard.CSA,
                    tMm = 10.0,
                    wMm = 250.0
                )
            )
        )
    }
}
