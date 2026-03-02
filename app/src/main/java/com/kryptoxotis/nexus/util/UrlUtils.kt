package com.kryptoxotis.nexus.util

import java.text.Normalizer

/** URL sanitization utilities to prevent injection of dangerous URI schemes (XSS, local file access, intent hijacking). */
object UrlUtils {
    /** URI schemes that must be rejected to prevent script injection, local file access, or intent-based attacks. */
    private val BLOCKED_SCHEMES = setOf(
        "javascript:", "data:", "file:", "content:", "intent:",
        "blob:", "vbscript:", "tel:", "sms:", "mailto:", "market:"
    )

    private val INVISIBLE_CHARS_REGEX =
        Regex("[\\s\\u00A0\\x00-\\x1f\\u200B-\\u200F\\u202A-\\u202E\\u00AD\\uFEFF]")

    /**
     * Returns true if the URL uses a blocked scheme.
     * Normalizes to NFKC (collapses fullwidth chars to ASCII equivalents),
     * then strips all whitespace, control characters, and Unicode invisible/format chars
     * from the entire string before checking.
     */
    fun hasBlockedScheme(url: String): Boolean {
        val normalized = Normalizer.normalize(url, Normalizer.Form.NFKC)
        val cleaned = normalized.replace(INVISIBLE_CHARS_REGEX, "").lowercase()
        return BLOCKED_SCHEMES.any { cleaned.startsWith(it) }
    }

    /** Strips invisible/control chars and lowercases for safe scheme comparison. */
    private fun normalizeForSchemeCheck(url: String): String {
        val normalized = Normalizer.normalize(url, Normalizer.Form.NFKC)
        return normalized.replace(INVISIBLE_CHARS_REGEX, "").lowercase()
    }

    /**
     * Ensures the URL uses HTTPS. Upgrades http:// to https:// and prepends
     * https:// to schemeless URLs. Returns empty string for blocked schemes or blank input.
     */
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
