package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kryptoxotis.nexus.presentation.theme.Dimens
import com.kryptoxotis.nexus.presentation.theme.NexusCardColors
import com.kryptoxotis.nexus.presentation.theme.NexusPrimaryGradient
import com.kryptoxotis.nexus.presentation.theme.NexusSurface
import com.kryptoxotis.nexus.presentation.theme.NexusTextPrimary
import com.kryptoxotis.nexus.presentation.theme.NexusTextSecondary
import com.kryptoxotis.nexus.presentation.theme.NexusTextTertiary

/** One toggleable link on the My Nexus card. */
data class QuickEditField(
    val key: String,
    val label: String,
    val value: String,
    val brandColor: Color,
    val materialIcon: ImageVector? = null,
    val drawableRes: Int = 0,
    val gradientIcon: Boolean = false
)

private val TileBackground: Brush
    get() = if (com.kryptoxotis.nexus.presentation.theme.NexusAppearance.dark) {
        Brush.linearGradient(listOf(Color(0xFF191919), Color(0xFF111111)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF1EFE9)))
    }
private val OffBorder: Color
    get() = if (com.kryptoxotis.nexus.presentation.theme.NexusAppearance.dark) Color(0xFF242424) else Color(0xFFDDD9D2)
private val OffTint: Color
    get() = if (com.kryptoxotis.nexus.presentation.theme.NexusAppearance.dark) Color(0xFF6A6A6A) else Color(0xFFA39E96)

/**
 * Eye-toggle tiles for what the My Nexus card transmits. Sharing is paused
 * while this sheet is open; "Share again" re-emulates with the updated set.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEditSheet(
    fields: List<QuickEditField>,
    excluded: Set<String>,
    onToggle: (String) -> Unit,
    onShareAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = NexusSurface,
        tonalElevation = 0.dp,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF333333)) }
    ) {
        val sharedCount = fields.count { it.key !in excluded }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenPadding)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "What you're sharing",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NexusTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$sharedCount of ${fields.size} shared",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = NexusTextTertiary
                )
            }
            Text(
                text = "Sharing is paused while you edit. Hidden links are never sent.",
                fontSize = 12.sp,
                color = NexusTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))

            fields.chunked(2).forEach { rowFields ->
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gapSmall)) {
                    rowFields.forEach { field ->
                        QuickEditTile(
                            field = field,
                            enabled = field.key !in excluded,
                            onClick = { onToggle(field.key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowFields.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.cardRadius))
                    .background(NexusPrimaryGradient)
                    .clickable(onClick = onShareAgain)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Share again",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun QuickEditTile(
    field: QuickEditField,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (enabled) field.brandColor.copy(alpha = 0.55f) else OffBorder
    val iconTint = when {
        !enabled -> OffTint
        field.gradientIcon -> Color.Unspecified
        else -> field.brandColor
    }
    Column(
        modifier = modifier
            .height(104.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TileBackground)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (field.drawableRes != 0) {
                Icon(
                    painter = painterResource(field.drawableRes),
                    contentDescription = field.label,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            } else if (field.materialIcon != null) {
                Icon(
                    imageVector = field.materialIcon,
                    contentDescription = field.label,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                if (enabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (enabled) "Shared" else "Hidden",
                tint = if (enabled) field.brandColor else Color(0xFF5A5A5A),
                modifier = Modifier.size(17.dp)
            )
        }
        Column {
            Text(
                text = field.label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color(0xFFD6D6D6) else Color(0xFF6F6F6F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = field.value.ifBlank { "Not set" },
                fontSize = 11.sp,
                color = if (enabled) Color(0xFF9A9A9A) else Color(0xFF5F5F5F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
