package com.kryptoxotis.nexus.domain.model

import org.json.JSONObject

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
    val github: String = ""
) {
    fun toJson(): String {
        val obj = JSONObject()
        obj.put("name", name)
        obj.put("jobTitle", jobTitle)
        obj.put("company", company)
        obj.put("phone", phone)
        obj.put("email", email)
        obj.put("website", website)
        obj.put("address", address)
        obj.put("linkedin", linkedin)
        obj.put("instagram", instagram)
        obj.put("twitter", twitter)
        obj.put("github", github)
        return obj.toString()
    }

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
        fun fromJson(json: String): BusinessCardData {
            return try {
                val obj = JSONObject(json)
                BusinessCardData(
                    name = obj.optString("name", ""),
                    jobTitle = obj.optString("jobTitle", ""),
                    company = obj.optString("company", ""),
                    phone = obj.optString("phone", ""),
                    email = obj.optString("email", ""),
                    website = obj.optString("website", ""),
                    address = obj.optString("address", ""),
                    linkedin = obj.optString("linkedin", ""),
                    instagram = obj.optString("instagram", ""),
                    twitter = obj.optString("twitter", ""),
                    github = obj.optString("github", "")
                )
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
