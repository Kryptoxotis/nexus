package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kryptoxotis.nexus.presentation.theme.resolveCardAppearance

private val COIN_PREVIEW_SIZE = 120.dp
private const val CREDIT_CARD_ASPECT_RATIO = 1.586f
private val ImageScrimColor = Color.Black.copy(alpha = 0.6f)

@Composable
fun CardPreview(
    title: String,
    subtitle: String,
    cardShape: String,
    storedColor: String?,
    imageUri: Any? = null
) {
    val hasImage = imageUri != null
    val appearance = remember(storedColor, hasImage) {
        resolveCardAppearance(storedColor, hasImage = hasImage)
    }
    val displayTitle = title.ifBlank { "Card Title" }
    val displaySubtitle = subtitle.ifBlank { "Subtitle" }

    if (cardShape == "coin") {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(COIN_PREVIEW_SIZE)
                    .clip(CircleShape)
                    .background(appearance.gradient),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    ImageOverlay(imageUri)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = appearance.textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = displaySubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = appearance.textColor.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CREDIT_CARD_ASPECT_RATIO)
                .clip(RoundedCornerShape(16.dp))
                .background(appearance.gradient)
        ) {
            if (imageUri != null) {
                ImageOverlay(imageUri)
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = appearance.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = displaySubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = appearance.textColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ImageOverlay(imageUri: Any) {
    AsyncImage(
        model = imageUri,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ImageScrimColor)
    )
}
