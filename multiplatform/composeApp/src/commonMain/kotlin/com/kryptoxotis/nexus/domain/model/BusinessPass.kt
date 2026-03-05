package com.kryptoxotis.nexus.domain.model

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
    val organizationName: String? = null
) {
    @OptIn(ExperimentalTime::class)
    fun isExpired(): Boolean {
        val expires = expiresAt ?: return false
        return try {
            Instant.parse(expires) < Clock.System.now()
        } catch (_: Exception) {
            false
        }
    }

    fun isUsable(): Boolean {
        return status == PassStatus.ACTIVE && !isExpired()
    }
}
