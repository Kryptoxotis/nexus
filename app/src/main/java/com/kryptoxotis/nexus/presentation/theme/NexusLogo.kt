package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val Teal = Color(0xFF037A68)
private val Orange = Color(0xFFFA5700)
private val Purple = Color(0xFF9163DD)
private val CardFill = Color(0xFF1A1A1A)

@Composable
fun NexusLogo(size: Dp = 80.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Card dimensions relative to canvas
        val cardW = w * 0.48f
        val cardH = h * 0.53f
        val cardR = CornerRadius(w * 0.07f, w * 0.07f)
        val strokeW = w * 0.018f
        val pivotX = w * 0.5f
        val pivotY = h * 0.67f

        // Back card - purple
        rotate(18f, Offset(pivotX, pivotY)) {
            val left = w * 0.23f
            val top = h * 0.07f
            drawRoundRect(CardFill, Offset(left, top), Size(cardW, cardH), cardR)
            drawRoundRect(Purple, Offset(left, top), Size(cardW, cardH), cardR, style = Stroke(strokeW))
        }

        // Middle card - orange
        rotate(8f, Offset(pivotX, pivotY)) {
            val left = w * 0.22f
            val top = h * 0.08f
            drawRoundRect(CardFill, Offset(left, top), Size(cardW, cardH), cardR)
            drawRoundRect(Orange, Offset(left, top), Size(cardW, cardH), cardR, style = Stroke(strokeW))
        }

        // Front card - teal
        rotate(-2f, Offset(pivotX, pivotY)) {
            val left = w * 0.21f
            val top = h * 0.1f
            drawRoundRect(CardFill, Offset(left, top), Size(cardW, cardH), cardR)
            drawRoundRect(Teal, Offset(left, top), Size(cardW, cardH), cardR, style = Stroke(strokeW))

            // Line details
            val lineX = left + cardW * 0.17f
            drawLine(Teal.copy(alpha = 0.25f), Offset(lineX, top + cardH * 0.25f), Offset(lineX + cardW * 0.5f, top + cardH * 0.25f), strokeW, StrokeCap.Round)
            drawLine(Teal.copy(alpha = 0.15f), Offset(lineX, top + cardH * 0.38f), Offset(lineX + cardW * 0.33f, top + cardH * 0.38f), strokeW * 0.8f, StrokeCap.Round)
            drawLine(Teal.copy(alpha = 0.10f), Offset(lineX, top + cardH * 0.51f), Offset(lineX + cardW * 0.4f, top + cardH * 0.51f), strokeW * 0.8f, StrokeCap.Round)
        }

        // NFC signal - orange dot + arcs
        val nfcY = h * 0.82f
        val nfcX = w * 0.5f
        drawCircle(Orange, w * 0.02f, Offset(nfcX, nfcY))

        val arcStroke = Stroke(strokeW, cap = StrokeCap.Round)
        drawArc(Orange, 200f, 140f, false, Offset(nfcX - w * 0.06f, nfcY - w * 0.06f), Size(w * 0.12f, w * 0.12f), style = arcStroke)
        drawArc(Orange.copy(alpha = 0.7f), 200f, 140f, false, Offset(nfcX - w * 0.1f, nfcY - w * 0.1f), Size(w * 0.2f, w * 0.2f), style = arcStroke)
        drawArc(Orange.copy(alpha = 0.4f), 200f, 140f, false, Offset(nfcX - w * 0.14f, nfcY - w * 0.14f), Size(w * 0.28f, w * 0.28f), style = arcStroke)
    }
}
