package com.kryptoxotis.nexus.presentation.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusCardColorsTest {

    @Test
    fun `parse null returns default palette entry and light mode`() {
        val (hex, isDark) = NexusCardColors.parse(null)
        assertEquals("#0A7968", hex)
        assertFalse(isDark)
    }

    @Test
    fun `parse blank string returns default`() {
        val (hex, isDark) = NexusCardColors.parse("  ")
        assertEquals("#0A7968", hex)
        assertFalse(isDark)
    }

    @Test
    fun `parse hex only returns light mode`() {
        val (hex, isDark) = NexusCardColors.parse("#FF1744")
        assertEquals("#FF1744", hex)
        assertFalse(isDark)
    }

    @Test
    fun `parse hex with dark suffix returns dark mode`() {
        val (hex, isDark) = NexusCardColors.parse("#FF1744:dark")
        assertEquals("#FF1744", hex)
        assertTrue(isDark)
    }

    @Test
    fun `parse colon-only string returns default hex`() {
        val (hex, isDark) = NexusCardColors.parse(":")
        assertEquals("#0A7968", hex)
        assertFalse(isDark)
    }

    @Test
    fun `parse string starting with colon returns default hex with dark`() {
        val (hex, isDark) = NexusCardColors.parse(":dark")
        assertEquals("#0A7968", hex)
        assertTrue(isDark)
    }

    @Test
    fun `encode light mode returns hex only`() {
        assertEquals("#0A7968", NexusCardColors.encode("#0A7968", false))
    }

    @Test
    fun `encode dark mode appends dark suffix`() {
        assertEquals("#0A7968:dark", NexusCardColors.encode("#0A7968", true))
    }

    @Test
    fun `round-trip encode then parse preserves light mode`() {
        val encoded = NexusCardColors.encode("#F95B1A", false)
        val (hex, isDark) = NexusCardColors.parse(encoded)
        assertEquals("#F95B1A", hex)
        assertFalse(isDark)
    }

    @Test
    fun `round-trip encode then parse preserves dark mode`() {
        val encoded = NexusCardColors.encode("#F95B1A", true)
        val (hex, isDark) = NexusCardColors.parse(encoded)
        assertEquals("#F95B1A", hex)
        assertTrue(isDark)
    }

    @Test
    fun `findByHex returns matching palette entry`() {
        val result = NexusCardColors.findByHex("#0A7968")
        assertEquals("Kryptoxotis Teal", result?.name)
    }

    @Test
    fun `findByHex is case insensitive`() {
        val result = NexusCardColors.findByHex("#0a7968")
        assertEquals("Kryptoxotis Teal", result?.name)
    }

    @Test
    fun `findByHex returns null for unknown hex`() {
        assertNull(NexusCardColors.findByHex("#FFFFFF"))
    }

    @Test
    fun `findByHex returns null for empty string`() {
        assertNull(NexusCardColors.findByHex(""))
    }

    @Test
    fun `palette is not empty and all entries have non-blank names`() {
        assertTrue(NexusCardColors.palette.isNotEmpty())
        NexusCardColors.palette.forEach { assertTrue(it.name.isNotBlank()) }
    }

    @Test
    fun `all palette brightHex values are unique`() {
        val hexes = NexusCardColors.palette.map { it.brightHex.uppercase() }
        assertEquals(hexes.size, hexes.toSet().size)
    }

    @Test
    fun `parse with extra colons still parses correctly`() {
        val (hex, isDark) = NexusCardColors.parse("#FF1744:dark:extra")
        assertEquals("#FF1744", hex)
        assertTrue(isDark)
    }

    @Test
    fun `findByHex returns null for dark variant hex`() {
        // Dark variants are not indexed — only brightHex is searchable
        assertNull(NexusCardColors.findByHex("#064D42"))
    }

    @Test
    fun `all palette entries have distinct bright and dark colors`() {
        NexusCardColors.palette.forEach { entry ->
            assertTrue(
                "${entry.name} bright and dark should differ",
                entry.bright != entry.dark
            )
        }
    }
}
