package com.kryptoxotis.nexus.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReceivedCardDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val name: String = "",
    @SerialName("job_title")
    val jobTitle: String = "",
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val linkedin: String = "",
    val instagram: String = "",
    val twitter: String = "",
    val github: String = "",
    val notes: String = "",
    @SerialName("received_at")
    val receivedAt: String? = null
)
