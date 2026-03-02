package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ResolveCardAppearanceTest {

    @Test
    fun `hasImage returns white text color`() {
        val appearance = resolveCardAppearance("#FF1744", hasImage = true)
        assertEquals(Color.White, appearance.textColor)
    }

    @Test
    fun `hasImage ignores stored color`() {
        val a1 = resolveCardAppearance("#FF1744", hasImage = true)
        val a2 = resolveCardAppearance("#0A7968", hasImage = true)
        assertEquals(a1.textColor, a2.textColor)
        assertEquals(a1.borderColor, a2.borderColor)
    }

    @Test
    fun `known palette hex in light mode returns non-null appearance`() {
        val appearance = resolveCardAppearance("#0A7968")
        assertNotNull(appearance.gradient)
        assertNotNull(appearance.textColor)
    }

    @Test
    fun `dark mode suffix returns colored text on dark bg`() {
        val appearance = resolveCardAppearance("#FF1744:dark")
        // In dark mode, text color should be the base color (red)
        assertEquals(Color(0xFFFF1744), appearance.textColor)
    }

    @Test
    fun `null storedColor uses default palette entry`() {
        val appearance = resolveCardAppearance(null)
        assertNotNull(appearance.gradient)
    }

    @Test
    fun `invalid hex falls back to default palette`() {
        val appearance = resolveCardAppearance("notacolor")
        assertNotNull(appearance.gradient)
    }

    @Test
    fun `blank string uses default`() {
        val appearance = resolveCardAppearance("")
        assertNotNull(appearance.gradient)
    }

    // ── contrastSafeTextColor ──

    @Test
    fun `contrastSafeTextColor returns black for white background`() {
        assertEquals(Color.Black, contrastSafeTextColor(Color.White))
    }

    @Test
    fun `contrastSafeTextColor returns white for black background`() {
        assertEquals(Color.White, contrastSafeTextColor(Color.Black))
    }

    @Test
    fun `contrastSafeTextColor returns black for cyan`() {
        // Cyan (#00E5FF) is bright — black text should have better contrast
        assertEquals(Color.Black, contrastSafeTextColor(Color(0xFF00E5FF)))
    }

    @Test
    fun `contrastSafeTextColor returns white for deep navy`() {
        assertEquals(Color.White, contrastSafeTextColor(Color(0xFF3355CC)))
    }

    @Test
    fun `contrastSafeTextColor returns black for yellow`() {
        assertEquals(Color.Black, contrastSafeTextColor(Color.Yellow))
    }
}
