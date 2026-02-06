package com.nfcpass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supabase passes table DTO.
 */
@Serializable
data class PassDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("pass_id")
    val passId: String,
    @SerialName("pass_name")
    val passName: String,
    val organization: String,
    @SerialName("is_active")
    val isActive: Boolean = false,
    @SerialName("expiry_date")
    val expiryDate: String? = null,
    val link: String? = null,
    @SerialName("business_id")
    val businessId: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
