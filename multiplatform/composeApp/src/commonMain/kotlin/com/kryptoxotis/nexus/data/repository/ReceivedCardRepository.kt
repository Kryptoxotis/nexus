package com.kryptoxotis.nexus.data.repository

import com.kryptoxotis.nexus.data.local.ReceivedCard
import com.kryptoxotis.nexus.data.local.ReceivedCardLocalDataSource
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.remote.dto.ReceivedCardDto
import com.kryptoxotis.nexus.domain.model.Result
import com.kryptoxotis.nexus.platform.Logger
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)
class ReceivedCardRepository(
    private val localDataSource: ReceivedCardLocalDataSource
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

    fun observeContacts(): Flow<List<ReceivedCard>> =
        _userId.flatMapLatest { userId -> localDataSource.observeByUser(userId) }

    suspend fun getById(id: String): ReceivedCard? = localDataSource.getById(id)

    suspend fun saveContact(
        name: String, jobTitle: String = "", company: String = "",
        phone: String = "", email: String = "", website: String = "",
        linkedin: String = "", instagram: String = "", twitter: String = "",
        github: String = "", facebook: String = "", youtube: String = "",
        tiktok: String = "", discord: String = "", twitch: String = "",
        whatsapp: String = ""
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            val now = Clock.System.now().toString()
            val id = Uuid.random().toString()

            val card = ReceivedCard(
                id = id, userId = userId, name = name, jobTitle = jobTitle,
                company = company, phone = phone, email = email, website = website,
                linkedin = linkedin, instagram = instagram, twitter = twitter,
                github = github, facebook = facebook, youtube = youtube,
                tiktok = tiktok, discord = discord, twitch = twitch,
                whatsapp = whatsapp, notes = "", receivedAt = now
            )
            localDataSource.insert(card)
            pushToSupabase(card)
            Result.Success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to save contact", e)
            Result.Error("Failed to save contact: ${e.message}", e)
        }
    }

    suspend fun deleteContact(id: String): Result<Unit> {
        return try {
            localDataSource.deleteById(id)
            deleteFromSupabase(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete contact: ${e.message}", e)
        }
    }

    suspend fun syncFromSupabase() {
        try {
            val userId = getCurrentUserId()
            if (userId == DEFAULT_USER_ID) return

            val remote = SupabaseClientProvider.getClient().postgrest["received_cards"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<ReceivedCardDto>()

            val local = localDataSource.getByUser(userId).associateBy { it.id }
            for (dto in remote) {
                val remoteId = dto.id ?: continue
                if (remoteId !in local) {
                    localDataSource.insert(ReceivedCard(
                        id = remoteId, userId = dto.userId, name = dto.name,
                        jobTitle = dto.jobTitle, company = dto.company,
                        phone = dto.phone, email = dto.email, website = dto.website,
                        linkedin = dto.linkedin, instagram = dto.instagram, twitter = dto.twitter,
                        github = dto.github, facebook = dto.facebook, youtube = dto.youtube,
                        tiktok = dto.tiktok, discord = dto.discord, twitch = dto.twitch,
                        whatsapp = dto.whatsapp, notes = dto.notes,
                        receivedAt = dto.receivedAt ?: Clock.System.now().toString()
                    ))
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to sync received cards", e)
        }
    }

    private suspend fun pushToSupabase(card: ReceivedCard) {
        try {
            if (card.userId == DEFAULT_USER_ID) return
            SupabaseClientProvider.getClient().postgrest["received_cards"].upsert(ReceivedCardDto(
                id = card.id, userId = card.userId, name = card.name,
                jobTitle = card.jobTitle, company = card.company,
                phone = card.phone, email = card.email, website = card.website,
                linkedin = card.linkedin, instagram = card.instagram, twitter = card.twitter,
                github = card.github, facebook = card.facebook, youtube = card.youtube,
                tiktok = card.tiktok, discord = card.discord, twitch = card.twitch,
                whatsapp = card.whatsapp, notes = card.notes, receivedAt = card.receivedAt
            ))
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to push contact to Supabase", e)
        }
    }

    private suspend fun deleteFromSupabase(id: String) {
        try {
            SupabaseClientProvider.getClient().postgrest["received_cards"].delete {
                filter { eq("id", id); eq("user_id", getCurrentUserId()) }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to delete contact from Supabase", e)
        }
    }
}
