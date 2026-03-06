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
 * Raised neumorphic surface.
 *
 * Creates a soft 3D "pop" effect with dual shadows on a dark background.
 * Light source: top-left. Dark shadow: bottom-right, Light highlight: top-left.
 * Shadows are drawn BEHIND the surface fill so they don't affect layout.
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

        onDrawWithContent {
            // --- Dark shadow (bottom-right) ---
            // Soft diffuse: many layers, small alpha, offset down-right
            val darkLayers = 10
            for (i in darkLayers downTo 1) {
                val frac = i.toFloat() / darkLayers
                val ox = e * 0.5f * frac
                val oy = e * 0.6f * frac
                val blur = e * 1.2f * frac
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.03f),
                    topLeft = Offset(ox, oy),
                    size = Size(size.width + blur * 0.3f, size.height + blur * 0.3f),
                    cornerRadius = CornerRadius(cr)
                )
            }

            // --- Light highlight (top-left) ---
            // Subtle brightening above and left of the element
            val lightLayers = 8
            for (i in lightLayers downTo 1) {
                val frac = i.toFloat() / lightLayers
                val ox = e * 0.35f * frac
                val oy = e * 0.4f * frac
                drawRoundRect(
                    color = Color(0xFF2A2A2A).copy(alpha = 0.04f),
                    topLeft = Offset(-ox, -oy),
                    size = Size(size.width + ox * 0.5f, size.height + oy * 0.5f),
                    cornerRadius = CornerRadius(cr)
                )
            }

            // Neon glow (optional)
            if (neonColor != null) {
                for (i in 5 downTo 1) {
                    val spread = e * 1.2f * i / 5f
                    drawRoundRect(
                        color = neonColor.copy(alpha = 0.04f * i / 5f),
                        topLeft = Offset(-spread * 0.5f, -spread * 0.5f),
                        size = Size(size.width + spread, size.height + spread),
                        cornerRadius = CornerRadius(cr + spread * 0.3f)
                    )
                }
            }

            // Surface fill
            drawRoundRect(surfaceColor, cornerRadius = CornerRadius(cr))

            // Subtle rim light — top edge catches light
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.055f),
                        Color.White.copy(alpha = 0.01f),
                        Color.Transparent,
                        Color.Transparent
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                cornerRadius = CornerRadius(cr)
            )

            drawContent()
        }
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Inset / recessed well.
 */
fun Modifier.neuInset(
    cornerRadius: Dp = 20.dp,
): Modifier = this
    .graphicsLayer(clip = false)
    .drawWithCache {
        val cr = cornerRadius.toPx()
        val d = density

        onDrawBehind {
            // Recessed surface
            drawRoundRect(NexusDeep, cornerRadius = CornerRadius(cr))

            // Dark inner shadow (top-left inside)
            for (i in 1..5) {
                val t = i / 5f
                val offset = 2.5f * d * t
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.06f * (1f - t)),
                    topLeft = Offset(offset, offset),
                    size = Size(size.width - offset * 0.5f, size.height - offset * 0.5f),
                    cornerRadius = CornerRadius(cr)
                )
            }

            // Light inner highlight (bottom-right inside)
            for (i in 1..3) {
                val t = i / 3f
                val offset = 1.5f * d * t
                drawRoundRect(
                    color = Color(0xFF2A2A2A).copy(alpha = 0.03f * (1f - t)),
                    topLeft = Offset(-offset, -offset),
                    size = Size(size.width + offset * 0.3f, size.height + offset * 0.3f),
                    cornerRadius = CornerRadius(cr)
                )
            }

            // Inner gradient for depth
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.06f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.01f)
                    )
                ),
                cornerRadius = CornerRadius(cr)
            )
        }
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Neon glow — colored halo.
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
                val spread = e * 1.2f * i / 6f
                drawRoundRect(
                    color = color.copy(alpha = 0.06f * i / 6f),
                    topLeft = Offset(-spread * 0.5f, -spread * 0.5f),
                    size = Size(size.width + spread, size.height + spread),
                    cornerRadius = CornerRadius(cr + spread * 0.5f)
                )
            }
        }
    }

/**
 * Raised circle for icon buttons.
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
            for (i in 8 downTo 1) {
                val t = i / 8f
                val off = e * 0.5f * t
                drawCircle(
                    color = Color.Black.copy(alpha = 0.03f),
                    radius = r + e * 0.15f * t,
                    center = center + Offset(off, off)
                )
            }

            // Light highlight (top-left)
            for (i in 6 downTo 1) {
                val t = i / 6f
                val off = e * 0.35f * t
                drawCircle(
                    color = Color(0xFF2A2A2A).copy(alpha = 0.04f),
                    radius = r + e * 0.1f * t,
                    center = center + Offset(-off, -off)
                )
            }

            // Neon glow
            if (neonColor != null) {
                for (i in 4 downTo 1) {
                    drawCircle(
                        color = neonColor.copy(alpha = 0.035f * i / 4f),
                        radius = r + e * 0.8f * i / 4f,
                        center = center
                    )
                }
            }

            // Surface
            drawCircle(surfaceColor, r, center)

            // Rim highlight
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.045f),
                        Color.Transparent
                    ),
                    start = center + Offset(-r, -r),
                    end = center + Offset(r * 0.3f, r * 0.3f)
                ),
                radius = r,
                center = center
            )

            drawContent()
        }
    }
    .clip(CircleShape)
