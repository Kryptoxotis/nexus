package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kryptoxotis.nexus.presentation.theme.Dimens
import com.kryptoxotis.nexus.presentation.theme.NexusCardColors
import com.kryptoxotis.nexus.presentation.theme.neonGlow

/** ISO/IEC 7810 ID-1 (85.6mm x 54mm) — every rectangular card in the app uses this ratio. */
const val CARD_ASPECT_RATIO = 1.586f

/** Fixed coin diameter from the prototype. */
val COIN_SIZE = 140.dp

private val CardBorderWidth = 2.5.dp
private val DarkCardBackground = Brush.linearGradient(listOf(Color(0xFF1A1A1A), Color(0xFF111111)))
private val ImageScrimColor = Color.Black.copy(alpha = 0.6f)
private val ImagePlaceholder = ColorPainter(Color.DarkGray)
private val ImageErrorPainter = ColorPainter(Color(0xFF3A1A1A))

/**
 * Resolved styling for a card, derived from its stored color string.
 * Exposed to [NexusCardShell] overlay content so custom layouts stay on-palette.
 */
data class NexusCardStyle(
    val bright: Color,
    val dark: Color,
    val gradient: Brush,
    val isDark: Boolean
) {
    /** Tag/category text color (top-left). */
    val tagColor: Color get() = if (isDark) bright.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.65f)
    /** NFC glyph tint (top-right). */
    val nfcColor: Color get() = if (isDark) bright.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f)
    /** Subtitle color. */
    val subtitleColor: Color get() = if (isDark) bright.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.75f)
    /** QR glyph tint (bottom-right). */
    val qrColor: Color get() = if (isDark) bright.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
    /** Solid title color for light mode; dark mode titles use [titleBrush]. */
    val titleColor: Color get() = if (isDark) bright else Color.White
    /** Gradient brush for dark-mode titles (bright -> dark), null in light mode. */
    val titleBrush: Brush? get() = if (isDark) gradient else null
}

/** Resolve a stored color string ("#RRGGBB" or "#RRGGBB:dark") into a [NexusCardStyle]. */
fun resolveCardStyle(storedColor: String?): NexusCardStyle {
    val (hex, isDark) = NexusCardColors.parse(storedColor)
    val entry = NexusCardColors.findByHex(hex) ?: NexusCardColors.palette[0]
    return NexusCardStyle(
        bright = entry.bright,
        dark = entry.dark,
        gradient = entry.gradient,
        isDark = isDark
    )
}

/**
 * The ONLY composable that draws a card, coin or pass.
 *
 * Layout: tag top-left, NFC glyph top-right, title + subtitle bottom-left, QR bottom-right.
 * Light mode: bright->dark gradient fill, white text.
 * Dark mode: #1A1A1A -> #111111 fill with a gradient 2.5dp border and gradient title.
 */
@Composable
fun CardPreview(
    title: String,
    subtitle: String,
    cardShape: String,
    storedColor: String?,
    imageUri: Any? = null,
    modifier: Modifier = Modifier,
    tag: String? = null,
    glow: Boolean = false,
    placeholders: Boolean = false,
    onQrClick: (() -> Unit)? = null
) {
    val style = remember(storedColor) { resolveCardStyle(storedColor) }
    val displayTitle = if (placeholders) title.ifBlank { "Card Title" } else title
    val displaySubtitle = if (placeholders) subtitle.ifBlank { "Subtitle" } else subtitle
    val shapeLabel = if (cardShape == "coin") "coin shaped" else "card shaped"
    val titleAnnouncement = displayTitle.ifBlank { "no title set" }
    val subtitleAnnouncement = displaySubtitle.ifBlank { "no subtitle set" }
    val contentDesc = remember(titleAnnouncement, subtitleAnnouncement, shapeLabel) {
        "Card preview: $titleAnnouncement, $subtitleAnnouncement, $shapeLabel"
    }

    if (cardShape == "coin") {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { contentDescription = contentDesc },
            contentAlignment = Alignment.Center
        ) {
            NexusCardShell(
                style = style,
                shape = CircleShape,
                glow = glow,
                modifier = Modifier.size(COIN_SIZE)
            ) {
                if (imageUri != null) ImageOverlay(imageUri)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(12.dp)
                ) {
                    CardTitleText(displayTitle, style, fontSize = 14, textAlign = TextAlign.Center)
                    if (displaySubtitle.isNotBlank()) {
                        Text(
                            text = displaySubtitle,
                            fontSize = 11.sp,
                            color = style.subtitleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                if (onQrClick != null) {
                    Icon(
                        Icons.Default.QrCode2,
                        contentDescription = "QR Code",
                        tint = style.qrColor,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                            .size(18.dp)
                            .clickable(onClick = onQrClick)
                    )
                }
            }
        }
    } else {
        NexusCardShell(
            style = style,
            shape = RoundedCornerShape(Dimens.cardRadius),
            glow = glow,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(CARD_ASPECT_RATIO)
                .semantics(mergeDescendants = true) { contentDescription = contentDesc }
        ) {
            if (imageUri != null) ImageOverlay(imageUri)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.screenPadding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tag != null) {
                        Text(
                            text = tag.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = style.tagColor
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.Nfc,
                        contentDescription = null,
                        tint = style.nfcColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(modifier = Modifier.weight(1f)) {
                        CardTitleText(displayTitle, style, fontSize = 20)
                        if (displaySubtitle.isNotBlank()) {
                            Text(
                                text = displaySubtitle,
                                fontSize = 12.sp,
                                color = style.subtitleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    if (onQrClick != null) {
                        Icon(
                            Icons.Default.QrCode2,
                            contentDescription = "QR Code",
                            tint = style.qrColor,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(onClick = onQrClick)
                        )
                    } else {
                        Icon(
                            Icons.Default.QrCode2,
                            contentDescription = null,
                            tint = style.qrColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shared card surface: fill, dark-mode gradient border, glow and clipping.
 * Screens with specialized card content (brand sub-cards, contact cards) build on this
 * so every card in the app shares one shape language.
 */
@Composable
fun NexusCardShell(
    style: NexusCardStyle,
    shape: Shape,
    modifier: Modifier = Modifier,
    glow: Boolean = false,
    borderBrush: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cornerRadius = if (shape == CircleShape) COIN_SIZE / 2 else Dimens.cardRadius
    val resolvedBorder = borderBrush ?: if (style.isDark) style.gradient else null
    Box(
        modifier = modifier
            .then(if (glow) Modifier.neonGlow(style.bright, cornerRadius = cornerRadius) else Modifier)
            .clip(shape)
            .background(if (style.isDark) DarkCardBackground else style.gradient)
            .then(
                if (resolvedBorder != null) Modifier.border(CardBorderWidth, resolvedBorder, shape)
                else Modifier
            ),
        content = content
    )
}

/** Title text — gradient brush in dark mode, solid white in light mode. */
@Composable
private fun CardTitleText(
    text: String,
    style: NexusCardStyle,
    fontSize: Int,
    textAlign: TextAlign? = null
) {
    val brush = style.titleBrush
    Text(
        text = text,
        style = if (brush != null) {
            TextStyle(brush = brush, fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
        } else {
            TextStyle(color = style.titleColor, fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign
    )
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
