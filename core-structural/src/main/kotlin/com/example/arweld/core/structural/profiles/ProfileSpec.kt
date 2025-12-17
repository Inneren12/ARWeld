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
    val dMm: Double? = null,
    val bfMm: Double? = null,
    val twMm: Double? = null,
    val tfMm: Double? = null,
    override val type: ProfileType = ProfileType.C
) : ProfileSpec

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
    override val massKgPerM: Double? = null,
    override val areaMm2: Double? = null,
    val tMm: Double,
    val wMm: Double,
    override val type: ProfileType = ProfileType.PL
) : ProfileSpec
