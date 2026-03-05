package com.kryptoxotis.nexus.domain.model

data class BusinessRequest(
    val id: String,
    val userId: String,
    val businessName: String,
    val businessType: String? = null,
    val contactEmail: String? = null,
    val message: String? = null,
    val status: String = "pending",
    val reviewedBy: String? = null,
    val reviewedAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)
