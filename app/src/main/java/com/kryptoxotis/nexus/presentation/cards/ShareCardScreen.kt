package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.presentation.theme.Dimens
import com.kryptoxotis.nexus.presentation.theme.NexusScaffold
import com.kryptoxotis.nexus.presentation.theme.NexusSurface
import com.kryptoxotis.nexus.presentation.theme.NexusTextSecondary
import com.kryptoxotis.nexus.presentation.theme.neuInset

/**
 * Full-screen tap-to-share moment: concentric animated rings in the active
 * card's color around a neuInset NFC circle. The phone is already emulating
 * the active card over HCE; this screen is the presentation of that state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareCardScreen(
    viewModel: PersonalCardViewModel,
    onNavigateBack: () -> Unit
) {
    val activeCard by viewModel.activeCard.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val card = activeCard ?: cards.firstOrNull()
    val style = remember(card?.color) { resolveCardStyle(card?.color) }
    var showQrSheet by remember { mutableStateOf(false) }

    NexusScaffold(
        title = "Share",
        onBack = onNavigateBack,
        bottomBar = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val rings = rememberInfiniteTransition(label = "shareRings")
            val phase by rings.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
                label = "ringPhase"
            )

            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(300.dp)) {
                    val maxRadius = size.minDimension / 2f
                    val baseRadius = 63.dp.toPx()
                    for (i in 0 until 3) {
                        val t = (phase + i / 3f) % 1f
                        val radius = baseRadius + (maxRadius - baseRadius) * t
                        drawCircle(
                            color = style.bright.copy(alpha = (1f - t) * 0.35f),
                            radius = radius,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .neuInset(cornerRadius = 63.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Nfc,
                        contentDescription = null,
                        tint = style.bright,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Hold near their phone",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (card != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sharing “${card.title}”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NexusTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Show QR instead",
                style = MaterialTheme.typography.titleSmall,
                color = style.bright,
                modifier = Modifier
                    .clickable { showQrSheet = true }
                    .padding(12.dp)
            )
        }
    }

    if (showQrSheet && card != null) {
        ModalBottomSheet(
            onDismissRequest = { showQrSheet = false },
            containerColor = NexusSurface,
            tonalElevation = 0.dp
        ) {
            CardQrSheet(card = card)
        }
    }
}
