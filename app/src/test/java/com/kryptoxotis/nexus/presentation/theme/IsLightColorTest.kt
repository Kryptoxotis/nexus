package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsLightColorTest {

    @Test
    fun `white is light`() {
        assertTrue(isLightColor(Color.White))
    }

    @Test
    fun `black is not light`() {
        assertFalse(isLightColor(Color.Black))
    }

    @Test
    fun `yellow is light`() {
        assertTrue(isLightColor(Color.Yellow))
    }

    @Test
    fun `navy is not light`() {
        assertFalse(isLightColor(Color(0xFF000080)))
    }

    @Test
    fun `mid gray is borderline dark`() {
        // #808080 has luminance ~0.216 which is above 0.179 threshold
        assertTrue(isLightColor(Color(0xFF808080)))
    }

    @Test
    fun `pure red is light (luminance ~0_213)`() {
        // Red has high WCAG luminance weight (0.2126)
        assertTrue(isLightColor(Color.Red))
    }

    @Test
    fun `pure blue is not light (luminance ~0_072)`() {
        // Blue has low WCAG luminance weight (0.0722)
        assertFalse(isLightColor(Color.Blue))
    }

    @Test
    fun `pure green is light (luminance ~0_715)`() {
        // Green has the highest WCAG luminance weight (0.7152)
        assertTrue(isLightColor(Color.Green))
    }

    @Test
    fun `semi-transparent white still returns true`() {
        // isLightColor ignores alpha — tests the contract
        assertTrue(isLightColor(Color.White.copy(alpha = 0.1f)))
    }
}
