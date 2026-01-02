package com.example.arweld.core.data.event

import com.example.arweld.core.data.db.entity.EventEntity

/**
 * Public wrapper for EventEntity -> domain mapping.
 * Actual mapping stays internal to core-data (toDomain()).
 */
fun EventEntity.toDomainPublic() = this.toDomain()
