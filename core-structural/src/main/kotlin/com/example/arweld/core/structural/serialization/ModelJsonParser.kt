package com.example.arweld.core.structural.serialization

import com.example.arweld.core.structural.model.Connection
import com.example.arweld.core.structural.model.Member
import com.example.arweld.core.structural.model.MemberKind
import com.example.arweld.core.structural.model.Node
import com.example.arweld.core.structural.model.OrientationMeta
import com.example.arweld.core.structural.model.Plate
import com.example.arweld.core.structural.model.StructuralModel
import com.example.arweld.core.structural.profiles.ProfileCatalog
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
    val orientation: OrientationMeta? = null
)

@Serializable
data class PlateDto(
    val id: String,
    val thicknessMm: Double,
    val widthMm: Double,
    val lengthMm: Double
)

@Serializable
data class ConnectionDto(
    val id: String,
    val memberIds: List<String>,
    val plateIds: List<String> = emptyList()
)

@Serializable
data class StructuralModelDto(
    val id: String,
    val units: String = "mm",
    val nodes: List<NodeDto>,
    val members: List<MemberDto>,
    val connections: List<ConnectionDto> = emptyList(),
    val plates: List<PlateDto> = emptyList(),
    val meta: Map<String, String> = emptyMap()
)

/**
 * Maps DTO to domain StructuralModel while normalizing profile designations.
 */
fun StructuralModelDto.toDomain(profileCatalog: ProfileCatalog): StructuralModel {
    if (!units.equals("mm", ignoreCase = true)) {
        throw IllegalArgumentException("Unsupported units '$units'. Only millimeters (mm) are supported in v0.1.")
    }

    val nodesDomain = nodes.map { Node(it.id, it.x, it.y, it.z) }
    val membersDomain = members.map { member ->
        val parsed = parseProfileString(member.profileDesignation)
        val catalogDesignation = profileCatalog.findByDesignation(parsed.designation)?.designation
        Member(
            id = member.id,
            kind = member.kind,
            profileDesignation = catalogDesignation ?: parsed.designation,
            nodeStartId = member.nodeStartId,
            nodeEndId = member.nodeEndId,
            orientation = member.orientation
        )
    }
    val connectionsDomain =
        connections.map { Connection(it.id, it.memberIds, it.plateIds) }
    val platesDomain =
        plates.map { Plate(it.id, it.thicknessMm, it.widthMm, it.lengthMm) }

    val enrichedMeta = meta + mapOf("units" to units.lowercase())

    return StructuralModel(
        id = id,
        nodes = nodesDomain,
        members = membersDomain,
        connections = connectionsDomain,
        plates = platesDomain,
        meta = enrichedMeta
    )
}
