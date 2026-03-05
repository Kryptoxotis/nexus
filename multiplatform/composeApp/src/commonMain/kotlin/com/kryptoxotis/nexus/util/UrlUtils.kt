package com.kryptoxotis.nexus.util

import com.kryptoxotis.nexus.platform.normalizeNfkc

object UrlUtils {
    private val BLOCKED_SCHEMES = setOf(
        "javascript:", "data:", "file:", "content:", "intent:",
        "blob:", "vbscript:", "tel:", "sms:", "mailto:", "market:"
    )

    private val INVISIBLE_CHARS_REGEX =
        Regex("[\\s\\u00A0\\x00-\\x1f\\u200B-\\u200F\\u202A-\\u202E\\u00AD\\uFEFF]")

    fun hasBlockedScheme(url: String): Boolean {
        val normalized = normalizeNfkc(url)
        val cleaned = normalized.replace(INVISIBLE_CHARS_REGEX, "").lowercase()
        return BLOCKED_SCHEMES.any { cleaned.startsWith(it) }
    }

    private fun normalizeForSchemeCheck(url: String): String {
        val normalized = normalizeNfkc(url)
        return normalized.replace(INVISIBLE_CHARS_REGEX, "").lowercase()
    }

    fun ensureHttps(url: String): String {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return ""
        if (hasBlockedScheme(trimmed)) return ""
        val cleaned = normalizeForSchemeCheck(trimmed)
        return when {
            cleaned.startsWith("https://") -> trimmed
            cleaned.startsWith("http://") -> "https://" + trimmed.substring(trimmed.indexOf("://") + 3)
            else -> "https://$trimmed"
        }
    }
}
