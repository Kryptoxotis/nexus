package com.nfcpass.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nfcpass.domain.model.Pass

/**
 * Room database entity for storing passes locally.
 * This allows the app to work offline and cache data from Supabase.
 *
 * Maps 1:1 with the domain Pass model.
 */
@Entity(tableName = "passes")
data class PassEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val passId: String,
    val passName: String,
    val organization: String,
    val isActive: Boolean,
    val expiryDate: String?,
    val link: String?,
    val businessId: String?,
    val createdAt: String,
    val updatedAt: String
) {
    fun toDomain(): Pass = Pass(
        id = id,
        userId = userId,
        passId = passId,
        passName = passName,
        organization = organization,
        isActive = isActive,
        expiryDate = expiryDate,
        link = link,
        businessId = businessId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(pass: Pass): PassEntity = PassEntity(
            id = pass.id,
            userId = pass.userId,
            passId = pass.passId,
            passName = pass.passName,
            organization = pass.organization,
            isActive = pass.isActive,
            expiryDate = pass.expiryDate,
            link = pass.link,
            businessId = pass.businessId,
            createdAt = pass.createdAt,
            updatedAt = pass.updatedAt
        )
    }
}
