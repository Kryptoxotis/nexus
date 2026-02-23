package com.kryptoxotis.nexus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PersonalCard

@Entity(tableName = "personal_cards")
data class PersonalCardEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val cardType: String,
    val title: String,
    val content: String?,
    val icon: String?,
    val color: String?,
    val imageUrl: String?,
    val isActive: Boolean,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String
) {
    fun toDomain(): PersonalCard = PersonalCard(
        id = id,
        userId = userId,
        cardType = CardType.fromString(cardType),
        title = title,
        content = content,
        icon = icon,
        color = color,
        imageUrl = imageUrl,
        isActive = isActive,
        orderIndex = orderIndex,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(card: PersonalCard): PersonalCardEntity = PersonalCardEntity(
            id = card.id,
            userId = card.userId,
            cardType = card.cardType.toDbString(),
            title = card.title,
            content = card.content,
            icon = card.icon,
            color = card.color,
            imageUrl = card.imageUrl,
            isActive = card.isActive,
            orderIndex = card.orderIndex,
            createdAt = card.createdAt,
            updatedAt = card.updatedAt
        )
    }
}
