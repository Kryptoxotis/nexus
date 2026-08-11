package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Routes the shell needs to know about. */
object NexusTabs {
    const val HOME_CARDS = "card_wallet"
    const val HOME_NEXUS = "contacts"
    const val HOME_PASSES = "business_passes"
    const val ACCOUNT = "accounts"
    const val SCAN = "scan_card"
}

/** Navigation handles for the shell; provided by MainActivity around the NavHost. */
data class NexusNavActions(
    val currentRoute: String?,
    val navigate: (String) -> Unit
)

val LocalNexusNav = staticCompositionLocalOf<NexusNavActions?> { null }

/**
 * Home's only bottom control: a floating scan glyph. No bar, no pill, no
 * label — it hovers over the content so nothing underneath gets walled off.
 */
@Composable
fun NexusBottomBar(nav: NexusNavActions) {
    Box(
        modifier = Modifier
            .padding(bottom = 18.dp)
            .size(56.dp)
            .neuCircle(elevation = 8.dp, surfaceColor = NexusRaised, neonColor = NexusTeal)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { nav.navigate(NexusTabs.SCAN) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Nfc,
            contentDescription = "Tap to receive",
            tint = NexusTeal,
            modifier = Modifier.size(26.dp)
        )
    }
}
