package com.kryptoxotis.nexus.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusinessRequestDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("business_name")
    val businessName: String,
    @SerialName("business_type")
    val businessType: String? = null,
    @SerialName("contact_email")
    val contactEmail: String? = null,
    val message: String? = null,
    val status: String = "pending",
    @SerialName("reviewed_by")
    val reviewedBy: String? = null,
    @SerialName("reviewed_at")
    val reviewedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
