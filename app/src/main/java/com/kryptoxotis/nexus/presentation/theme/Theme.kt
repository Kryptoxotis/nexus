package com.kryptoxotis.nexus.presentation.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.dp

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
    // Dialogs (surfaceContainerHigh) and sheets (surfaceContainerLow) both sit on #1A1A1A
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
