package com.kryptoxotis.nexus.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccessLogDto(
    val id: String? = null,
    @SerialName("card_id")
    val cardId: String? = null,
    @SerialName("card_type")
    val cardType: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("organization_id")
    val organizationId: String? = null,
    @SerialName("access_granted")
    val accessGranted: Boolean = true,
    val metadata: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
