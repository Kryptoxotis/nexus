package com.kryptoxotis.nexus.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kryptoxotis.nexus.domain.model.BusinessPass
import com.kryptoxotis.nexus.domain.model.PassStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BusinessPassLocalDataSource(private val db: NexusDatabase) {

    private val queries get() = db.businessPassQueries

    fun observePassesByUser(userId: String): Flow<List<BusinessPass>> =
        queries.selectAll(userId).asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toDomain() }
        }

    fun getPassesByUser(userId: String): List<BusinessPass> =
        queries.selectAll(userId).executeAsList().map { it.toDomain() }

    fun insertPass(pass: BusinessPass) {
        queries.insert(
            id = pass.id,
            userId = pass.userId,
            organizationId = pass.organizationId,
            status = pass.status.toDbString(),
            expiresAt = pass.expiresAt,
            useCount = pass.useCount.toLong(),
            metadata = pass.metadata,
            createdAt = pass.createdAt,
            updatedAt = pass.updatedAt
        )
    }

    fun deletePass(id: String) {
        queries.deleteById(id)
    }

    private fun com.kryptoxotis.nexus.data.local.BusinessPass.toDomain(): BusinessPass = BusinessPass(
        id = id,
        userId = userId,
        organizationId = organizationId,
        status = PassStatus.fromString(status),
        expiresAt = expiresAt,
        useCount = useCount.toInt(),
        metadata = metadata,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
