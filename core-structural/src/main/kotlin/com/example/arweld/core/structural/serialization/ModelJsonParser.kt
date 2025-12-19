package com.example.arweld.core.structural.serialization

import com.example.arweld.core.structural.model.BoltGroup
import com.example.arweld.core.structural.model.BoltPoint2D
import com.example.arweld.core.structural.model.Connection
import com.example.arweld.core.structural.model.Member
import com.example.arweld.core.structural.model.MemberKind
import com.example.arweld.core.structural.model.Node
import com.example.arweld.core.structural.model.OrientationMeta
import com.example.arweld.core.structural.model.Plate
import com.example.arweld.core.structural.model.StructuralModel
import com.example.arweld.core.structural.model.CORE_LENGTH_UNIT
import com.example.arweld.core.structural.profiles.ProfileCatalog
import com.example.arweld.core.structural.profiles.ProfileStandard
import com.example.arweld.core.structural.profiles.parse.parseProfileString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object ModelJsonParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(jsonString: String): StructuralModelDto =
        json.decodeFromString(jsonString)
}

@Serializable
data class NodeDto(
    val id: String,
    val x: Double,
    val y: Double,
    val z: Double
)

@Serializable
data class MemberDto(
    val id: String,
    val kind: MemberKind,
    @SerialName("profile") val profileDesignation: String,
    val nodeStartId: String,
    val nodeEndId: String,
    @SerialName("orientation") val orientationMeta: OrientationMeta? = null
)

@Serializable
data class PlateDto(
    val id: String,
    val thicknessMm: Double,
    val widthMm: Double,
    val lengthMm: Double
)

@Serializable
data class BoltPoint2DDto(
    val xMm: Double,
    val yMm: Double
)

@Serializable
data class BoltGroupDto(
    val id: String,
    val boltDiaMm: Double,
    val grade: String? = null,
    val pattern: List<BoltPoint2DDto> = emptyList()
)

@Serializable
data class ConnectionDto(
    val id: String,
    val memberIds: List<String>,
    val plateIds: List<String> = emptyList(),
    val boltGroupIds: List<String> = emptyList()
)

@Serializable
data class StructuralModelDto(
    val id: String,
    val units: String,
    val nodes: List<NodeDto>,
    val members: List<MemberDto>,
    val connections: List<ConnectionDto> = emptyList(),
    val plates: List<PlateDto> = emptyList(),
    val boltGroups: List<BoltGroupDto> = emptyList(),
    val meta: Map<String, String> = emptyMap()
)

/**
 * Maps DTO to domain StructuralModel while normalizing profile designations.
 */
fun StructuralModelDto.toDomain(profileCatalog: ProfileCatalog): StructuralModel {
    if (!units.equals(CORE_LENGTH_UNIT, ignoreCase = true)) {
        throw IllegalArgumentException("Unsupported units '$units'. Only millimeters (mm) are supported in v0.1.")
    }

    val normalizedUnits = CORE_LENGTH_UNIT
    val nodesDomain = nodes.map { Node(it.id, it.x, it.y, it.z) }
    val membersDomain = members.map { member ->
        val parsed = parseProfileString(member.profileDesignation)
        val profile = profileCatalog.findByDesignation(
            parsed.designation,
            parsed.standardHint ?: ProfileStandard.CSA
        ) ?: throw IllegalArgumentException(
            "Profile '${member.profileDesignation}' not found for member ${member.id}."
        )
        Member(
            id = member.id,
            kind = member.kind,
            profile = profile,
            nodeStartId = member.nodeStartId,
            nodeEndId = member.nodeEndId,
            orientationMeta = member.orientationMeta
        )
    }
    val connectionsDomain =
        connections.map { Connection(it.id, it.memberIds, it.plateIds, it.boltGroupIds) }
    val platesDomain = plates.map { Plate(it.id, it.thicknessMm, it.widthMm, it.lengthMm) }
    val boltGroupsDomain = boltGroups.map { boltGroup ->
        BoltGroup(
            id = boltGroup.id,
            boltDiaMm = boltGroup.boltDiaMm,
            grade = boltGroup.grade,
            pattern = boltGroup.pattern.map { BoltPoint2D(it.xMm, it.yMm) }
        )
    }

    val enrichedMeta = meta + mapOf("units" to normalizedUnits)

    return StructuralModel(
        id = id,
        nodes = nodesDomain,
        members = membersDomain,
        connections = connectionsDomain,
        plates = platesDomain,
        boltGroups = boltGroupsDomain,
        meta = enrichedMeta
    )
}
