package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CMP-compatible neumorphic raised surface.
 * Uses multiple offset drawRoundRect calls at varying alphas to approximate
 * the Bitmap + BlurMaskFilter approach from the Android-only version.
 */
fun Modifier.neuRaised(
    cornerRadius: Dp = 22.dp,
    elevation: Dp = 10.dp,
    surfaceColor: Color = NexusSurface,
    neonColor: Color? = null,
): Modifier = this
    .graphicsLayer(clip = false)
    .drawWithCache {
        val cr = cornerRadius.toPx()
        val e = elevation.toPx()
        val darkColor = Color(0xFF050505)
        val lightColor = Color(0xFF383838)

        onDrawWithContent {
            // Simulate dark shadow (bottom-right) with multiple layers
            for (i in 3 downTo 1) {
                val offset = e * i / 3f
                val alpha = 0.15f / i
                drawRoundRect(
                    color = darkColor.copy(alpha = alpha),
                    topLeft = Offset(offset, offset),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cr)
                )
            }

            // Simulate light highlight (top-left)
            for (i in 2 downTo 1) {
                val offset = e * 0.3f * i / 2f
                val alpha = 0.08f / i
                drawRoundRect(
                    color = lightColor.copy(alpha = alpha),
                    topLeft = Offset(-offset, -offset),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cr)
                )
            }

            // Neon glow
            if (neonColor != null) {
                for (i in 3 downTo 1) {
                    val spread = e * 0.5f * i / 3f
                    drawRoundRect(
                        color = neonColor.copy(alpha = 0.07f / i),
                        topLeft = Offset(-spread, -spread),
                        size = Size(size.width + spread * 2, size.height + spread * 2),
                        cornerRadius = CornerRadius(cr + spread)
                    )
                }
            }

            // Surface fill
            drawRoundRect(surfaceColor, cornerRadius = CornerRadius(cr))
            drawContent()
        }
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * CMP-compatible inset/recessed well effect.
 */
fun Modifier.neuInset(
    cornerRadius: Dp = 20.dp,
): Modifier = this
    .graphicsLayer(clip = false)
    .drawWithCache {
        val cr = cornerRadius.toPx()
        onDrawBehind {
            // Deep background
            drawRoundRect(NexusDeep, cornerRadius = CornerRadius(cr))

            // Dark inner shadow (top-left)
            for (i in 1..3) {
                val offset = 2f * density * i / 3f
                drawRoundRect(
                    color = Color(0xFF050505).copy(alpha = 0.12f / i),
                    topLeft = Offset(offset, offset),
                    size = Size(size.width - offset, size.height - offset),
                    cornerRadius = CornerRadius(cr)
                )
            }

            // Light inner highlight (bottom-right)
            for (i in 1..2) {
                val offset = 1.5f * density * i / 2f
                drawRoundRect(
                    color = Color(0xFF383838).copy(alpha = 0.06f / i),
                    topLeft = Offset(-offset, -offset),
                    size = Size(size.width + offset, size.height + offset),
                    cornerRadius = CornerRadius(cr)
                )
            }
        }
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * CMP-compatible neon glow.
 */
fun Modifier.neonGlow(
    color: Color,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 16.dp,
): Modifier = this
    .graphicsLayer(clip = false)
    .drawWithCache {
        val cr = cornerRadius.toPx()
        val e = elevation.toPx()
        onDrawBehind {
            for (i in 4 downTo 1) {
                val spread = e * i / 4f
                drawRoundRect(
                    color = color.copy(alpha = 0.12f / i),
                    topLeft = Offset(-spread, -spread),
                    size = Size(size.width + spread * 2, size.height + spread * 2),
                    cornerRadius = CornerRadius(cr + spread)
                )
            }
        }
    }

/**
 * CMP-compatible raised circle for icon buttons.
 */
fun Modifier.neuCircle(
    elevation: Dp = 6.dp,
    surfaceColor: Color = NexusSurface,
    neonColor: Color? = null,
): Modifier = this
    .graphicsLayer(clip = false)
    .drawWithCache {
        val e = elevation.toPx()
        val r = minOf(size.width, size.height) / 2
        val center = Offset(size.width / 2, size.height / 2)

        onDrawWithContent {
            // Dark shadow
            for (i in 3 downTo 1) {
                val offset = e * i / 3f
                drawCircle(
                    color = Color(0xFF050505).copy(alpha = 0.15f / i),
                    radius = r,
                    center = center + Offset(offset, offset)
                )
            }
            // Light highlight
            for (i in 2 downTo 1) {
                val offset = e * 0.3f * i / 2f
                drawCircle(
                    color = Color(0xFF383838).copy(alpha = 0.08f / i),
                    radius = r,
                    center = center + Offset(-offset, -offset)
                )
            }
            // Neon
            if (neonColor != null) {
                for (i in 2 downTo 1) {
                    drawCircle(
                        color = neonColor.copy(alpha = 0.06f / i),
                        radius = r + e * 0.5f * i,
                        center = center
                    )
                }
            }
            drawCircle(surfaceColor, r, center)
            drawContent()
        }
    }
    .clip(CircleShape)
