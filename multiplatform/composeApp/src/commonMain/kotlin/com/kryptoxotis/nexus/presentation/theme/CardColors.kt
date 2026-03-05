package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.pow

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

    fun parse(stored: String?): Pair<String, Boolean> {
        if (stored.isNullOrBlank()) return Pair(palette[0].brightHex, false)
        val parts = stored.split(":")
        val hex = parts[0].takeIf { it.isNotBlank() } ?: palette[0].brightHex
        val isDark = parts.getOrNull(1) == "dark"
        return Pair(hex, isDark)
    }

    fun encode(hex: String, isDark: Boolean): String =
        if (isDark) "$hex:dark" else hex

    fun findByHex(hex: String): NexusCardColor? =
        palette.find { it.brightHex.equals(hex, ignoreCase = true) }
}

data class CardAppearance(
    val gradient: Brush,
    val textColor: Color,
    val borderColor: Color,
    val neonColor: Color
)

private val ImageCardAppearance = CardAppearance(
    gradient = Brush.linearGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Black.copy(alpha = 0.5f))),
    textColor = Color.White,
    borderColor = NexusPrimary,
    neonColor = NexusPrimary
)

private val DarkCardGradient = Brush.linearGradient(listOf(Color(0xFF1A1A1A), Color(0xFF111111)))

private val HEX_COLOR_REGEX = Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$")

/** Parse hex color string to Compose Color without android.graphics.Color */
private fun parseHexColor(hex: String): Color? {
    val clean = hex.trim()
    if (!HEX_COLOR_REGEX.matches(clean)) return null
    return try {
        val colorLong = clean.removePrefix("#").toLong(16)
        if (clean.length == 7) { // #RRGGBB
            Color(
                red = ((colorLong shr 16) and 0xFF).toInt(),
                green = ((colorLong shr 8) and 0xFF).toInt(),
                blue = (colorLong and 0xFF).toInt()
            )
        } else { // #RRGGBBAA
            Color(
                red = ((colorLong shr 24) and 0xFF).toInt(),
                green = ((colorLong shr 16) and 0xFF).toInt(),
                blue = ((colorLong shr 8) and 0xFF).toInt(),
                alpha = (colorLong and 0xFF).toInt()
            )
        }
    } catch (_: Exception) { null }
}

fun resolveCardAppearance(storedColor: String?, hasImage: Boolean = false): CardAppearance {
    if (hasImage) return ImageCardAppearance

    val (hex, isDark) = NexusCardColors.parse(storedColor)
    val paletteEntry = NexusCardColors.findByHex(hex)

    val baseColor = paletteEntry?.bright
        ?: parseHexColor(hex)
        ?: NexusCardColors.palette[0].bright

    val darkVariant = paletteEntry?.dark ?: baseColor.copy(alpha = 0.5f)

    return if (isDark) {
        CardAppearance(DarkCardGradient, baseColor, baseColor, baseColor)
    } else {
        val textColor = contrastSafeTextColor(baseColor)
        CardAppearance(
            gradient = paletteEntry?.gradient ?: Brush.linearGradient(listOf(baseColor, darkVariant)),
            textColor = textColor, borderColor = baseColor, neonColor = baseColor
        )
    }
}

internal fun contrastSafeTextColor(background: Color): Color {
    val luminance = relativeLuminance(background)
    val contrastWithBlack = (luminance + 0.05) / (0.0 + 0.05)
    val contrastWithWhite = (1.0 + 0.05) / (luminance + 0.05)
    return if (contrastWithBlack > contrastWithWhite) Color.Black else Color.White
}

internal fun isLightColor(color: Color): Boolean =
    relativeLuminance(color) > 0.179f

private fun relativeLuminance(color: Color): Float {
    fun linearize(c: Float): Float =
        if (c <= 0.04045f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
    return 0.2126f * linearize(color.red) + 0.7152f * linearize(color.green) + 0.0722f * linearize(color.blue)
}
