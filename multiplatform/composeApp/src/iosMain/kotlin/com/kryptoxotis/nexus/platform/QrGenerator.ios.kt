package com.kryptoxotis.nexus.platform

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint

actual object QrGenerator {
    actual suspend fun generate(
        content: String,
        size: Int,
        foregroundColor: Int,
        backgroundColor: Int
    ): ImageBitmap {
        val fgColor = Color(foregroundColor)
        val bgColor = Color(backgroundColor)

        val bitmap = ImageBitmap(size, size)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // Background
        paint.color = bgColor
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

        // Draw a simple pattern to indicate QR placeholder
        paint.color = fgColor
        val moduleSize = size / 21f

        // Draw finder patterns (top-left, top-right, bottom-left corners)
        drawFinderPattern(canvas, paint, 0f, 0f, moduleSize)
        drawFinderPattern(canvas, paint, (size - 7 * moduleSize), 0f, moduleSize)
        drawFinderPattern(canvas, paint, 0f, (size - 7 * moduleSize), moduleSize)

        return bitmap
    }

    private fun drawFinderPattern(canvas: Canvas, paint: Paint, x: Float, y: Float, moduleSize: Float) {
        // Outer border
        canvas.drawRect(x, y, x + 7 * moduleSize, y + moduleSize, paint)
        canvas.drawRect(x, y + 6 * moduleSize, x + 7 * moduleSize, y + 7 * moduleSize, paint)
        canvas.drawRect(x, y, x + moduleSize, y + 7 * moduleSize, paint)
        canvas.drawRect(x + 6 * moduleSize, y, x + 7 * moduleSize, y + 7 * moduleSize, paint)
        // Inner square
        canvas.drawRect(x + 2 * moduleSize, y + 2 * moduleSize, x + 5 * moduleSize, y + 5 * moduleSize, paint)
    }
}
