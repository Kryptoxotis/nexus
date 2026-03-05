package com.kryptoxotis.nexus.data.repository

import com.kryptoxotis.nexus.data.local.PersonalCardLocalDataSource
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.remote.dto.PersonalCardDto
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PersonalCard
import com.kryptoxotis.nexus.domain.model.Result
import com.kryptoxotis.nexus.platform.Logger
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)
class PersonalCardRepository(
    private val localDataSource: PersonalCardLocalDataSource
) {
    companion object {
        private const val TAG = "Nexus:CardRepo"
        const val DEFAULT_USER_ID = "local-user"
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

    fun observeUserCards(): Flow<List<PersonalCard>> =
        _userId.flatMapLatest { userId -> localDataSource.observeCardsByUser(userId) }

    fun observeActiveCard(): Flow<PersonalCard?> =
        _userId.flatMapLatest { userId -> localDataSource.observeActiveCard(userId) }

    suspend fun getActiveCard(): PersonalCard? =
        localDataSource.getActiveCard(getCurrentUserId())

    private fun now(): String = Clock.System.now().toString()

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
            val timestamp = now()
            val id = Uuid.random().toString()

            val existingCards = localDataSource.getCardsByUser(userId)
            val nextOrder = (existingCards.maxOfOrNull { it.orderIndex } ?: -1) + 1

            val card = PersonalCard(
                id = id, userId = userId, cardType = cardType, title = title,
                content = content, icon = icon, color = color, imageUrl = imageUrl,
                cardShape = cardShape, isActive = false, orderIndex = nextOrder,
                createdAt = timestamp, updatedAt = timestamp
            )

            localDataSource.insertCard(card)
            pushCardToSupabase(card)
            Logger.d(TAG, "Card added: ${card.id}")
            Result.Success(card)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to add card", e)
            Result.Error("Failed to add card: ${e.message}", e)
        }
    }

    suspend fun activateCard(cardId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            val timestamp = now()
            localDataSource.activateCard(userId, cardId, timestamp)

            val activated = localDataSource.getCardById(cardId)
            if (activated != null) pushCardToSupabase(activated)

            Logger.d(TAG, "Card activated: $cardId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to activate card", e)
            Result.Error("Failed to activate card: ${e.message}", e)
        }
    }

    suspend fun activateCardOnly(cardId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            localDataSource.activateCard(userId, cardId, now())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to activate card: ${e.message}", e)
        }
    }

    suspend fun deactivateCard(cardId: String): Result<Unit> {
        return try {
            val card = localDataSource.getCardById(cardId) ?: return Result.Error("Card not found")
            val timestamp = now()
            localDataSource.updateCard(cardId, card.title, card.content, card.color, card.cardShape, timestamp)
            // Re-fetch to get updated state
            val updated = localDataSource.getCardById(cardId)
            if (updated != null) pushCardToSupabase(updated)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to deactivate card: ${e.message}", e)
        }
    }

    suspend fun updateCard(cardId: String, title: String, content: String?, color: String?, cardShape: String): Result<Unit> {
        return try {
            localDataSource.updateCard(cardId, title, content, color, cardShape, now())
            val updated = localDataSource.getCardById(cardId)
            if (updated != null) pushCardToSupabase(updated)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update card: ${e.message}", e)
        }
    }

    suspend fun deleteCard(cardId: String): Result<Unit> {
        return try {
            localDataSource.deleteCard(cardId)
            deleteCardFromSupabase(cardId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete card: ${e.message}", e)
        }
    }

    suspend fun createStack(cardId1: String, cardId2: String): Result<String> {
        return try {
            val stackId = Uuid.random().toString()
            val timestamp = now()
            localDataSource.updateStackId(cardId1, stackId, timestamp)
            localDataSource.updateStackId(cardId2, stackId, timestamp)
            coroutineScope {
                launch { localDataSource.getCardById(cardId1)?.let { pushCardToSupabase(it) } }
                launch { localDataSource.getCardById(cardId2)?.let { pushCardToSupabase(it) } }
            }
            Result.Success(stackId)
        } catch (e: Exception) {
            Result.Error("Failed to create stack: ${e.message}", e)
        }
    }

    suspend fun addToStack(cardId: String, stackId: String): Result<Unit> {
        return try {
            localDataSource.updateStackId(cardId, stackId, now())
            localDataSource.getCardById(cardId)?.let { pushCardToSupabase(it) }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to add to stack: ${e.message}", e)
        }
    }

    suspend fun removeFromStack(cardId: String): Result<Unit> {
        return try {
            val card = localDataSource.getCardById(cardId) ?: return Result.Error("Card not found")
            val oldStackId = card.stackId
            val timestamp = now()
            localDataSource.updateStackId(cardId, null, timestamp)
            localDataSource.getCardById(cardId)?.let { pushCardToSupabase(it) }

            if (oldStackId != null) {
                val remaining = localDataSource.getCardsByUser(getCurrentUserId())
                    .filter { it.stackId == oldStackId }
                if (remaining.size <= 1) {
                    for (c in remaining) {
                        localDataSource.updateStackId(c.id, null, timestamp)
                        localDataSource.getCardById(c.id)?.let { pushCardToSupabase(it) }
                    }
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to remove from stack: ${e.message}", e)
        }
    }

    suspend fun dissolveStack(stackId: String): Result<Unit> {
        return try {
            val timestamp = now()
            val cards = localDataSource.getCardsByUser(getCurrentUserId())
                .filter { it.stackId == stackId }
            for (card in cards) {
                localDataSource.updateStackId(card.id, null, timestamp)
                localDataSource.getCardById(card.id)?.let { pushCardToSupabase(it) }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to dissolve stack: ${e.message}", e)
        }
    }

    suspend fun reorderCards(cardIds: List<String>): Result<Unit> {
        return try {
            val timestamp = now()
            cardIds.forEachIndexed { index, id ->
                localDataSource.updateOrderIndex(id, index, timestamp)
            }
            coroutineScope {
                for (id in cardIds) {
                    launch { localDataSource.getCardById(id)?.let { pushCardToSupabase(it) } }
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

            val supabase = SupabaseClientProvider.getClient()
            val remoteCards = supabase.postgrest["personal_cards"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<PersonalCardDto>()

            val localCards = localDataSource.getCardsByUser(userId)
            val localById = localCards.associateBy { it.id }
            val remoteById = remoteCards.associateBy { it.id ?: "" }.filterKeys { it.isNotEmpty() }

            for ((id, remote) in remoteById) {
                val local = localById[id]
                if (local == null) {
                    localDataSource.insertCard(PersonalCard(
                        id = id, userId = remote.userId,
                        cardType = CardType.fromString(remote.cardType),
                        title = remote.title, content = remote.content,
                        icon = remote.icon, color = remote.color,
                        imageUrl = remote.imageUrl, cardShape = remote.cardShape,
                        stackId = remote.stackId, isActive = false,
                        orderIndex = remote.orderIndex,
                        createdAt = remote.createdAt ?: now(),
                        updatedAt = remote.updatedAt ?: now()
                    ))
                } else {
                    val remoteUpdated = remote.updatedAt ?: ""
                    if (remoteUpdated >= local.updatedAt) {
                        localDataSource.insertCard(local.copy(
                            cardType = CardType.fromString(remote.cardType),
                            title = remote.title, content = remote.content,
                            icon = remote.icon, color = remote.color,
                            imageUrl = remote.imageUrl, cardShape = remote.cardShape,
                            orderIndex = remote.orderIndex, stackId = remote.stackId,
                            updatedAt = remoteUpdated
                        ))
                    }
                }
            }

            for ((id, _) in localById) {
                if (id !in remoteById) {
                    localDataSource.getCardById(id)?.let { pushCardToSupabase(it) }
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to sync from Supabase", e)
        }
    }

    suspend fun migrateLocalUserCards(realUserId: String) {
        try {
            val localCards = localDataSource.getCardsByUser(DEFAULT_USER_ID)
            if (localCards.isEmpty()) return
            for (card in localCards) {
                val updated = card.copy(userId = realUserId, updatedAt = now())
                localDataSource.insertCard(updated)
                pushCardToSupabase(updated)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to migrate local user cards", e)
        }
    }

    private suspend fun pushCardToSupabase(card: PersonalCard) {
        try {
            if (card.userId == DEFAULT_USER_ID) return
            SupabaseClientProvider.getClient().postgrest["personal_cards"].upsert(PersonalCardDto(
                id = card.id, userId = card.userId,
                cardType = card.cardType.toDbString(), title = card.title,
                content = card.content, icon = card.icon, color = card.color,
                imageUrl = card.imageUrl, cardShape = card.cardShape,
                isActive = card.isActive, orderIndex = card.orderIndex,
                stackId = card.stackId, createdAt = card.createdAt, updatedAt = card.updatedAt
            ))
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to push card to Supabase", e)
        }
    }

    private suspend fun deleteCardFromSupabase(cardId: String) {
        try {
            SupabaseClientProvider.getClient().postgrest["personal_cards"].delete {
                filter { eq("id", cardId); eq("user_id", getCurrentUserId()) }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to delete card from Supabase", e)
        }
    }
}
