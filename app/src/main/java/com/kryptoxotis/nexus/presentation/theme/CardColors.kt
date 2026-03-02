package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.pow

/**
 * A named color pair for card rendering.
 * [bright] is the primary gradient stop, [dark] is the secondary.
 * [brightHex] is the serialized form of [bright] used in persistence.
 * [gradient] is a pre-computed linear gradient for rendering efficiency.
 */
data class NexusCardColor(
    val name: String,
    val bright: Color,
    val dark: Color,
    val brightHex: String,
    val gradient: Brush = Brush.linearGradient(listOf(bright, dark))
)

object NexusCardColors {

    val palette = listOf(
        NexusCardColor("Kryptoxotis Teal", Color(0xFF0A7968), Color(0xFF064D42), "#0A7968"),
        NexusCardColor("Blaze Orange",     Color(0xFFF95B1A), Color(0xFFA83A0E), "#F95B1A"),
        NexusCardColor("Deep Navy",        Color(0xFF3355CC), Color(0xFF11257E), "#3355CC"),
        NexusCardColor("Red",              Color(0xFFFF1744), Color(0xFFD50000), "#FF1744"),
        NexusCardColor("Forest Green",     Color(0xFF388E3C), Color(0xFF124116), "#388E3C"),
        NexusCardColor("Cyan",             Color(0xFF00E5FF), Color(0xFF00838F), "#00E5FF"),
        NexusCardColor("Purple",           Color(0xFFB388FF), Color(0xFF6A1B9A), "#B388FF"),
        NexusCardColor("Pink",             Color(0xFFFF4081), Color(0xFFAD1457), "#FF4081"),
    )

    /** Parse stored color string -> (hex, isDark). e.g. "#0AD7A5:dark" -> ("#0AD7A5", true) */
    fun parse(stored: String?): Pair<String, Boolean> {
        if (stored.isNullOrBlank()) return Pair(palette[0].brightHex, false)
        val parts = stored.split(":")
        val hex = parts[0].takeIf { it.isNotBlank() } ?: palette[0].brightHex
        val isDark = parts.getOrNull(1) == "dark"
        return Pair(hex, isDark)
    }

    /** Encode hex + isDark -> stored string. e.g. ("#0AD7A5", true) -> "#0AD7A5:dark" */
    fun encode(hex: String, isDark: Boolean): String {
        return if (isDark) "$hex:dark" else hex
    }

    /** Find palette entry by brightHex, or null for legacy colors */
    fun findByHex(hex: String): NexusCardColor? {
        return palette.find { it.brightHex.equals(hex, ignoreCase = true) }
    }
}

/**
 * Resolved visual appearance for rendering a card.
 * @property gradient Background fill.
 * @property textColor Foreground text.
 * @property borderColor Stroke around the card edge.
 * @property neonColor Glow/shadow halo color, may differ from borderColor in dark mode.
 */
data class CardAppearance(
    val gradient: Brush,
    val textColor: Color,
    val borderColor: Color,
    val neonColor: Color
)

/** Cached appearance for image-backed cards (dark scrim, white text, default neon) */
private val ImageCardAppearance = CardAppearance(
    gradient = Brush.linearGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Black.copy(alpha = 0.5f))),
    textColor = Color.White,
    borderColor = NexusPrimary,
    neonColor = NexusPrimary
)

/** Cached gradient for dark-mode cards — slightly lighter than app background so cards are visible */
private val DarkCardGradient = Brush.linearGradient(listOf(Color(0xFF1A1A1A), Color(0xFF111111)))

/** Regex for validating hex color strings before passing to native parseColor */
private val HEX_COLOR_REGEX = Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$")

/** Resolve the full visual appearance from a stored color string */
fun resolveCardAppearance(storedColor: String?, hasImage: Boolean = false): CardAppearance {
    if (hasImage) return ImageCardAppearance

    val (hex, isDark) = NexusCardColors.parse(storedColor)
    val paletteEntry = NexusCardColors.findByHex(hex)

    val baseColor = if (paletteEntry != null) {
        paletteEntry.bright
    } else {
        // Legacy fallback: validate hex format before calling native parseColor
        val safeHex = hex.trim()
        if (HEX_COLOR_REGEX.matches(safeHex)) {
            try {
                Color(android.graphics.Color.parseColor(safeHex))
            } catch (_: Exception) {
                NexusCardColors.palette[0].bright
            }
        } else {
            NexusCardColors.palette[0].bright
        }
    }

    val darkVariant = paletteEntry?.dark ?: baseColor.copy(alpha = 0.5f)

    return if (isDark) {
        // Dark mode: black bg, neon colored text + border
        CardAppearance(
            gradient = DarkCardGradient,
            textColor = baseColor,
            borderColor = baseColor,
            neonColor = baseColor
        )
    } else {
        // Light mode: gradient bg, pick whichever of black/white has higher contrast
        val textColor = contrastSafeTextColor(baseColor)
        CardAppearance(
            gradient = paletteEntry?.gradient ?: Brush.linearGradient(listOf(baseColor, darkVariant)),
            textColor = textColor,
            borderColor = baseColor,
            neonColor = baseColor
        )
    }
}

/**
 * Returns whichever of black or white has a higher contrast ratio against [background].
 * Uses WCAG relative luminance with proper sRGB linearisation.
 */
internal fun contrastSafeTextColor(background: Color): Color {
    val luminance = relativeLuminance(background)
    val contrastWithBlack = (luminance + 0.05) / (0.0 + 0.05)
    val contrastWithWhite = (1.0 + 0.05) / (luminance + 0.05)
    return if (contrastWithBlack > contrastWithWhite) Color.Black else Color.White
}

/**
 * Returns true if the color is too bright for white text overlay.
 * Uses WCAG relative luminance with proper sRGB linearisation.
 * Threshold 0.179 corresponds to a 4.5:1 contrast ratio against white.
 */
internal fun isLightColor(color: Color): Boolean {
    return relativeLuminance(color) > 0.179f
}

private fun relativeLuminance(color: Color): Float {
    fun linearize(c: Float): Float =
        if (c <= 0.04045f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
    return 0.2126f * linearize(color.red) + 0.7152f * linearize(color.green) + 0.0722f * linearize(color.blue)
}
