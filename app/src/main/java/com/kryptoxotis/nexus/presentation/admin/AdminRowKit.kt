package com.kryptoxotis.nexus.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kryptoxotis.nexus.presentation.theme.NexusTextPrimary
import com.kryptoxotis.nexus.presentation.theme.NexusTextSecondary
import com.kryptoxotis.nexus.presentation.theme.neuInset
import com.kryptoxotis.nexus.presentation.theme.neuRaised

private val RowSurface = Color(0xFF141414)
private val PillSurface = Color(0xFF1F1F1F)
private val PillText = Color(0xFFB0B0B0)

/**
 * Small filled pill for role/status. Body pills stay neutral; pass [color]
 * only for the status pill (teal = active, tertiary gray = inactive).
 */
@Composable
internal fun AdminPill(text: String, color: Color? = null) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(PillSurface)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color ?: PillText,
            maxLines = 1
        )
    }
}

/**
 * The one admin list row: neuRaised #141414 at 14dp radius, 40dp avatar,
 * stacked title/subtitle that ellipsize instead of pushing the trailing
 * control out of alignment, pills under the text.
 */
@Composable
internal fun AdminListRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    pills: List<Pair<String, Color?>>,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .neuRaised(cornerRadius = 14.dp, surfaceColor = RowSurface)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .neuInset(cornerRadius = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = NexusTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NexusTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = NexusTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (pills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pills.forEach { (text, color) -> AdminPill(text, color) }
                }
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}
