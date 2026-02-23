package com.kryptoxotis.nexus.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllowedEmailDto(
    val id: String = "",
    val email: String,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("account_type")
    val accountType: String = "individual",
    @SerialName("added_by")
    val addedBy: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
