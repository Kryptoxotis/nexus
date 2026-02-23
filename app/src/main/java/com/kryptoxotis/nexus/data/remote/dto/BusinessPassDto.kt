package com.kryptoxotis.nexus.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusinessPassDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("organization_id")
    val organizationId: String,
    val status: String = "active",
    @SerialName("expires_at")
    val expiresAt: String? = null,
    @SerialName("use_count")
    val useCount: Int = 0,
    val metadata: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
