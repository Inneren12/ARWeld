package com.example.arweld.core.structural.profiles

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
enum class ProfileType {
    W,
    HSS,
    C,
    L,
    PL
}

@Serializable
enum class ProfileStandard {
    CSA,
    AISC
}

@Serializable
enum class ChannelSeries {
    C,
    MC
}

@Serializable
@JsonClassDiscriminator("specKind")
@OptIn(ExperimentalSerializationApi::class)
sealed interface ProfileSpec {
    val type: ProfileType
    val designation: String
    val standard: ProfileStandard
    val aliases: List<String>
    val massKgPerM: Double?
    val areaMm2: Double?
}

@Serializable
@SerialName("WShapeSpec")
data class WShapeSpec(
    override val designation: String,
    override val standard: ProfileStandard = ProfileStandard.CSA,
    override val aliases: List<String> = emptyList(),
    override val massKgPerM: Double? = null,
    override val areaMm2: Double? = null,
    val dMm: Double,
    val bfMm: Double,
    val twMm: Double,
    val tfMm: Double,
    val kMm: Double? = null,
    val rMm: Double? = null,
    override val type: ProfileType = ProfileType.W
) : ProfileSpec

@Serializable
@SerialName("ChannelSpec")
data class ChannelSpec(
    override val designation: String,
    override val standard: ProfileStandard = ProfileStandard.CSA,
    override val aliases: List<String> = emptyList(),
    override val massKgPerM: Double? = null,
    override val areaMm2: Double? = null,
    val dMm: Double,
    val bfMm: Double,
    val twMm: Double,
    val tfMm: Double,
    val channelSeries: ChannelSeries = ChannelSeries.C,
    override val type: ProfileType = ProfileType.C
) : ProfileSpec {
    init {
        require(dMm > 0) { "dMm must be positive" }
        require(bfMm > 0) { "bfMm must be positive" }
        require(twMm > 0) { "twMm must be positive" }
        require(tfMm > 0) { "tfMm must be positive" }
    }
}

@Serializable
@SerialName("HssSpec")
data class HssSpec(
    override val designation: String,
    override val standard: ProfileStandard = ProfileStandard.CSA,
    override val aliases: List<String> = emptyList(),
    override val massKgPerM: Double? = null,
    override val areaMm2: Double? = null,
    val hMm: Double,
    val bMm: Double,
    val tMm: Double,
    val cornerRadiusMm: Double? = null,
    override val type: ProfileType = ProfileType.HSS
) : ProfileSpec

@Serializable
@SerialName("AngleSpec")
data class AngleSpec(
    override val designation: String,
    override val standard: ProfileStandard = ProfileStandard.CSA,
    override val aliases: List<String> = emptyList(),
    override val massKgPerM: Double? = null,
    override val areaMm2: Double? = null,
    val leg1Mm: Double,
    val leg2Mm: Double,
    val tMm: Double,
    val cornerRadiusMm: Double? = null,
    override val type: ProfileType = ProfileType.L
) : ProfileSpec

@Serializable
@SerialName("PlateSpec")
data class PlateSpec(
    override val designation: String,
    override val standard: ProfileStandard = ProfileStandard.CSA,
    override val aliases: List<String> = emptyList(),
    val tMm: Double,
    val wMm: Double,
    override val areaMm2: Double = tMm * wMm,
    override val massKgPerM: Double = areaMm2 * 1e-6 * 7850,
    override val type: ProfileType = ProfileType.PL
) : ProfileSpec {
    init {
        require(tMm > 0) { "tMm must be positive" }
        require(wMm > 0) { "wMm must be positive" }
    }
}
