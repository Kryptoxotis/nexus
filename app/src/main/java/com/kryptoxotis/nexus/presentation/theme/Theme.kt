package com.kryptoxotis.nexus.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NexusDarkColorScheme = darkColorScheme(
    primary = NexusOrange,
    onPrimary = Color.White,
    primaryContainer = NexusOrangeDark,
    onPrimaryContainer = NexusTextPrimary,
    secondary = NexusBlue,
    onSecondary = Color.White,
    secondaryContainer = NexusBlueDark,
    onSecondaryContainer = NexusTextPrimary,
    tertiary = NexusBlueLight,
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
fun NexusTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = NexusDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
