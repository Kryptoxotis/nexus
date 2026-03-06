package com.kryptoxotis.nexus.data.repository

import com.kryptoxotis.nexus.data.local.BusinessPassLocalDataSource
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.remote.dto.BusinessPassDto
import com.kryptoxotis.nexus.data.remote.dto.OrganizationDto
import com.kryptoxotis.nexus.domain.model.BusinessPass
import com.kryptoxotis.nexus.domain.model.EnrollmentMode
import com.kryptoxotis.nexus.domain.model.PassStatus
import com.kryptoxotis.nexus.domain.model.Result
import com.kryptoxotis.nexus.platform.Logger
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class BusinessPassRepository(
    private val localDataSource: BusinessPassLocalDataSource
) {
    companion object {
        private const val TAG = "Nexus:BizPassRepo"
    }

    private fun getCurrentUserId(): String? {
        return try {
            SupabaseClientProvider.getClient().auth.currentUserOrNull()?.id
        } catch (_: Exception) {
            null
        }
    }

    fun observeUserPasses(): Flow<List<BusinessPass>> {
        val userId = getCurrentUserId() ?: return flowOf(emptyList())
        return localDataSource.observePassesByUser(userId)
    }

    fun observeOrganizationPasses(orgId: String): Flow<List<BusinessPass>> =
        localDataSource.observePassesByOrganization(orgId)

    suspend fun enrollInOrganization(
        organizationId: String,
        organizationName: String? = null
    ): Result<BusinessPass> {
        return try {
            val userId = getCurrentUserId() ?: return Result.Error("Not authenticated")
            val supabase = SupabaseClientProvider.getClient()

            val org = supabase.postgrest["organizations"]
                .select { filter { eq("id", organizationId) } }
                .decodeSingleOrNull<OrganizationDto>()
                ?: return Result.Error("Organization not found")

            if (EnrollmentMode.fromString(org.enrollmentMode) != EnrollmentMode.OPEN) {
                return Result.Error("This organization does not allow open enrollment")
            }

            val now = Clock.System.now().toString()
            val id = Uuid.random().toString()

            val pass = BusinessPass(
                id = id, userId = userId, organizationId = organizationId,
                status = PassStatus.ACTIVE, createdAt = now, updatedAt = now,
                organizationName = organizationName
            )

            supabase.postgrest["business_passes"].insert(BusinessPassDto(
                id = id, userId = userId, organizationId = organizationId,
                status = "active", createdAt = now, updatedAt = now
            ))

            localDataSource.insertPass(pass)
            Result.Success(pass)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to enroll", e)
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

            val orgIds = remotePasses.map { it.organizationId }.distinct()
            val orgNames = mutableMapOf<String, String>()
            if (orgIds.isNotEmpty()) {
                try {
                    supabase.postgrest["organizations"]
                        .select { filter { isIn("id", orgIds) } }
                        .decodeList<OrganizationDto>()
                        .forEach { org -> org.id?.let { orgNames[it] = org.name } }
                } catch (_: Exception) {}
            }

            val localPasses = localDataSource.getPassesByUser(userId)
            val localById = localPasses.associateBy { it.id }

            for (remote in remotePasses) {
                val id = remote.id ?: continue
                val now = Clock.System.now().toString()
                val pass = BusinessPass(
                    id = id, userId = remote.userId, organizationId = remote.organizationId,
                    status = PassStatus.fromString(remote.status), expiresAt = remote.expiresAt,
                    useCount = remote.useCount, metadata = remote.metadata,
                    organizationName = orgNames[remote.organizationId],
                    createdAt = remote.createdAt ?: now, updatedAt = remote.updatedAt ?: now
                )
                val local = localById[id]
                if (local == null || (remote.updatedAt ?: "") >= local.updatedAt) {
                    localDataSource.insertPass(pass)
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to sync business passes", e)
        }
    }
}
