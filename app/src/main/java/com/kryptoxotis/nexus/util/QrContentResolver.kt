package com.kryptoxotis.nexus.util

import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PersonalCard

object QrContentResolver {

    fun resolve(card: PersonalCard): String {
        val content = card.content ?: return card.title

        return when (card.cardType) {
            CardType.LINK -> resolveUrl(content)
            CardType.FILE -> resolveUrl(content)
            CardType.CONTACT -> resolveContact(card.title, content)
            CardType.SOCIAL_MEDIA -> resolveUrl(content)
            CardType.CUSTOM -> if (looksLikeUrl(content)) resolveUrl(content) else content
            CardType.BUSINESS_CARD -> BusinessCardData.fromJson(content).toVCard()
        }
    }

    private fun resolveUrl(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    private fun resolveContact(name: String, content: String): String {
        val lines = content.lines().map { it.trim() }.filter { it.isNotEmpty() }

        val sb = StringBuilder()
        sb.appendLine("BEGIN:VCARD")
        sb.appendLine("VERSION:3.0")
        sb.appendLine("FN:$name")

        for (line in lines) {
            when {
                line.contains("@") -> sb.appendLine("EMAIL:$line")
                line.matches(Regex("[+\\d\\s()-]{7,}")) -> sb.appendLine("TEL:$line")
                looksLikeUrl(line) -> sb.appendLine("URL:${resolveUrl(line)}")
                else -> sb.appendLine("NOTE:$line")
            }
        }

        sb.appendLine("END:VCARD")
        return sb.toString().trimEnd()
    }

    private fun looksLikeUrl(text: String): Boolean {
        val t = text.trim().lowercase()
        return t.startsWith("http://") || t.startsWith("https://") ||
                t.startsWith("www.")
    }
}
