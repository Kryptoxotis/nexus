package com.kryptoxotis.nexus.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlUtilsTest {

    // ── hasBlockedScheme ──

    @Test
    fun `blocks javascript scheme`() {
        assertTrue(UrlUtils.hasBlockedScheme("javascript:alert(1)"))
    }

    @Test
    fun `blocks data scheme`() {
        assertTrue(UrlUtils.hasBlockedScheme("data:text/html,<h1>Hi</h1>"))
    }

    @Test
    fun `blocks file scheme`() {
        assertTrue(UrlUtils.hasBlockedScheme("file:///etc/passwd"))
    }

    @Test
    fun `blocks content scheme`() {
        assertTrue(UrlUtils.hasBlockedScheme("content://contacts/people"))
    }

    @Test
    fun `blocks intent scheme`() {
        assertTrue(UrlUtils.hasBlockedScheme("intent://scan/#Intent"))
    }

    @Test
    fun `blocks blob scheme`() {
        assertTrue(UrlUtils.hasBlockedScheme("blob:http://example.com/uuid"))
    }

    @Test
    fun `blocks vbscript scheme`() {
        assertTrue(UrlUtils.hasBlockedScheme("vbscript:MsgBox"))
    }

    @Test
    fun `blocks tel scheme`() {
        assertTrue(UrlUtils.hasBlockedScheme("tel:+1234567890"))
    }

    @Test
    fun `blocks sms scheme`() {
        assertTrue(UrlUtils.hasBlockedScheme("sms:+1234567890"))
    }

    @Test
    fun `blocks mailto scheme`() {
        assertTrue(UrlUtils.hasBlockedScheme("mailto:user@example.com"))
    }

    @Test
    fun `blocks market scheme`() {
        assertTrue(UrlUtils.hasBlockedScheme("market://details?id=com.app"))
    }

    @Test
    fun `allows https URLs`() {
        assertFalse(UrlUtils.hasBlockedScheme("https://example.com"))
    }

    @Test
    fun `allows http URLs`() {
        assertFalse(UrlUtils.hasBlockedScheme("http://example.com"))
    }

    @Test
    fun `handles empty string`() {
        assertFalse(UrlUtils.hasBlockedScheme(""))
    }

    @Test
    fun `blocks mixed case JavaScript`() {
        assertTrue(UrlUtils.hasBlockedScheme("JavaScript:alert(1)"))
    }

    @Test
    fun `blocks uppercase DATA`() {
        assertTrue(UrlUtils.hasBlockedScheme("DATA:text/html,test"))
    }

    @Test
    fun `blocks with leading spaces`() {
        assertTrue(UrlUtils.hasBlockedScheme("   javascript:alert(1)"))
    }

    @Test
    fun `blocks with leading tab`() {
        assertTrue(UrlUtils.hasBlockedScheme("\tjavascript:alert(1)"))
    }

    @Test
    fun `blocks with leading newline`() {
        assertTrue(UrlUtils.hasBlockedScheme("\njavascript:alert(1)"))
    }

    @Test
    fun `blocks with leading carriage return`() {
        assertTrue(UrlUtils.hasBlockedScheme("\rjavascript:alert(1)"))
    }

    // ── ensureHttps ──

    @Test
    fun `ensureHttps prepends https to bare domain`() {
        assertEquals("https://example.com", UrlUtils.ensureHttps("example.com"))
    }

    @Test
    fun `ensureHttps preserves existing https`() {
        assertEquals("https://example.com", UrlUtils.ensureHttps("https://example.com"))
    }

    @Test
    fun `ensureHttps upgrades http to https`() {
        assertEquals("https://example.com", UrlUtils.ensureHttps("http://example.com"))
    }

    @Test
    fun `ensureHttps handles uppercase HTTP`() {
        assertEquals("https://example.com", UrlUtils.ensureHttps("HTTP://example.com"))
    }

    @Test
    fun `ensureHttps handles uppercase HTTPS`() {
        // ensureHttps preserves the original string when the normalized scheme is https
        assertEquals("HTTPS://example.com", UrlUtils.ensureHttps("HTTPS://example.com"))
    }

    @Test
    fun `ensureHttps returns empty for blocked scheme`() {
        assertEquals("", UrlUtils.ensureHttps("javascript:alert(1)"))
    }

    @Test
    fun `ensureHttps handles empty string`() {
        assertEquals("", UrlUtils.ensureHttps(""))
    }

    @Test
    fun `blocks zero-width space before javascript`() {
        assertTrue(UrlUtils.hasBlockedScheme("\u200Bjavascript:alert(1)"))
    }

    @Test
    fun `blocks soft hyphen before javascript`() {
        assertTrue(UrlUtils.hasBlockedScheme("\u00ADjavascript:alert(1)"))
    }

    @Test
    fun `ensureHttps trims whitespace`() {
        assertEquals("https://example.com", UrlUtils.ensureHttps("  example.com  "))
    }

    // ── Fullwidth unicode normalization (NFKC) ──

    @Test
    fun `blocks fullwidth javascript via NFKC normalization`() {
        // Fullwidth "javascript:" -> NFKC normalizes to ASCII
        val fullwidthJs = "\uFF4A\uFF41\uFF56\uFF41\uFF53\uFF43\uFF52\uFF49\uFF50\uFF54\uFF1Aalert(1)"
        assertTrue(UrlUtils.hasBlockedScheme(fullwidthJs))
    }

    @Test
    fun `blocks mid-string invisible chars in scheme`() {
        // Zero-width space embedded within "javascript:" scheme
        assertTrue(UrlUtils.hasBlockedScheme("java\u200Bscript:alert(1)"))
    }

    @Test
    fun `ensureHttps blocks mid-string invisible chars`() {
        assertEquals("", UrlUtils.ensureHttps("java\u200Bscript:alert(1)"))
    }

    @Test
    fun `ensureHttps preserves original for uppercase HTTPS`() {
        // Original string preserved when scheme matches https after normalization
        assertEquals("HTTPS://example.com", UrlUtils.ensureHttps("HTTPS://example.com"))
    }
}
