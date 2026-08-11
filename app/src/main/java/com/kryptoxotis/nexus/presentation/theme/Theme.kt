package com.kryptoxotis.nexus.presentation.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

@Composable
fun NexusTheme(
    content: @Composable () -> Unit
) {
    // Reading NexusAppearance.dark here subscribes the whole tree —
    // flipping the setting rebuilds the scheme instantly
    val isDark = NexusAppearance.dark
    val colorScheme = if (isDark) {
        darkColorScheme(
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
            surfaceContainerLowest = NexusDeep,
            surfaceContainerLow = NexusSurface,
            surfaceContainer = NexusSurface,
            surfaceContainerHigh = NexusSurface,
            surfaceContainerHighest = NexusSurface,
            surfaceVariant = NexusSurfaceVariant,
            onSurfaceVariant = NexusTextSecondary,
            error = NexusError,
            onError = NexusTextPrimary,
            errorContainer = Color(0xFF3A1515),
            onErrorContainer = NexusError,
            outline = NexusBorder
        )
    } else {
        lightColorScheme(
            primary = NexusTeal,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFCDE8E2),
            onPrimaryContainer = NexusTealDark,
            secondary = NexusOrange,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFBE0CF),
            onSecondaryContainer = NexusOrangeDark,
            tertiary = NexusOrangeDark,
            onTertiary = Color.White,
            background = NexusBackground,
            onBackground = NexusTextPrimary,
            surface = NexusSurface,
            onSurface = NexusTextPrimary,
            surfaceContainerLowest = NexusDeep,
            surfaceContainerLow = NexusSurface,
            surfaceContainer = NexusSurface,
            surfaceContainerHigh = NexusSurface,
            surfaceContainerHighest = NexusSurface,
            surfaceVariant = NexusSurfaceVariant,
            onSurfaceVariant = NexusTextSecondary,
            error = NexusError,
            onError = Color.White,
            errorContainer = Color(0xFFF7D9D6),
            onErrorContainer = Color(0xFF8F1D14),
            outline = NexusBorder
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        // Dialogs and bottom sheets take extraLarge; cards/controls take medium/small.
        shapes = Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(14.dp),
            large = RoundedCornerShape(18.dp),
            extraLarge = RoundedCornerShape(18.dp)
        ),
        content = content
    )
}
