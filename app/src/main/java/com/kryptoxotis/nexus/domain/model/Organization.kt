package com.kryptoxotis.nexus.domain.model

enum class EnrollmentMode {
    OPEN, PIN, INVITE, CLOSED;

    companion object {
        fun fromString(value: String): EnrollmentMode {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: OPEN
        }
    }

    fun toDbString(): String = name.lowercase()
}

data class Organization(
    val id: String,
    val name: String,
    val type: String? = null,
    val description: String? = null,
    val logoUrl: String? = null,
    val ownerId: String,
    val enrollmentMode: EnrollmentMode = EnrollmentMode.OPEN,
    val staticPin: String? = null,
    val allowSelfEnrollment: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)
