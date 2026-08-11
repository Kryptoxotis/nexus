package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kryptoxotis.nexus.presentation.theme.Dimens

private val RingEasing = CubicBezierEasing(0.2f, 0.6f, 0.3f, 1f)

/**
 * Full-screen emulation state: the tapped card lifted onto a color-washed
 * backdrop with concentric NFC rings. Tap anywhere outside the card to stop.
 * The phone is emulating over HCE the whole time this is visible.
 */
@Composable
fun EmulateOverlay(
    title: String,
    subtitle: String,
    storedColor: String?,
    isNexus: Boolean,
    onDismiss: () -> Unit,
    onShowQr: () -> Unit,
    onQuickEdit: () -> Unit
) {
    val style = remember(storedColor) { resolveCardStyle(storedColor) }
    val bright = style.bright

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(bright.copy(alpha = 0.22f), Color.Black.copy(alpha = 0.88f)),
                        center = Offset(size.width * 0.5f, size.height * 0.42f),
                        radius = size.height * 0.68f
                    )
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            CardPreview(
                title = title,
                subtitle = subtitle,
                cardShape = "card",
                storedColor = storedColor,
                tag = "Sharing now",
                glow = true,
                onQrClick = onShowQr,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* consume — only outside taps dismiss */ }
            )

            EmulateRings(color = bright)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Hold near their phone",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE6E6E6)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap the QR icon if they don't have NFC · tap outside to close",
                    fontSize = 12.sp,
                    color = Color(0xFF7D7D7D),
                    textAlign = TextAlign.Center
                )
            }

            if (isNexus) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(15.dp))
                        .clickable(onClick = onQuickEdit)
                        .padding(horizontal = 20.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color(0xFFE0E0E0),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(9.dp))
                    Text(
                        text = "Quick edit what's shared",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }
}

/** Three staggered expanding rings around a 42dp NFC glyph (prototype nx-ring). */
@Composable
private fun EmulateRings(color: Color) {
    val transition = rememberInfiniteTransition(label = "emulateRings")
    val phases = listOf(0, 800, 1600).map { delay ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = RingEasing),
                initialStartOffset = StartOffset(delay)
            ),
            label = "ring$delay"
        )
    }

    Box(modifier = Modifier.size(104.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val baseRadius = size.minDimension / 2f
            phases.forEach { phase ->
                val p = phase.value
                val scale = 0.55f + 1.3f * p
                val alpha = (0.6f * (1f - p / 0.7f)).coerceIn(0f, 1f)
                if (alpha > 0f) {
                    drawCircle(
                        color = color.copy(alpha = alpha),
                        radius = baseRadius * scale,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
        Icon(
            Icons.Default.Nfc,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(42.dp)
        )
    }
}
