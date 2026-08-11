package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

private val BarBackground = Color(0xFF0D0D0D)
private val BarBorder = Color(0xFF191919)

/**
 * The bottom bar is NOT tabs — section switching happens in the home screen's
 * segmented switcher. This is a single raised Scan action that opens the QR scanner.
 */
@Composable
fun NexusBottomBar(nav: NexusNavActions) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BarBorder)
            .padding(top = 1.dp)
            .background(BarBackground)
            .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 32dp slot; the 58dp circle bottom-aligns so it overhangs 26dp above the bar
        Box(
            modifier = Modifier.size(width = 58.dp, height = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .neuInset(cornerRadius = 29.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { nav.navigate(NexusTabs.SCAN) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.QrCodeScanner,
                    contentDescription = "Scan a card",
                    tint = NexusTeal,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = "SCAN A CARD",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            color = NexusTextTertiary,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
