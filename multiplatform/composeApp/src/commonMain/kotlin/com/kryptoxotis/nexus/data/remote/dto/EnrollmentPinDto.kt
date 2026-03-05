package com.kryptoxotis.nexus.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnrollmentPinDto(
    val id: String? = null,
    @SerialName("organization_id")
    val organizationId: String,
    @SerialName("pin_code")
    val pinCode: String,
    @SerialName("is_used")
    val isUsed: Boolean = false,
    @SerialName("used_by")
    val usedBy: String? = null,
    @SerialName("expires_at")
    val expiresAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
