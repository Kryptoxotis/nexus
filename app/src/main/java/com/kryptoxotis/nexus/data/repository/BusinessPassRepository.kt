package com.kryptoxotis.nexus.data.repository

import android.util.Log
import com.kryptoxotis.nexus.data.local.BusinessPassDao
import com.kryptoxotis.nexus.data.local.BusinessPassEntity
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.remote.dto.BusinessPassDto
import com.kryptoxotis.nexus.domain.model.BusinessPass
import com.kryptoxotis.nexus.domain.model.PassStatus
import com.kryptoxotis.nexus.domain.model.Result
import com.kryptoxotis.nexus.data.remote.dto.OrganizationDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class BusinessPassRepository(
    private val passDao: BusinessPassDao
) {
    companion object {
        private const val TAG = "Nexus:BizPassRepo"
    }

    private fun getCurrentUserId(): String? {
        return try {
            SupabaseClientProvider.getClient().auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    fun observeUserPasses(): Flow<List<BusinessPass>> {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.d(TAG, "No authenticated user, returning empty pass list")
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        return passDao.observePassesByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun observeOrganizationPasses(orgId: String): Flow<List<BusinessPass>> {
        return passDao.observePassesByOrganization(orgId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun enrollInOrganization(
        organizationId: String,
        organizationName: String? = null
    ): Result<BusinessPass> {
        return try {
            val userId = getCurrentUserId() ?: return Result.Error("Not authenticated")
            val now = java.time.Instant.now().toString()
            val id = UUID.randomUUID().toString()

            val pass = BusinessPass(
                id = id,
                userId = userId,
                organizationId = organizationId,
                status = PassStatus.ACTIVE,
                createdAt = now,
                updatedAt = now,
                organizationName = organizationName
            )

            // Push to Supabase first (it has the unique constraint)
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["business_passes"].insert(BusinessPassDto(
                id = id,
                userId = userId,
                organizationId = organizationId,
                status = "active",
                createdAt = now,
                updatedAt = now
            ))

            // Cache locally
            passDao.insertPass(BusinessPassEntity.fromDomain(pass))

            Log.d(TAG, "Enrolled in organization: $organizationId")
            Result.Success(pass)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enroll", e)
            Result.Error("Failed to enroll: ${e.message}", e)
        }
    }

    suspend fun syncFromSupabase() {
        try {
            val userId = getCurrentUserId() ?: return

            val supabase = SupabaseClientProvider.getClient()
            val remotePasses = supabase.postgrest["business_passes"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<BusinessPassDto>()

            // Fetch org names for display (single batched query)
            val orgIds = remotePasses.mapNotNull { it.organizationId }.distinct()
            val orgNames = mutableMapOf<String, String>()
            if (orgIds.isNotEmpty()) {
                try {
                    val orgs = supabase.postgrest["organizations"]
                        .select { filter { isIn("id", orgIds) } }
                        .decodeList<OrganizationDto>()
                    orgs.forEach { org -> org.id?.let { orgNames[it] = org.name } }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to fetch org names")
                }
            }

            val localPasses = passDao.getPassesByUser(userId)
            val localById = localPasses.associateBy { it.id }

            for (remote in remotePasses) {
                val id = remote.id ?: continue
                val orgName = orgNames[remote.organizationId]
                val entity = BusinessPassEntity(
                    id = id,
                    userId = remote.userId,
                    organizationId = remote.organizationId,
                    status = remote.status,
                    expiresAt = remote.expiresAt,
                    useCount = remote.useCount,
                    metadata = remote.metadata,
                    organizationName = orgName,
                    createdAt = remote.createdAt ?: java.time.Instant.now().toString(),
                    updatedAt = remote.updatedAt ?: java.time.Instant.now().toString()
                )

                val local = localById[id]
                if (local == null) {
                    passDao.insertPass(entity)
                } else {
                    // Update existing pass with latest remote data
                    val remoteUpdated = remote.updatedAt ?: ""
                    if (remoteUpdated >= local.updatedAt) {
                        passDao.insertPass(entity)
                    }
                }
            }

            Log.d(TAG, "Sync complete: ${remotePasses.size} remote passes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync business passes", e)
        }
    }
}
