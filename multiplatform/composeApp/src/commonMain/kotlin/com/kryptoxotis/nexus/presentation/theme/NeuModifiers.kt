package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


/**
 * Neumorphic raised surface with proper 3D pop effect.
 * Dark shadow bottom-right + light highlight top-left + gradient overlay.
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
            // Dark shadow (bottom-right) — multiple layers for soft blur
            for (i in 6 downTo 1) {
                val offset = e * i / 4f
                val alpha = 0.22f / i
                val expand = e * 0.3f * i / 6f
                drawRoundRect(
                    color = darkColor.copy(alpha = alpha),
                    topLeft = Offset(offset - expand, offset - expand),
                    size = Size(size.width + expand * 2, size.height + expand * 2),
                    cornerRadius = CornerRadius(cr + expand)
                )
            }

            // Light highlight (top-left) — visible highlight for depth
            for (i in 5 downTo 1) {
                val offset = e * 0.5f * i / 4f
                val alpha = 0.12f / i
                val expand = e * 0.2f * i / 5f
                drawRoundRect(
                    color = lightColor.copy(alpha = alpha),
                    topLeft = Offset(-offset - expand, -offset - expand),
                    size = Size(size.width + expand * 2, size.height + expand * 2),
                    cornerRadius = CornerRadius(cr + expand)
                )
            }

            // Neon glow
            if (neonColor != null) {
                for (i in 5 downTo 1) {
                    val spread = e * 0.6f * i / 3f
                    drawRoundRect(
                        color = neonColor.copy(alpha = 0.10f / i),
                        topLeft = Offset(-spread, -spread),
                        size = Size(size.width + spread * 2, size.height + spread * 2),
                        cornerRadius = CornerRadius(cr + spread)
                    )
                }
            }

            // Surface fill
            drawRoundRect(surfaceColor, cornerRadius = CornerRadius(cr))

            // Gradient highlight overlay — light hitting top edge
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.06f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.04f)
                    ),
                    startY = 0f,
                    endY = size.height
                ),
                cornerRadius = CornerRadius(cr)
            )

            // Top-left edge highlight for 3D pop
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width * 0.4f, size.height * 0.4f)
                ),
                cornerRadius = CornerRadius(cr)
            )

            drawContent()
        }
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Inset / recessed well — inner shadow effect.
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

            // Dark inner shadow (top-left) — multiple layers
            for (i in 1..5) {
                val offset = 2.5f * density * i / 4f
                val alpha = 0.18f / i
                drawRoundRect(
                    color = Color(0xFF050505).copy(alpha = alpha),
                    topLeft = Offset(offset, offset),
                    size = Size(size.width - offset, size.height - offset),
                    cornerRadius = CornerRadius(cr)
                )
            }

            // Light inner highlight (bottom-right)
            for (i in 1..3) {
                val offset = 2f * density * i / 3f
                val alpha = 0.08f / i
                drawRoundRect(
                    color = Color(0xFF383838).copy(alpha = alpha),
                    topLeft = Offset(-offset, -offset),
                    size = Size(size.width + offset, size.height + offset),
                    cornerRadius = CornerRadius(cr)
                )
            }

            // Subtle inset gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.08f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                cornerRadius = CornerRadius(cr)
            )
        }
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Neon glow — colored shadow halo.
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
            for (i in 6 downTo 1) {
                val spread = e * i / 4f
                drawRoundRect(
                    color = color.copy(alpha = 0.14f / i),
                    topLeft = Offset(-spread, -spread),
                    size = Size(size.width + spread * 2, size.height + spread * 2),
                    cornerRadius = CornerRadius(cr + spread)
                )
            }
        }
    }

/**
 * Raised circle for icon buttons with neumorphic depth.
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
            // Dark shadow (bottom-right)
            for (i in 5 downTo 1) {
                val offset = e * i / 3f
                val alpha = 0.20f / i
                drawCircle(
                    color = Color(0xFF050505).copy(alpha = alpha),
                    radius = r + e * 0.15f * i,
                    center = center + Offset(offset, offset)
                )
            }
            // Light highlight (top-left)
            for (i in 4 downTo 1) {
                val offset = e * 0.4f * i / 3f
                val alpha = 0.10f / i
                drawCircle(
                    color = Color(0xFF383838).copy(alpha = alpha),
                    radius = r + e * 0.1f * i,
                    center = center + Offset(-offset, -offset)
                )
            }
            // Neon
            if (neonColor != null) {
                for (i in 3 downTo 1) {
                    drawCircle(
                        color = neonColor.copy(alpha = 0.08f / i),
                        radius = r + e * 0.5f * i,
                        center = center
                    )
                }
            }
            // Surface fill
            drawCircle(surfaceColor, r, center)

            // Gradient highlight for 3D pop
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center + Offset(-r * 0.3f, -r * 0.3f),
                    radius = r * 1.2f
                ),
                radius = r,
                center = center
            )

            drawContent()
        }
    }
    .clip(CircleShape)
