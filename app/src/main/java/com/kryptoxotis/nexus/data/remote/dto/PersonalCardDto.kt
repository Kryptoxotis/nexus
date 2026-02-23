package com.kryptoxotis.nexus.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonalCardDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("card_type")
    val cardType: String = "custom",
    val title: String,
    val content: String? = null,
    val icon: String? = null,
    val color: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = false,
    @SerialName("order_index")
    val orderIndex: Int = 0,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
