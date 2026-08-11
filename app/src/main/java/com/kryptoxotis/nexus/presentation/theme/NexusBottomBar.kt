package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Tab identifiers matched against the current nav route. */
object NexusTabs {
    const val WALLET = "card_wallet"
    const val CONTACTS = "contacts"
    const val PASSES = "business_passes"
    const val ACCOUNT = "accounts"
    const val SHARE = "share_card"
}

/** Navigation handles the bottom bar needs; provided by MainActivity around the NavHost. */
data class NexusNavActions(
    val currentRoute: String?,
    val navigate: (String) -> Unit
)

val LocalNexusNav = staticCompositionLocalOf<NexusNavActions?> { null }

private val BarBackground = Color(0xFF0D0D0D)
private val BarBorder = Color(0xFF191919)

/**
 * Four destinations plus the raised NFC share action in the center:
 * Wallet · Contacts · [share] · Passes · Account. Admin is NOT a tab —
 * it lives as a row inside the Account screen.
 */
@Composable
fun NexusBottomBar(nav: NexusNavActions) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BarBorder)
                .padding(top = 1.dp)
                .background(BarBackground)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NexusBarItem(
                label = "Wallet",
                filled = Icons.Filled.CreditCard,
                outlined = Icons.Outlined.CreditCard,
                selected = nav.currentRoute == NexusTabs.WALLET,
                onClick = { nav.navigate(NexusTabs.WALLET) },
                modifier = Modifier.weight(1f)
            )
            NexusBarItem(
                label = "Contacts",
                filled = Icons.Filled.People,
                outlined = Icons.Outlined.People,
                selected = nav.currentRoute == NexusTabs.CONTACTS,
                onClick = { nav.navigate(NexusTabs.CONTACTS) },
                modifier = Modifier.weight(1f)
            )
            // Center slot reserved for the raised share button
            Box(modifier = Modifier.weight(1f))
            NexusBarItem(
                label = "Passes",
                filled = Icons.Filled.Badge,
                outlined = Icons.Outlined.Badge,
                selected = nav.currentRoute == NexusTabs.PASSES,
                onClick = { nav.navigate(NexusTabs.PASSES) },
                modifier = Modifier.weight(1f)
            )
            NexusBarItem(
                label = "Account",
                filled = Icons.Filled.AccountCircle,
                outlined = Icons.Outlined.AccountCircle,
                selected = nav.currentRoute == NexusTabs.ACCOUNT,
                onClick = { nav.navigate(NexusTabs.ACCOUNT) },
                modifier = Modifier.weight(1f)
            )
        }
        // Raised NFC share action — sits above the bar
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(58.dp)
                .neuCircle(elevation = 8.dp, surfaceColor = NexusTeal)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { nav.navigate(NexusTabs.SHARE) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Nfc,
                contentDescription = "Tap to share",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun NexusBarItem(
    label: String,
    filled: ImageVector,
    outlined: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            if (selected) filled else outlined,
            contentDescription = label,
            tint = if (selected) NexusTeal else NexusTextTertiary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected) NexusTeal else NexusTextTertiary
        )
    }
}
