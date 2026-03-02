package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kryptoxotis.nexus.presentation.theme.resolveCardAppearance

private val COIN_PREVIEW_SIZE = 120.dp
private const val CREDIT_CARD_ASPECT_RATIO = 1.586f // ISO/IEC 7810 ID-1 (85.6mm / 54mm)
private val ImageScrimColor = Color.Black.copy(alpha = 0.6f)
private val ImagePlaceholder = ColorPainter(Color.DarkGray)
private val ImageErrorPainter = ColorPainter(Color(0xFF3A1A1A))

/**
 * Live preview of a card's visual appearance.
 * [cardShape] is "card" or "coin". [storedColor] is a color string in the format
 * "#RRGGBB" or "#RRGGBB:dark" (see [com.kryptoxotis.nexus.presentation.theme.NexusCardColors.parse]).
 */
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
    val titleAnnouncement = if (title.isBlank()) "no title set" else title
    val subtitleAnnouncement = if (subtitle.isBlank()) "no subtitle set" else subtitle
    val shapeLabel = if (cardShape == "coin") "coin shaped" else "card shaped"
    val contentDesc = remember(titleAnnouncement, subtitleAnnouncement, shapeLabel) {
        "Card preview: $titleAnnouncement, $subtitleAnnouncement, $shapeLabel"
    }

    if (cardShape == "coin") {
        // Coin preview
        Box(
            modifier = Modifier.fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = contentDesc
                },
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
        // Card preview (credit card aspect ratio)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CREDIT_CARD_ASPECT_RATIO)
                .clip(RoundedCornerShape(16.dp))
                .background(appearance.gradient)
                .semantics(mergeDescendants = true) {
                    contentDescription = contentDesc
                }
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
        contentScale = ContentScale.Crop,
        placeholder = ImagePlaceholder,
        error = ImageErrorPainter
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ImageScrimColor)
            .semantics { invisibleToUser() }
    )
}
