package com.kryptoxotis.nexus.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizationDto(
    val id: String? = null,
    val name: String,
    val type: String? = null,
    val description: String? = null,
    @SerialName("logo_url")
    val logoUrl: String? = null,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("enrollment_mode")
    val enrollmentMode: String = "open",
    @SerialName("static_pin")
    val staticPin: String? = null,
    @SerialName("allow_self_enrollment")
    val allowSelfEnrollment: Boolean = true,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
