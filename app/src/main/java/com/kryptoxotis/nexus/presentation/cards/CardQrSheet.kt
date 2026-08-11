package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import com.kryptoxotis.nexus.domain.model.PersonalCard
import com.kryptoxotis.nexus.presentation.theme.NexusCardColors
import com.kryptoxotis.nexus.presentation.theme.resolveCardAppearance
import com.kryptoxotis.nexus.util.QrCodeGenerator
import com.kryptoxotis.nexus.util.QrContentResolver
import androidx.compose.ui.unit.dp

/**
 * QR fallback rendered in the card's own colours — dark cards get a black
 * background with the card colour as foreground, light cards the inverse.
 * This is the iPhone-to-Android path, so it's reachable from the wallet AND
 * from inside the emulation overlay.
 */
@Composable
internal fun CardQrSheet(card: PersonalCard) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = card.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        val qrContent = QrContentResolver.resolve(card)
        val (_, isDarkCard) = NexusCardColors.parse(card.color)
        val appearance = resolveCardAppearance(card.color)
        val cardColor = appearance.neonColor.copy(alpha = 1f)
        val qrFg = if (isDarkCard) cardColor.toArgb() else 0xFF000000.toInt()
        val qrBg = if (isDarkCard) 0xFF0A0A0A.toInt() else cardColor.toArgb()
        val boxBg = if (isDarkCard) Color(0xFF0A0A0A) else cardColor
        var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
        LaunchedEffect(qrContent, qrFg, qrBg) {
            val old = qrBitmap
            qrBitmap = try {
                QrCodeGenerator.generate(qrContent, 400, foregroundColor = qrFg, backgroundColor = qrBg)
            } catch (e: Exception) {
                null
            }
            old?.recycle()
        }
        DisposableEffect(Unit) {
            onDispose { qrBitmap?.recycle() }
        }

        val bitmap = qrBitmap
        if (bitmap != null) {
            Card(
                modifier = Modifier.size(240.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(boxBg)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scan to open · works on iPhone too",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
