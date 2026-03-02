package com.kryptoxotis.nexus.presentation.theme

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Shadow colors — neumorphic convention: dark shadow (bottom-right) + highlight (top-left)
private const val SHADOW_DARK = 0xFF050505.toInt()
private const val SHADOW_HIGHLIGHT = 0xFF383838.toInt() // visible highlight on dark gray surface
private const val NEON_ALPHA_RAISED = 55   // ~21% opacity for subtle halo
private const val NEON_ALPHA_CIRCLE = 40   // ~16% opacity for smaller circle targets
private val NEON_BLUR_RAISED = 24.dp
private const val NEON_BLUR_CIRCLE = 14f
private const val MAX_BITMAP_SIZE = 2048

/**
 * Raised neumorphic surface — bitmap-cached dual shadow.
 * Dark shadow offset bottom-right + light highlight offset top-left.
 *
 * drawWithCache runs once per size/density change, not per frame.
 * Bitmaps are NOT recycled — asImageBitmap() is a zero-copy wrapper;
 * the GPU reads asynchronously so recycle() would cause use-after-free.
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

        // Shadow offset and blur scale factors
        val darkOff = e
        val darkBlur = (e * 2f).coerceAtLeast(1f)
        val lightOff = e * 0.5f
        val lightBlur = (e * 1.5f).coerceAtLeast(1f)

        val pad = (maxOf(darkOff + darkBlur, lightOff + lightBlur) * 1.5f).coerceAtLeast(4f)
        val bw = (size.width + pad * 2).toInt().coerceIn(1, MAX_BITMAP_SIZE)
        val bh = (size.height + pad * 2).toInt().coerceIn(1, MAX_BITMAP_SIZE)

        val bmp = try {
            Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
        } catch (_: Throwable) {
            return@drawWithCache onDrawWithContent { drawContent() }
        }
        val c = android.graphics.Canvas(bmp)

        val darkPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = SHADOW_DARK
            maskFilter = BlurMaskFilter(darkBlur, BlurMaskFilter.Blur.NORMAL)
        }
        val lightPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = SHADOW_HIGHLIGHT
            maskFilter = BlurMaskFilter(lightBlur, BlurMaskFilter.Blur.NORMAL)
        }

        // Dark shadow (bottom-right)
        c.drawRoundRect(
            pad + darkOff, pad + darkOff,
            pad + size.width + darkOff, pad + size.height + darkOff,
            cr, cr, darkPaint
        )

        // Light highlight (top-left)
        c.drawRoundRect(
            pad - lightOff, pad - lightOff,
            pad + size.width - lightOff, pad + size.height - lightOff,
            cr, cr, lightPaint
        )

        // Neon glow layer
        if (neonColor != null) {
            val neonBlur = NEON_BLUR_RAISED.toPx()
            c.drawRoundRect(
                pad, pad, pad + size.width, pad + size.height,
                cr, cr,
                android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = neonColor.toArgb()
                    alpha = NEON_ALPHA_RAISED
                    maskFilter = BlurMaskFilter(neonBlur, BlurMaskFilter.Blur.NORMAL)
                }
            )
        }

        val img = bmp.asImageBitmap()

        onDrawWithContent {
            drawImage(img, topLeft = Offset(-pad, -pad))
            drawRoundRect(surfaceColor, cornerRadius = CornerRadius(cr))
            drawContent()
        }
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Inset / recessed well — bitmap-cached inner shadow.
 * Dark inner shadow from top-left + light highlight from bottom-right.
 *
 * Uses a "frame with hole" technique: draw a large filled rect, punch a
 * rounded-rect hole offset inward, then blur. The blurred edge bleeding
 * inside the hole creates the illusion of a shadow cast inward.
 */
fun Modifier.neuInset(
    cornerRadius: Dp = 20.dp,
): Modifier = this
    .graphicsLayer(clip = false)
    .drawWithCache {
        val cr = cornerRadius.toPx()
        val w = size.width.toInt().coerceIn(1, MAX_BITMAP_SIZE)
        val h = size.height.toInt().coerceIn(1, MAX_BITMAP_SIZE)

        val bmp = try {
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        } catch (_: Throwable) {
            return@drawWithCache onDrawBehind {
                drawRoundRect(NexusDeep, cornerRadius = CornerRadius(cr))
            }
        }
        val c = android.graphics.Canvas(bmp)

        // Deep background fill
        c.drawRoundRect(0f, 0f, size.width, size.height, cr, cr,
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = NexusDeep.toArgb()
            })

        // Clip to shape for inset shadow rendering
        c.save()
        c.clipPath(android.graphics.Path().apply {
            addRoundRect(0f, 0f, size.width, size.height, cr, cr,
                android.graphics.Path.Direction.CW)
        })

        // Use size-relative overflow so the frame is always larger than the shape
        val overflow = maxOf(size.width, size.height) * 2f

        // Dark inner shadow (top-left edge)
        val darkFrame = android.graphics.Path().apply {
            addRect(-overflow, -overflow, size.width + overflow, size.height + overflow,
                android.graphics.Path.Direction.CW)
        }
        val darkHole = android.graphics.Path().apply {
            addRoundRect(6 * density, 6 * density,
                size.width + 2 * density, size.height + 2 * density,
                cr, cr, android.graphics.Path.Direction.CW)
        }
        darkFrame.op(darkHole, android.graphics.Path.Op.DIFFERENCE)
        c.drawPath(darkFrame,
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = SHADOW_DARK
                maskFilter = BlurMaskFilter(12 * density, BlurMaskFilter.Blur.NORMAL)
            })

        // Light inner highlight (bottom-right edge)
        val lightFrame = android.graphics.Path().apply {
            addRect(-overflow, -overflow, size.width + overflow, size.height + overflow,
                android.graphics.Path.Direction.CW)
        }
        val lightHole = android.graphics.Path().apply {
            addRoundRect(-2 * density, -2 * density,
                size.width - 4 * density, size.height - 4 * density,
                cr, cr, android.graphics.Path.Direction.CW)
        }
        lightFrame.op(lightHole, android.graphics.Path.Op.DIFFERENCE)
        c.drawPath(lightFrame,
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = SHADOW_HIGHLIGHT
                maskFilter = BlurMaskFilter(8 * density, BlurMaskFilter.Blur.NORMAL)
            })

        c.restore()

        val img = bmp.asImageBitmap()
        onDrawBehind { drawImage(img) }
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Neon glow — colored shadow halo.
 * For FABs, accent buttons, active indicators.
 */
fun Modifier.neonGlow(
    color: Color,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 16.dp,
): Modifier = this
    .graphicsLayer(clip = false)
    .drawWithCache {
        val cr = cornerRadius.toPx()
        val blur = elevation.toPx().coerceAtLeast(1f)
        val pad = blur * 2f
        val bw = (size.width + pad * 2).toInt().coerceIn(1, MAX_BITMAP_SIZE)
        val bh = (size.height + pad * 2).toInt().coerceIn(1, MAX_BITMAP_SIZE)

        val bmp = try {
            Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
        } catch (_: Throwable) {
            return@drawWithCache onDrawWithContent { drawContent() }
        }
        android.graphics.Canvas(bmp).drawRoundRect(
            pad, pad, pad + size.width, pad + size.height,
            cr, cr,
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color.copy(alpha = 0.55f).toArgb()
                maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
            }
        )

        val img = bmp.asImageBitmap()
        onDrawBehind {
            drawImage(img, topLeft = Offset(-pad, -pad))
        }
    }

/**
 * Raised circle — for icon buttons with neumorphic depth.
 * Alpha ~16% (40/255) — slightly dimmer glow for smaller circle targets.
 */
fun Modifier.neuCircle(
    elevation: Dp = 6.dp,
    surfaceColor: Color = NexusSurface,
    neonColor: Color? = null,
): Modifier = this
    .graphicsLayer(clip = false)
    .drawWithCache {
        val e = elevation.toPx()
        val darkOff = e
        val darkBlur = (e * 2f).coerceAtLeast(1f)
        val lightOff = e * 0.5f
        val lightBlur = (e * 1.5f).coerceAtLeast(1f)
        val pad = (maxOf(darkOff + darkBlur, lightOff + lightBlur) * 1.5f).coerceAtLeast(4f)
        val bw = (size.width + pad * 2).toInt().coerceIn(1, MAX_BITMAP_SIZE)
        val bh = (size.height + pad * 2).toInt().coerceIn(1, MAX_BITMAP_SIZE)
        val cx = pad + size.width / 2
        val cy = pad + size.height / 2
        val r = minOf(size.width, size.height) / 2

        val bmp = try {
            Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
        } catch (_: Throwable) {
            return@drawWithCache onDrawWithContent { drawContent() }
        }
        val bc = android.graphics.Canvas(bmp)

        val darkPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = SHADOW_DARK
            maskFilter = BlurMaskFilter(darkBlur, BlurMaskFilter.Blur.NORMAL)
        }
        val lightPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = SHADOW_HIGHLIGHT
            maskFilter = BlurMaskFilter(lightBlur, BlurMaskFilter.Blur.NORMAL)
        }

        bc.drawCircle(cx + darkOff, cy + darkOff, r, darkPaint)
        bc.drawCircle(cx - lightOff, cy - lightOff, r, lightPaint)

        if (neonColor != null) {
            bc.drawCircle(cx, cy, r,
                android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = neonColor.toArgb()
                    alpha = NEON_ALPHA_CIRCLE
                    maskFilter = BlurMaskFilter(NEON_BLUR_CIRCLE * density, BlurMaskFilter.Blur.NORMAL)
                })
        }

        val img = bmp.asImageBitmap()
        onDrawWithContent {
            drawImage(img, topLeft = Offset(-pad, -pad))
            drawCircle(surfaceColor, r)
            drawContent()
        }
    }
    .clip(CircleShape)
