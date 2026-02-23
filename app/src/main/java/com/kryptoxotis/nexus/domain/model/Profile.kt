package com.kryptoxotis.nexus.domain.model

enum class AccountType {
    INDIVIDUAL, BUSINESS, ADMIN;

    companion object {
        fun fromString(value: String): AccountType {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: INDIVIDUAL
        }
    }

    fun toDbString(): String = name.lowercase()
}

data class Profile(
    val id: String,
    val email: String? = null,
    val fullName: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val accountType: AccountType = AccountType.INDIVIDUAL,
    val status: String = "active",
    val createdAt: String? = null,
    val updatedAt: String? = null
)
