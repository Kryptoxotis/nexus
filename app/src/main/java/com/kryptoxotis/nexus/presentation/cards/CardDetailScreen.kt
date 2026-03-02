package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kryptoxotis.nexus.presentation.theme.NexusBackground
import com.kryptoxotis.nexus.presentation.theme.neonGlow
import com.kryptoxotis.nexus.presentation.theme.neuCircle
import com.kryptoxotis.nexus.presentation.theme.resolveCardAppearance

@Composable
fun CardDetailScreen(
    cardId: String,
    viewModel: PersonalCardViewModel,
    onNavigateBack: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val card = cards.find { it.id == cardId }

    DisposableEffect(cardId) {
        onDispose { viewModel.deactivateCard(cardId) }
    }

    val appearance = remember(card?.color, card?.imageUrl) {
        resolveCardAppearance(card?.color, hasImage = card?.imageUrl != null)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onNavigateBack() }
    ) {
        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(start = 8.dp, top = 40.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Back", tint = Color(0xFF888888))
        }

        if (card == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Card not found", color = Color(0xFF666666))
            }
            return@Box
        }

        val isCoin = card.cardShape == "coin"

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isCoin) {
                // Coin shape — circle with glow
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .neonGlow(appearance.neonColor, cornerRadius = 80.dp, elevation = 14.dp)
                        .clip(CircleShape)
                        .background(appearance.gradient)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* consume */ },
                    contentAlignment = Alignment.Center
                ) {
                    if (card.imageUrl != null) {
                        AsyncImage(
                            model = card.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                    }
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = appearance.textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                // Card shape — rectangle with glow
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .neonGlow(appearance.neonColor, cornerRadius = 16.dp, elevation = 14.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* consume */ },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(appearance.borderColor, appearance.borderColor.copy(alpha = 0.3f))
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.586f)
                            .background(appearance.gradient)
                    ) {
                        if (card.imageUrl != null) {
                            AsyncImage(
                                model = card.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                        }
                        // NFC icon top-right
                        Icon(
                            Icons.Default.Nfc,
                            contentDescription = null,
                            tint = appearance.textColor.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(20.dp)
                        )
                        // Title bottom-left
                        Text(
                            text = card.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = appearance.textColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Ready to tap",
                style = MaterialTheme.typography.bodyMedium,
                color = appearance.neonColor
            )
        }
    }
}
