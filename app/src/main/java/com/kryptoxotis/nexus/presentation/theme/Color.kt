package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Live UI preferences. Snapshot-state backed so every composable (and draw
 * lambda) that reads a theme color re-renders the moment a setting flips.
 * SettingsStore loads/persists these.
 */
object NexusAppearance {
    var dark by mutableStateOf(true)
    var deckView by mutableStateOf(false)
    var nfcSharing by mutableStateOf(true)
    var notifications by mutableStateOf(true)
}

// Primary palette - Kryptoxotis Teal (same hue family in both themes)
val NexusTeal = Color(0xFF037A68)
val NexusTealLight = Color(0xFF0AD7A5)
val NexusTealDark = Color(0xFF025E50)
val NexusTealGlow = Color(0x59037A68) // 35% alpha

// Accent orange (admin/business contexts only)
val NexusOrange = Color(0xFFFA5700)
val NexusOrangeLight = Color(0xFFFF8C4A)
val NexusOrangeDark = Color(0xFFC44500)

// ── Dynamic surfaces: dark neumorphic black / pastel light ──
val NexusBackground: Color get() = if (NexusAppearance.dark) Color(0xFF0A0A0A) else Color(0xFFF3F1EC)
val NexusDeep: Color get() = if (NexusAppearance.dark) Color(0xFF111111) else Color(0xFFE9E6E0)
val NexusSurface: Color get() = if (NexusAppearance.dark) Color(0xFF1A1A1A) else Color(0xFFFBFAF7)
val NexusRaised: Color get() = if (NexusAppearance.dark) Color(0xFF141414) else Color(0xFFFAF8F4)
val NexusSurfaceVariant: Color get() = if (NexusAppearance.dark) Color(0xFF383838) else Color(0xFFD8D4CD)

// Alias for brand primary (used by CardColors, NeuModifiers)
val NexusPrimary = NexusTeal

// ── Dynamic text ──
val NexusTextPrimary: Color get() = if (NexusAppearance.dark) Color(0xFFEEEEEE) else Color(0xFF1F1E1B)
val NexusTextSecondary: Color get() = if (NexusAppearance.dark) Color(0xFF8A8A8A) else Color(0xFF6E6A63)
val NexusTextTertiary: Color get() = if (NexusAppearance.dark) Color(0xFF7D7D7D) else Color(0xFF8B867E)
val NexusControlText: Color get() = if (NexusAppearance.dark) Color(0xFFD0D0D0) else Color(0xFF3A3833)
val NexusMutedText: Color get() = if (NexusAppearance.dark) Color(0xFF6F6F6F) else Color(0xFF9B968E)

// ── Dynamic chrome ──
val NexusBorder: Color get() = if (NexusAppearance.dark) Color(0xFF383838) else Color(0xFFD8D4CD)
val NexusPillSurface: Color get() = if (NexusAppearance.dark) Color(0xFF1F1F1F) else Color(0xFFEAE6E0)
val NexusDotInactive: Color get() = if (NexusAppearance.dark) Color(0xFF333333) else Color(0xFFD5D1CA)

// Neumorphic shadow/highlight pair — the whole depth illusion flips with the theme
val NexusShadow: Color get() = if (NexusAppearance.dark) Color.Black else Color(0xFFB9B3A9)
val NexusHighlight: Color get() = if (NexusAppearance.dark) Color(0xFF2A2A2A) else Color.White

// Status
val NexusError = Color(0xFFFF3B30)
val NexusSuccess = Color(0xFF34C759)
val NexusWarning = Color(0xFFFFCC00)
