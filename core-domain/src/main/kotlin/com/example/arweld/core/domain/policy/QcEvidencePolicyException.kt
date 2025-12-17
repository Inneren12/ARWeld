package com.example.arweld.core.domain.policy

/**
 * Thrown when QC evidence requirements are not satisfied prior to marking pass/fail.
 */
class QcEvidencePolicyException(
    val reasons: List<String>,
) : Exception("QC evidence policy failed")
