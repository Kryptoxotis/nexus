package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NexusDarkColorScheme = darkColorScheme(
    primary = NexusTeal,
    onPrimary = Color.White,
    primaryContainer = NexusTealDark,
    onPrimaryContainer = NexusTextPrimary,
    secondary = NexusOrange,
    onSecondary = Color.White,
    secondaryContainer = NexusOrangeDark,
    onSecondaryContainer = NexusTextPrimary,
    tertiary = NexusOrangeLight,
    onTertiary = NexusBackground,
    background = NexusBackground,
    onBackground = NexusTextPrimary,
    surface = NexusSurface,
    onSurface = NexusTextPrimary,
    surfaceVariant = NexusSurfaceVariant,
    onSurfaceVariant = NexusTextSecondary,
    error = NexusError,
    onError = NexusTextPrimary,
    errorContainer = Color(0xFF3A1515),
    onErrorContainer = NexusError,
    outline = NexusBorder
)

@Composable
fun NexusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NexusDarkColorScheme,
        typography = Typography,
        content = content
    )
}
