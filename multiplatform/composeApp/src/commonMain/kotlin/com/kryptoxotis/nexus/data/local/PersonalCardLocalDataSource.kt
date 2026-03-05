package com.kryptoxotis.nexus.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PersonalCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PersonalCardLocalDataSource(private val db: NexusDatabase) {

    private val queries get() = db.personalCardQueries

    fun observeCardsByUser(userId: String): Flow<List<PersonalCard>> =
        queries.selectAll(userId).asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toDomain() }
        }

    fun observeActiveCard(userId: String): Flow<PersonalCard?> =
        queries.selectActive(userId).asFlow().mapToOneOrNull(Dispatchers.Default).map { it?.toDomain() }

    fun getCardsByUser(userId: String): List<PersonalCard> =
        queries.selectAll(userId).executeAsList().map { it.toDomain() }

    fun getCardById(id: String): PersonalCard? =
        queries.selectById(id).executeAsOneOrNull()?.toDomain()

    fun getActiveCard(userId: String): PersonalCard? =
        queries.selectActive(userId).executeAsOneOrNull()?.toDomain()

    fun insertCard(card: PersonalCard) {
        queries.insert(
            id = card.id,
            userId = card.userId,
            cardType = card.cardType.toDbString(),
            title = card.title,
            content = card.content,
            icon = card.icon,
            color = card.color,
            imageUrl = card.imageUrl,
            cardShape = card.cardShape,
            isActive = if (card.isActive) 1L else 0L,
            orderIndex = card.orderIndex.toLong(),
            stackId = card.stackId,
            createdAt = card.createdAt,
            updatedAt = card.updatedAt
        )
    }

    fun updateCard(id: String, title: String, content: String?, color: String?, cardShape: String, updatedAt: String) {
        queries.updateCard(title, content, color, cardShape, updatedAt, id)
    }

    fun activateCard(userId: String, cardId: String, updatedAt: String) {
        queries.transaction {
            queries.deactivateAll(updatedAt, userId)
            queries.activate(updatedAt, cardId)
        }
    }

    fun deactivateAll(userId: String, updatedAt: String) {
        queries.deactivateAll(updatedAt, userId)
    }

    fun deleteCard(id: String) {
        queries.deleteById(id)
    }

    fun deleteAllForUser(userId: String) {
        queries.deleteAllForUser(userId)
    }

    fun updateOrderIndex(id: String, orderIndex: Int, updatedAt: String) {
        queries.updateOrderIndex(orderIndex.toLong(), updatedAt, id)
    }

    fun updateStackId(id: String, stackId: String?, updatedAt: String) {
        queries.updateStackId(stackId, updatedAt, id)
    }

    private fun com.kryptoxotis.nexus.data.local.PersonalCard.toDomain(): PersonalCard = PersonalCard(
        id = id,
        userId = userId,
        cardType = CardType.fromString(cardType),
        title = title,
        content = content,
        icon = icon,
        color = color,
        imageUrl = imageUrl,
        cardShape = cardShape,
        isActive = isActive != 0L,
        orderIndex = orderIndex.toInt(),
        stackId = stackId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
