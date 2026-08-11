package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background

/**
 * The single screen shell: neumorphic header (back button, title, teal subtitle,
 * action buttons) over the app background. No screen uses a raw Material TopAppBar.
 */
@Composable
fun NexusScaffold(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: List<Pair<ImageVector, () -> Unit>> = emptyList(),
    bottomBar: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.gap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(Dimens.iconButton)
                        .neuCircle(elevation = 6.dp)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NexusTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.gap))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NexusTextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = NexusTeal
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gapSmall)) {
                actions.forEach { (icon, onClick) ->
                    Box(
                        modifier = Modifier
                            .size(Dimens.iconButton)
                            .neuCircle(elevation = 6.dp)
                            .clickable(onClick = onClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = NexusTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            content = content
        )
        val nav = LocalNexusNav.current
        if (bottomBar && nav != null) {
            NexusBottomBar(nav)
        }
    }
}
