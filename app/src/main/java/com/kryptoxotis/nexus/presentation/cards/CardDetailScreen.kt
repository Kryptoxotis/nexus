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
import com.kryptoxotis.nexus.presentation.theme.Dimens
import com.kryptoxotis.nexus.presentation.theme.NexusBackground
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

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CardPreview(
                title = card.title,
                subtitle = "",
                cardShape = card.cardShape,
                storedColor = card.color,
                imageUri = card.imageUrl,
                tag = card.cardType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                glow = true,
                modifier = Modifier
                    .padding(horizontal = Dimens.screenPadding)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* consume */ }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Ready to tap",
                style = MaterialTheme.typography.bodyMedium,
                color = appearance.neonColor
            )
        }
    }
}
