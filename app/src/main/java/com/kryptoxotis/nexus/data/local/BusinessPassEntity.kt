package com.kryptoxotis.nexus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kryptoxotis.nexus.domain.model.BusinessPass
import com.kryptoxotis.nexus.domain.model.PassStatus

@Entity(tableName = "business_passes")
data class BusinessPassEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val organizationId: String,
    val status: String,
    val expiresAt: String?,
    val useCount: Int,
    val metadata: String?,
    val organizationName: String?,
    val createdAt: String,
    val updatedAt: String
) {
    fun toDomain(): BusinessPass = BusinessPass(
        id = id,
        userId = userId,
        organizationId = organizationId,
        status = PassStatus.fromString(status),
        expiresAt = expiresAt,
        useCount = useCount,
        metadata = metadata,
        createdAt = createdAt,
        updatedAt = updatedAt,
        organizationName = organizationName
    )

    companion object {
        fun fromDomain(pass: BusinessPass): BusinessPassEntity = BusinessPassEntity(
            id = pass.id,
            userId = pass.userId,
            organizationId = pass.organizationId,
            status = pass.status.toDbString(),
            expiresAt = pass.expiresAt,
            useCount = pass.useCount,
            metadata = pass.metadata,
            organizationName = pass.organizationName,
            createdAt = pass.createdAt,
            updatedAt = pass.updatedAt
        )
    }
}
