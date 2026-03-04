package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kryptoxotis.nexus.presentation.theme.*

@Composable
fun SharedLinkScreen(
    url: String,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    // Pulsing animation for the NFC ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Pulsing NFC indicator
        Box(contentAlignment = Alignment.Center) {
            // Outer pulse ring
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale)
                    .border(2.dp, NexusTeal.copy(alpha = pulseAlpha), CircleShape)
            )
            // Inner circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(NexusTeal.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "NFC Active",
                    modifier = Modifier.size(48.dp),
                    tint = NexusTeal
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "NFC ACTIVE",
            color = NexusTeal,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap another phone to share",
            color = NexusTextSecondary,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // URL card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NexusSurface)
                .border(1.dp, NexusTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "LINK",
                    color = NexusTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = url,
                    color = NexusTextPrimary,
                    fontSize = 15.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Actions at the bottom
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NexusTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save to Wallet", modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onDiscard,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusTextSecondary)
        ) {
            Text("Discard", modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
