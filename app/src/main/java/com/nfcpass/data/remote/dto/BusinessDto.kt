package com.nfcpass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusinessDto(
    val id: String? = null,
    @SerialName("owner_id")
    val ownerId: String,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    @SerialName("logo_url")
    val logoUrl: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class BusinessMemberDto(
    val id: String? = null,
    @SerialName("business_id")
    val businessId: String,
    @SerialName("user_id")
    val userId: String,
    val role: String = "member",
    val status: String = "active",
    @SerialName("joined_at")
    val joinedAt: String? = null
)
