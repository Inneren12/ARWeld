package com.example.arweld.core.structural.core

import com.example.arweld.core.structural.model.StructuralModel
import com.example.arweld.core.structural.profiles.ProfileCatalog
import com.example.arweld.core.structural.serialization.ModelJsonParser
import com.example.arweld.core.structural.serialization.toDomain

interface StructuralModelCore {
    fun loadModelFromJson(json: String): StructuralModel
    fun validate(model: StructuralModel): ValidationResult
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

class DefaultStructuralModelCore(
    private val profileCatalog: ProfileCatalog
) : StructuralModelCore {

    override fun loadModelFromJson(json: String): StructuralModel {
        val dto = ModelJsonParser.parse(json)
        val model = dto.toDomain(profileCatalog)
        val validation = validate(model)
        if (!validation.isValid) {
            throw IllegalArgumentException("Invalid model: ${validation.errors.joinToString()}")
        }
        return model
    }

    override fun validate(model: StructuralModel): ValidationResult {
        val errors = mutableListOf<String>()

        val nodeIds = model.nodes.map { it.id }
        if (nodeIds.size != nodeIds.toSet().size) {
            errors.add("Duplicate node ids detected.")
        }
        val nodeIdSet = nodeIds.toSet()

        val memberIds = model.members.map { it.id }
        if (memberIds.size != memberIds.toSet().size) {
            errors.add("Duplicate member ids detected.")
        }
        val memberIdSet = memberIds.toSet()

        model.members.forEach { member ->
            if (member.nodeStartId !in nodeIdSet) {
                errors.add("Member ${member.id} references missing nodeStartId ${member.nodeStartId}.")
            }
            if (member.nodeEndId !in nodeIdSet) {
                errors.add("Member ${member.id} references missing nodeEndId ${member.nodeEndId}.")
            }
            if (profileCatalog.findByDesignation(member.profileDesignation) == null) {
                errors.add("Profile '${member.profileDesignation}' not found for member ${member.id}.")
            }
        }

        model.connections.forEach { connection ->
            val missingMembers = connection.memberIds.filterNot { it in memberIdSet }
            if (missingMembers.isNotEmpty()) {
                errors.add("Connection ${connection.id} references missing members ${missingMembers.joinToString()}.")
            }
            val missingPlates = connection.plateIds.filterNot { plateId ->
                model.plates.any { it.id == plateId }
            }
            if (missingPlates.isNotEmpty()) {
                errors.add("Connection ${connection.id} references missing plates ${missingPlates.joinToString()}.")
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }
}
