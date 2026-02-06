package com.nfcpass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("display_name")
    val displayName: String? = null,
    val email: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("current_role")
    val currentRole: String? = "personal",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
