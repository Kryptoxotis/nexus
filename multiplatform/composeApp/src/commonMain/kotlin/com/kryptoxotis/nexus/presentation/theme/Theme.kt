package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Every surface-level color is the same dark background.
// Material3's tonal elevation is disabled (surfaceTint = Transparent).
// All depth/3D comes from neumorphic shadows in NeuModifiers, not from color changes.
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
    surface = NexusBackground,
    onSurface = NexusTextPrimary,
    surfaceVariant = NexusBackground,
    onSurfaceVariant = NexusTextSecondary,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = NexusBackground,
    surfaceContainerLow = NexusBackground,
    surfaceContainer = NexusBackground,
    surfaceContainerHigh = NexusBackground,
    surfaceContainerHighest = NexusBackground,
    surfaceBright = NexusBackground,
    surfaceDim = NexusBackground,
    inverseSurface = NexusTextPrimary,
    inverseOnSurface = NexusBackground,
    inversePrimary = NexusTealLight,
    error = NexusError,
    onError = NexusTextPrimary,
    errorContainer = Color(0xFF3A1515),
    onErrorContainer = NexusError,
    outline = NexusBorder,
    outlineVariant = Color(0xFF222222),
    scrim = Color.Black
)

@Composable
fun NexusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NexusDarkColorScheme,
        typography = Typography,
        content = content
    )
}
