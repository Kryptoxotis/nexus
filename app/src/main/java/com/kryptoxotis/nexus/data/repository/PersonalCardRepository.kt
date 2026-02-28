package com.kryptoxotis.nexus.data.repository

import android.util.Log
import com.kryptoxotis.nexus.data.local.PersonalCardDao
import com.kryptoxotis.nexus.data.local.PersonalCardEntity
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.remote.dto.PersonalCardDto
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PersonalCard
import com.kryptoxotis.nexus.domain.model.Result
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalCardRepository(
    private val cardDao: PersonalCardDao
) {
    companion object {
        private const val TAG = "Nexus:CardRepo"
        const val DEFAULT_USER_ID = "local-user"
    }

    private val _userId = MutableStateFlow(DEFAULT_USER_ID)

    /** Call after auth state changes so observe flows re-subscribe with the correct userId. */
    fun refreshUserId() {
        _userId.value = getCurrentUserId()
    }

    private fun getCurrentUserId(): String {
        return try {
            SupabaseClientProvider.getClient().auth.currentUserOrNull()?.id ?: run {
                Log.d(TAG, "No authenticated user, using local fallback")
                DEFAULT_USER_ID
            }
        } catch (e: Exception) {
            Log.d(TAG, "Auth check failed, using local fallback")
            DEFAULT_USER_ID
        }
    }

    suspend fun deactivateAllCardsOnStartup() {
        cardDao.deactivateAll()
    }

    fun observeUserCards(): Flow<List<PersonalCard>> {
        return _userId.flatMapLatest { userId ->
            cardDao.observeCardsByUser(userId).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    fun observeActiveCard(): Flow<PersonalCard?> {
        return _userId.flatMapLatest { userId ->
            cardDao.observeActiveCard(userId).map { entity ->
                entity?.toDomain()
            }
        }
    }

    suspend fun getActiveCard(): PersonalCard? {
        val userId = getCurrentUserId()
        return cardDao.getActiveCard(userId)?.toDomain()
    }

    suspend fun addCard(
        cardType: CardType,
        title: String,
        content: String? = null,
        icon: String? = null,
        color: String? = null,
        imageUrl: String? = null,
        cardShape: String = "card"
    ): Result<PersonalCard> {
        return try {
            val userId = getCurrentUserId()
            val now = java.time.Instant.now().toString()
            val id = UUID.randomUUID().toString()

            val existingCards = cardDao.getCardsByUser(userId)
            val nextOrder = (existingCards.maxOfOrNull { it.orderIndex } ?: -1) + 1

            val card = PersonalCard(
                id = id,
                userId = userId,
                cardType = cardType,
                title = title,
                content = content,
                icon = icon,
                color = color,
                imageUrl = imageUrl,
                cardShape = cardShape,
                isActive = false,
                orderIndex = nextOrder,
                createdAt = now,
                updatedAt = now
            )

            cardDao.insertCard(PersonalCardEntity.fromDomain(card))
            pushCardToSupabase(card)

            Log.d(TAG, "Card added: ${card.id}")
            Result.Success(card)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add card", e)
            Result.Error("Failed to add card: ${e.message}", e)
        }
    }

    suspend fun activateCard(cardId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()

            // Remember previously active card before switching
            val previouslyActive = cardDao.getActiveCard(userId)

            // Atomic local operation: deactivate all + activate target in one transaction
            cardDao.activateCardAtomically(userId, cardId)

            // Only push the changed cards (old-active → inactive, new-active → active)
            if (previouslyActive != null && previouslyActive.id != cardId) {
                val deactivated = cardDao.getCardById(previouslyActive.id)
                if (deactivated != null) pushEntityToSupabase(deactivated)
            }
            val activated = cardDao.getCardById(cardId)
            if (activated != null) pushEntityToSupabase(activated)

            Log.d(TAG, "Card activated: $cardId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to activate card", e)
            Result.Error("Failed to activate card: ${e.message}", e)
        }
    }

    suspend fun deactivateCard(cardId: String): Result<Unit> {
        return try {
            val entity = cardDao.getCardById(cardId) ?: return Result.Error("Card not found")
            val updated = entity.copy(
                isActive = false,
                updatedAt = java.time.Instant.now().toString()
            )
            cardDao.updateCard(updated)
            pushEntityToSupabase(updated)

            Log.d(TAG, "Card deactivated: $cardId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deactivate card", e)
            Result.Error("Failed to deactivate card: ${e.message}", e)
        }
    }

    suspend fun updateCard(
        cardId: String,
        title: String,
        content: String?,
        color: String?,
        cardShape: String
    ): Result<Unit> {
        return try {
            val entity = cardDao.getCardById(cardId) ?: return Result.Error("Card not found")
            val updated = entity.copy(
                title = title,
                content = content,
                color = color,
                cardShape = cardShape,
                updatedAt = java.time.Instant.now().toString()
            )
            cardDao.updateCard(updated)
            pushEntityToSupabase(updated)
            Log.d(TAG, "Card updated: $cardId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update card", e)
            Result.Error("Failed to update card: ${e.message}", e)
        }
    }

    suspend fun deleteCard(cardId: String): Result<Unit> {
        return try {
            val entity = cardDao.getCardById(cardId) ?: return Result.Error("Card not found")
            cardDao.deleteCard(entity)
            deleteCardFromSupabase(cardId)

            Log.d(TAG, "Card deleted: $cardId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete card", e)
            Result.Error("Failed to delete card: ${e.message}", e)
        }
    }

    suspend fun reorderCards(cardIds: List<String>): Result<Unit> {
        return try {
            val updatedEntities = mutableListOf<PersonalCardEntity>()
            cardIds.forEachIndexed { index, id ->
                val entity = cardDao.getCardById(id) ?: return@forEachIndexed
                val updated = entity.copy(orderIndex = index)
                cardDao.updateCard(updated)
                updatedEntities.add(updated)
            }
            // Push all reordered cards in parallel
            coroutineScope {
                for (entity in updatedEntities) {
                    launch { pushEntityToSupabase(entity) }
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to reorder cards: ${e.message}", e)
        }
    }

    suspend fun syncFromSupabase() {
        try {
            val userId = getCurrentUserId()
            if (userId == DEFAULT_USER_ID) return

            Log.d(TAG, "Syncing cards bidirectionally")

            val supabase = SupabaseClientProvider.getClient()
            val remoteCards = supabase.postgrest["personal_cards"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<PersonalCardDto>()

            val localCards = cardDao.getCardsByUser(userId)
            val localById = localCards.associateBy { it.id }
            val remoteById = remoteCards.associateBy { it.id ?: "" }.filterKeys { it.isNotEmpty() }

            // Pull: insert or update remote cards locally
            for ((id, remote) in remoteById) {
                val local = localById[id]
                if (local == null) {
                    // Remote card missing locally - insert (always inactive locally)
                    cardDao.insertCard(PersonalCardEntity(
                        id = id,
                        userId = remote.userId,
                        cardType = remote.cardType,
                        title = remote.title,
                        content = remote.content,
                        icon = remote.icon,
                        color = remote.color,
                        imageUrl = remote.imageUrl,
                        cardShape = remote.cardShape,
                        isActive = false,
                        orderIndex = remote.orderIndex,
                        createdAt = remote.createdAt ?: java.time.Instant.now().toString(),
                        updatedAt = remote.updatedAt ?: java.time.Instant.now().toString()
                    ))
                } else {
                    // Both exist - update local if remote is newer or equal (server wins ties)
                    // Never sync isActive — cards should always default to off locally
                    val remoteUpdated = remote.updatedAt ?: ""
                    val localUpdated = local.updatedAt
                    if (remoteUpdated >= localUpdated) {
                        cardDao.updateCard(local.copy(
                            cardType = remote.cardType,
                            title = remote.title,
                            content = remote.content,
                            icon = remote.icon,
                            color = remote.color,
                            imageUrl = remote.imageUrl,
                            cardShape = remote.cardShape,
                            isActive = local.isActive,
                            orderIndex = remote.orderIndex,
                            updatedAt = remoteUpdated
                        ))
                    }
                }
            }

            // Push: push local cards missing remotely to Supabase
            for ((id, local) in localById) {
                if (id !in remoteById) {
                    pushEntityToSupabase(local)
                }
            }

            Log.d(TAG, "Bidirectional sync complete: ${remoteCards.size} remote, ${localCards.size} local")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync from Supabase", e)
        }
    }

    suspend fun migrateLocalUserCards(realUserId: String) {
        try {
            val localCards = cardDao.getCardsByUser(DEFAULT_USER_ID)
            if (localCards.isEmpty()) return

            Log.d(TAG, "Migrating ${localCards.size} local-user cards")

            for (card in localCards) {
                val updated = card.copy(
                    userId = realUserId,
                    updatedAt = java.time.Instant.now().toString()
                )
                cardDao.updateCard(updated)
                pushEntityToSupabase(updated)
            }

            Log.d(TAG, "Migration complete for ${localCards.size} cards")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to migrate local user cards", e)
        }
    }

    private suspend fun pushCardToSupabase(card: PersonalCard) {
        try {
            if (card.userId == DEFAULT_USER_ID) return

            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["personal_cards"].upsert(PersonalCardDto(
                id = card.id,
                userId = card.userId,
                cardType = card.cardType.toDbString(),
                title = card.title,
                content = card.content,
                icon = card.icon,
                color = card.color,
                imageUrl = card.imageUrl,
                cardShape = card.cardShape,
                isActive = card.isActive,
                orderIndex = card.orderIndex,
                createdAt = card.createdAt,
                updatedAt = card.updatedAt
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push card to Supabase", e)
        }
    }

    private suspend fun pushEntityToSupabase(entity: PersonalCardEntity) {
        try {
            if (entity.userId == DEFAULT_USER_ID) return
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["personal_cards"].upsert(PersonalCardDto(
                id = entity.id,
                userId = entity.userId,
                cardType = entity.cardType,
                title = entity.title,
                content = entity.content,
                icon = entity.icon,
                color = entity.color,
                imageUrl = entity.imageUrl,
                cardShape = entity.cardShape,
                isActive = entity.isActive,
                orderIndex = entity.orderIndex,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push card entity to Supabase", e)
        }
    }

    private suspend fun deleteCardFromSupabase(cardId: String) {
        try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["personal_cards"].delete {
                filter {
                    eq("id", cardId)
                    eq("user_id", getCurrentUserId())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete card from Supabase", e)
        }
    }
}
