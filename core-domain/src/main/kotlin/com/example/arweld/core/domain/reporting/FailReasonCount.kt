package com.example.arweld.core.domain.reporting

import kotlinx.serialization.Serializable

@Serializable
data class FailReasonCount(
    val reason: String,
    val count: Int,
)
