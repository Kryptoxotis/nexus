package com.kryptoxotis.nexus.domain.model

enum class PassStatus {
    ACTIVE, EXPIRED, REVOKED, SUSPENDED;

    companion object {
        fun fromString(value: String): PassStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: ACTIVE
        }
    }

    fun toDbString(): String = name.lowercase()
}

data class BusinessPass(
    val id: String,
    val userId: String,
    val organizationId: String,
    val status: PassStatus = PassStatus.ACTIVE,
    val expiresAt: String? = null,
    val useCount: Int = 0,
    val metadata: String? = null,
    val createdAt: String,
    val updatedAt: String,
    // Denormalized from organization for display
    val organizationName: String? = null
) {
    fun isExpired(): Boolean {
        val expires = expiresAt ?: return false
        return try {
            java.time.Instant.parse(expires).isBefore(java.time.Instant.now())
        } catch (e: Exception) {
            false
        }
    }

    fun isUsable(): Boolean {
        return status == PassStatus.ACTIVE && !isExpired()
    }
}
