package com.example.arweld.core.structural.model

import kotlinx.serialization.Serializable

@Serializable
enum class MemberKind { BEAM, COLUMN, BRACE, OTHER }
