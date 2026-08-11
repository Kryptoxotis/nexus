package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

private val PillSurface = Color(0xFF141414)

/**
 * Home's only bottom control: a single floating pill that opens the QR scanner.
 * It floats over the page background like every other raised surface —
 * no bar strip, no seam.
 */
@Composable
fun NexusBottomBar(nav: NexusNavActions) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp, top = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(52.dp)
                .neuRaised(cornerRadius = 26.dp, surfaceColor = PillSurface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { nav.navigate(NexusTabs.SCAN) }
                .padding(horizontal = 26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.QrCodeScanner,
                contentDescription = null,
                tint = NexusTeal,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Scan a card",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFD0D0D0)
            )
        }
    }
}
