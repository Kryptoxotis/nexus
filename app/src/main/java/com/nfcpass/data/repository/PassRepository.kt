package com.nfcpass.data.repository

import android.util.Log
import com.nfcpass.data.local.PassDao
import com.nfcpass.data.local.PassEntity
import com.nfcpass.data.remote.SupabaseClientProvider
import com.nfcpass.data.remote.dto.PassDto
import com.nfcpass.domain.model.Pass
import com.nfcpass.domain.model.Result
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Repository for managing Pass data.
 * Offline-first: Room is source of truth, syncs with Supabase when online.
 */
class PassRepository(
    private val passDao: PassDao
) {
    companion object {
        private const val TAG = "NFCPass:PassRepo"
        const val DEFAULT_USER_ID = "local-user"
    }

    /**
     * Gets the current user ID from Supabase auth, or falls back to local-user.
     */
    private fun getCurrentUserId(): String {
        return try {
            SupabaseClientProvider.getClient().auth.currentUserOrNull()?.id ?: DEFAULT_USER_ID
        } catch (e: Exception) {
            DEFAULT_USER_ID
        }
    }

    /**
     * Observes all passes as a Flow
     */
    fun observeUserPasses(): Flow<List<Pass>> {
        val userId = getCurrentUserId()
        Log.d(TAG, "Observing passes for user: $userId")
        return passDao.observePassesByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Observes the currently active pass as a Flow
     */
    fun observeActivePass(): Flow<Pass?> {
        val userId = getCurrentUserId()
        return passDao.observeActivePass(userId).map { it?.toDomain() }
    }

    /**
     * Gets the currently active pass (for NFC emulation)
     */
    suspend fun getActivePass(): Pass? {
        val userId = getCurrentUserId()
        return passDao.getActivePass(userId)?.toDomain()
    }

    /**
     * Adds a new pass - writes to Room first, then pushes to Supabase.
     */
    suspend fun addPass(
        passId: String,
        passName: String,
        organization: String,
        expiryDate: String? = null,
        link: String? = null,
        businessId: String? = null
    ): Result<Pass> {
        return try {
            val userId = getCurrentUserId()
            Log.d(TAG, "Adding pass: $passName for user: $userId")

            val now = java.time.Instant.now().toString()
            val id = UUID.randomUUID().toString()

            val pass = Pass(
                id = id,
                userId = userId,
                passId = passId,
                passName = passName,
                organization = organization,
                isActive = false,
                expiryDate = expiryDate,
                link = link,
                businessId = businessId,
                createdAt = now,
                updatedAt = now
            )

            // Insert into local database first (offline-first)
            passDao.insertPass(PassEntity.fromDomain(pass))

            // Push to Supabase in background (best effort)
            pushPassToSupabase(pass)

            Log.d(TAG, "Pass added successfully: ${pass.id}")
            Result.Success(pass)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add pass", e)
            Result.Error("Failed to add pass: ${e.message}", e)
        }
    }

    /**
     * Activates a pass for NFC emulation (local-only, NFC state doesn't sync)
     */
    suspend fun activatePass(passId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            Log.d(TAG, "Activating pass: $passId")

            passDao.deactivateAllPasses(userId)

            val passEntity = passDao.getPassById(passId) ?: return Result.Error("Pass not found")

            val updatedEntity = passEntity.copy(
                isActive = true,
                updatedAt = java.time.Instant.now().toString()
            )
            passDao.updatePass(updatedEntity)

            Log.d(TAG, "Pass activated successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to activate pass", e)
            Result.Error("Failed to activate pass: ${e.message}", e)
        }
    }

    /**
     * Deactivates a pass (local-only)
     */
    suspend fun deactivatePass(passId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deactivating pass: $passId")

            val passEntity = passDao.getPassById(passId) ?: return Result.Error("Pass not found")
            val updatedEntity = passEntity.copy(
                isActive = false,
                updatedAt = java.time.Instant.now().toString()
            )
            passDao.updatePass(updatedEntity)

            Log.d(TAG, "Pass deactivated successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deactivate pass", e)
            Result.Error("Failed to deactivate pass: ${e.message}", e)
        }
    }

    /**
     * Deletes a pass - from Room first, then from Supabase.
     */
    suspend fun deletePass(passId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting pass: $passId")

            val passEntity = passDao.getPassById(passId) ?: return Result.Error("Pass not found")
            passDao.deletePass(passEntity)

            // Delete from Supabase (best effort)
            deletePassFromSupabase(passId)

            Log.d(TAG, "Pass deleted successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete pass", e)
            Result.Error("Failed to delete pass: ${e.message}", e)
        }
    }

    /**
     * Syncs passes from Supabase into Room on login.
     */
    suspend fun syncFromSupabase() {
        try {
            val userId = getCurrentUserId()
            if (userId == DEFAULT_USER_ID) return

            Log.d(TAG, "Syncing passes from Supabase for user: $userId")

            val supabase = SupabaseClientProvider.getClient()
            val remotePasses = supabase.postgrest["passes"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<PassDto>()

            val localPasses = passDao.getPassesByUser(userId)
            val localIds = localPasses.map { it.id }.toSet()

            // Merge: add remote passes that don't exist locally
            for (remote in remotePasses) {
                val id = remote.id ?: continue
                if (id !in localIds) {
                    passDao.insertPass(PassEntity(
                        id = id,
                        userId = remote.userId,
                        passId = remote.passId,
                        passName = remote.passName,
                        organization = remote.organization,
                        isActive = false, // Don't activate remotely synced passes
                        expiryDate = remote.expiryDate,
                        link = remote.link,
                        businessId = remote.businessId,
                        createdAt = remote.createdAt ?: java.time.Instant.now().toString(),
                        updatedAt = remote.updatedAt ?: java.time.Instant.now().toString()
                    ))
                }
            }

            Log.d(TAG, "Sync complete: ${remotePasses.size} remote, ${localPasses.size} local")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync from Supabase", e)
        }
    }

    /**
     * Migrates local-user passes to the real user ID after first login.
     */
    suspend fun migrateLocalPasses(realUserId: String) {
        try {
            val localPasses = passDao.getPassesByUser(DEFAULT_USER_ID)
            if (localPasses.isEmpty()) return

            Log.d(TAG, "Migrating ${localPasses.size} local passes to user: $realUserId")

            for (pass in localPasses) {
                val migrated = pass.copy(userId = realUserId)
                passDao.updatePass(migrated)
                pushPassToSupabase(migrated.toDomain())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to migrate local passes", e)
        }
    }

    private suspend fun pushPassToSupabase(pass: Pass) {
        try {
            if (pass.userId == DEFAULT_USER_ID) return

            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["passes"].upsert(PassDto(
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
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push pass to Supabase", e)
        }
    }

    private suspend fun deletePassFromSupabase(passId: String) {
        try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["passes"].delete {
                filter { eq("id", passId) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete pass from Supabase", e)
        }
    }
}
