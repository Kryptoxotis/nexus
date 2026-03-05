package com.kryptoxotis.nexus.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

enum class CardType {
    LINK, FILE, CONTACT, SOCIAL_MEDIA, CUSTOM, BUSINESS_CARD;

    companion object {
        fun fromString(value: String): CardType {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: CUSTOM
        }
    }

    fun toDbString(): String = name.lowercase()
}

@Serializable
data class BusinessCardData(
    val name: String = "",
    val jobTitle: String = "",
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val address: String = "",
    val linkedin: String = "",
    val instagram: String = "",
    val twitter: String = "",
    val github: String = "",
    val facebook: String = "",
    val youtube: String = "",
    val tiktok: String = "",
    val discord: String = "",
    val twitch: String = "",
    val whatsapp: String = "",
    val organizationId: String = ""
) {
    fun toJson(): String = json.encodeToString(this)

    fun toVCard(): String {
        val sb = StringBuilder()
        sb.appendLine("BEGIN:VCARD")
        sb.appendLine("VERSION:3.0")
        if (name.isNotBlank()) sb.appendLine("FN:$name")
        if (jobTitle.isNotBlank()) sb.appendLine("TITLE:$jobTitle")
        if (company.isNotBlank()) sb.appendLine("ORG:$company")
        if (phone.isNotBlank()) sb.appendLine("TEL:$phone")
        if (email.isNotBlank()) sb.appendLine("EMAIL:$email")
        if (website.isNotBlank()) sb.appendLine("URL:$website")
        if (address.isNotBlank()) sb.appendLine("ADR:;;$address;;;;")
        if (linkedin.isNotBlank()) sb.appendLine("X-SOCIALPROFILE;type=linkedin:$linkedin")
        if (instagram.isNotBlank()) sb.appendLine("X-SOCIALPROFILE;type=instagram:$instagram")
        if (twitter.isNotBlank()) sb.appendLine("X-SOCIALPROFILE;type=twitter:$twitter")
        if (github.isNotBlank()) sb.appendLine("X-SOCIALPROFILE;type=github:$github")
        if (facebook.isNotBlank()) sb.appendLine("X-SOCIALPROFILE;type=facebook:$facebook")
        if (youtube.isNotBlank()) sb.appendLine("X-SOCIALPROFILE;type=youtube:$youtube")
        if (tiktok.isNotBlank()) sb.appendLine("X-SOCIALPROFILE;type=tiktok:$tiktok")
        if (discord.isNotBlank()) sb.appendLine("X-SOCIALPROFILE;type=discord:$discord")
        if (twitch.isNotBlank()) sb.appendLine("X-SOCIALPROFILE;type=twitch:$twitch")
        if (whatsapp.isNotBlank()) sb.appendLine("X-SOCIALPROFILE;type=whatsapp:$whatsapp")
        if (organizationId.isNotBlank()) sb.appendLine("X-NEXUS-ORG:$organizationId")
        sb.appendLine("END:VCARD")
        return sb.toString().trimEnd()
    }

    fun subtitle(): String {
        return listOfNotNull(
            jobTitle.ifBlank { null },
            company.ifBlank { null }
        ).joinToString(" at ").ifBlank { name }
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(jsonStr: String): BusinessCardData {
            return try {
                json.decodeFromString<BusinessCardData>(jsonStr)
            } catch (_: Exception) {
                BusinessCardData()
            }
        }
    }
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
    val stackId: String? = null,
    val createdAt: String,
    val updatedAt: String
) {
    fun displayContent(): String = when (cardType) {
        CardType.LINK -> content ?: ""
        CardType.FILE -> title
        CardType.CONTACT -> content ?: ""
        CardType.SOCIAL_MEDIA -> content ?: ""
        CardType.CUSTOM -> content ?: title
        CardType.BUSINESS_CARD -> {
            val data = BusinessCardData.fromJson(content ?: "")
            "${data.name} - ${data.jobTitle}".trim(' ', '-')
        }
    }
}
