package com.kryptoxotis.nexus.domain.model

enum class CardType {
    LINK, FILE, CONTACT, SOCIAL_MEDIA, CUSTOM;

    companion object {
        fun fromString(value: String): CardType {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: CUSTOM
        }
    }

    fun toDbString(): String = name.lowercase()
}

data class PersonalCard(
    val id: String,
    val userId: String,
    val cardType: CardType = CardType.CUSTOM,
    val title: String,
    val content: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val imageUrl: String? = null,
    val cardShape: String = "card",
    val isActive: Boolean = false,
    val orderIndex: Int = 0,
    val createdAt: String,
    val updatedAt: String
) {
    fun displayContent(): String = when (cardType) {
        CardType.LINK -> content ?: ""
        CardType.FILE -> title
        CardType.CONTACT -> content ?: ""
        CardType.SOCIAL_MEDIA -> content ?: ""
        CardType.CUSTOM -> content ?: title
    }
}
