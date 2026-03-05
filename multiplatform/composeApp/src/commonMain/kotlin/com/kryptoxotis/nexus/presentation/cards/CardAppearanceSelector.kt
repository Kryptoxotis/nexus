package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.presentation.theme.NexusCardColor
import com.kryptoxotis.nexus.presentation.theme.NexusCardColors
import com.kryptoxotis.nexus.presentation.theme.isLightColor
import com.kryptoxotis.nexus.presentation.theme.neuInset
import com.kryptoxotis.nexus.presentation.theme.neuRaised
import com.kryptoxotis.nexus.presentation.theme.neonGlow

private val SWATCH_SIZE = 48.dp
private val SWATCH_CORNER_RADIUS = 10.dp

/**
 * Selector UI for card shape ("card" or "coin"), light/dark mode, and palette color.
 * [selectedColorHex] uses brightHex values from [NexusCardColors.palette].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardAppearanceSelector(
    cardShape: String,
    onCardShapeChange: (String) -> Unit,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    selectedColorHex: String,
    onColorSelected: (String) -> Unit
) {
    // Card shape selector
    Text(
        text = "Card Shape",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { heading() }
    )
    Row(
        modifier = Modifier.fillMaxWidth().semantics { selectableGroup() },
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ToggleOption(
            label = "Card",
            isSelected = cardShape == "card",
            onClick = { onCardShapeChange("card") },
            modifier = Modifier.weight(1f)
        )
        ToggleOption(
            label = "Coin",
            isSelected = cardShape == "coin",
            onClick = { onCardShapeChange("coin") },
            modifier = Modifier.weight(1f)
        )
    }

    // Light / Dark mode toggle
    Text(
        text = "Card Mode",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { heading() }
    )
    Row(
        modifier = Modifier.fillMaxWidth().semantics { selectableGroup() },
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ToggleOption(
            label = "Light",
            isSelected = !isDarkMode,
            onClick = { onDarkModeChange(false) },
            modifier = Modifier.weight(1f)
        )
        ToggleOption(
            label = "Dark",
            isSelected = isDarkMode,
            onClick = { onDarkModeChange(true) },
            modifier = Modifier.weight(1f)
        )
    }

    // Color swatches
    Text(
        text = "Card Color",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { heading() }
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth().semantics { selectableGroup() },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NexusCardColors.palette.forEach { entry ->
            key(entry.brightHex) {
                ColorSwatch(
                    entry = entry,
                    isSelected = selectedColorHex == entry.brightHex,
                    onSelected = { onColorSelected(entry.brightHex) }
                )
            }
        }
    }
    // Selected color name with liveRegion for screen reader announcements
    val selectedEntry = remember(selectedColorHex) { NexusCardColors.findByHex(selectedColorHex) }
    if (selectedEntry != null) {
        Text(
            text = "Selected: ${selectedEntry.name}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
            }
        )
    }
}

@Composable
private fun ColorSwatch(
    entry: NexusCardColor,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val isLight = remember(entry.bright) { isLightColor(entry.bright) }
    val indicatorColor = if (isLight) Color.Black else Color.White

    Box(
        modifier = Modifier
            .size(SWATCH_SIZE)
            .then(
                if (isSelected) Modifier.neonGlow(entry.bright, cornerRadius = SWATCH_CORNER_RADIUS, elevation = 8.dp)
                else Modifier
            )
            .clip(RoundedCornerShape(SWATCH_CORNER_RADIUS))
            .background(entry.gradient)
            .then(
                if (isSelected) Modifier.border(2.dp, indicatorColor, RoundedCornerShape(SWATCH_CORNER_RADIUS))
                else Modifier
            )
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onSelected
            )
            .semantics {
                contentDescription = entry.name
                stateDescription = if (isSelected) "Selected" else "Not selected"
            },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = indicatorColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ToggleOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .then(if (isSelected) Modifier.neuInset(cornerRadius = 12.dp) else Modifier.neuRaised(cornerRadius = 12.dp))
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
