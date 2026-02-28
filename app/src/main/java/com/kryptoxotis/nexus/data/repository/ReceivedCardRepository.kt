package com.kryptoxotis.nexus.data.repository

import android.util.Log
import com.kryptoxotis.nexus.data.local.ReceivedCardDao
import com.kryptoxotis.nexus.data.local.ReceivedCardEntity
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.remote.dto.ReceivedCardDto
import com.kryptoxotis.nexus.domain.model.Result
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ReceivedCardRepository(
    private val dao: ReceivedCardDao
) {
    companion object {
        private const val TAG = "Nexus:ReceivedRepo"
        private const val DEFAULT_USER_ID = "local-user"
    }

    private val _userId = MutableStateFlow(DEFAULT_USER_ID)

    fun refreshUserId() {
        _userId.value = getCurrentUserId()
    }

    private fun getCurrentUserId(): String {
        return try {
            SupabaseClientProvider.getClient().auth.currentUserOrNull()?.id ?: DEFAULT_USER_ID
        } catch (_: Exception) {
            DEFAULT_USER_ID
        }
    }

    fun observeContacts(): Flow<List<ReceivedCardEntity>> {
        return _userId.flatMapLatest { userId ->
            dao.observeByUser(userId)
        }
    }

    suspend fun getById(id: String): ReceivedCardEntity? {
        return dao.getById(id)
    }

    suspend fun saveContact(
        name: String,
        jobTitle: String = "",
        company: String = "",
        phone: String = "",
        email: String = "",
        website: String = "",
        linkedin: String = "",
        instagram: String = "",
        twitter: String = "",
        github: String = ""
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            val now = java.time.Instant.now().toString()
            val id = UUID.randomUUID().toString()

            val entity = ReceivedCardEntity(
                id = id,
                userId = userId,
                name = name,
                jobTitle = jobTitle,
                company = company,
                phone = phone,
                email = email,
                website = website,
                linkedin = linkedin,
                instagram = instagram,
                twitter = twitter,
                github = github,
                receivedAt = now
            )
            dao.insert(entity)
            pushToSupabase(entity)
            Log.d(TAG, "Contact saved: $name")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save contact", e)
            Result.Error("Failed to save contact: ${e.message}", e)
        }
    }

    suspend fun deleteContact(id: String): Result<Unit> {
        return try {
            val entity = dao.getById(id) ?: return Result.Error("Contact not found")
            dao.delete(entity)
            deleteFromSupabase(id)
            Log.d(TAG, "Contact deleted: $id")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete contact", e)
            Result.Error("Failed to delete contact: ${e.message}", e)
        }
    }

    suspend fun syncFromSupabase() {
        try {
            val userId = getCurrentUserId()
            if (userId == DEFAULT_USER_ID) return

            val supabase = SupabaseClientProvider.getClient()
            val remote = supabase.postgrest["received_cards"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<ReceivedCardDto>()

            val local = dao.getByUser(userId).associateBy { it.id }

            for (dto in remote) {
                val id = dto.id ?: continue
                if (id !in local) {
                    dao.insert(ReceivedCardEntity(
                        id = id,
                        userId = dto.userId,
                        name = dto.name,
                        jobTitle = dto.jobTitle,
                        company = dto.company,
                        phone = dto.phone,
                        email = dto.email,
                        website = dto.website,
                        linkedin = dto.linkedin,
                        instagram = dto.instagram,
                        twitter = dto.twitter,
                        github = dto.github,
                        notes = dto.notes,
                        receivedAt = dto.receivedAt ?: java.time.Instant.now().toString()
                    ))
                }
            }
            Log.d(TAG, "Sync complete: ${remote.size} remote contacts")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync received cards", e)
        }
    }

    private suspend fun pushToSupabase(entity: ReceivedCardEntity) {
        try {
            if (entity.userId == DEFAULT_USER_ID) return
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["received_cards"].upsert(ReceivedCardDto(
                id = entity.id,
                userId = entity.userId,
                name = entity.name,
                jobTitle = entity.jobTitle,
                company = entity.company,
                phone = entity.phone,
                email = entity.email,
                website = entity.website,
                linkedin = entity.linkedin,
                instagram = entity.instagram,
                twitter = entity.twitter,
                github = entity.github,
                notes = entity.notes,
                receivedAt = entity.receivedAt
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push contact to Supabase", e)
        }
    }

    private suspend fun deleteFromSupabase(id: String) {
        try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["received_cards"].delete {
                filter {
                    eq("id", id)
                    eq("user_id", getCurrentUserId())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete contact from Supabase", e)
        }
    }
}
