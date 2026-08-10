package com.kryptoxotis.nexus.presentation.cards

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.R
import com.kryptoxotis.nexus.presentation.theme.Dimens
import com.kryptoxotis.nexus.presentation.theme.NexusCardColors
import com.kryptoxotis.nexus.presentation.theme.neuRaised

private fun safeLaunchUrl(context: Context, rawUrl: String) {
    val blockedSchemes = listOf("javascript:", "data:", "file:", "content:", "intent:", "blob:", "vbscript:")
    val trimmed = rawUrl.trim()
    if (blockedSchemes.any { trimmed.lowercase().startsWith(it) }) return
    val url = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")
        && !trimmed.startsWith("tel:") && !trimmed.startsWith("mailto:")) "https://$trimmed" else trimmed
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addCategory(Intent.CATEGORY_BROWSABLE) })
    } catch (_: Exception) { }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contactId: String,
    viewModel: ReceivedCardViewModel,
    onNavigateBack: () -> Unit
) {
    val contact by viewModel.getContact(contactId).collectAsState(initial = null)
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nexus") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        val c = contact
        if (c == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Contact not found")
            }
            return@Scaffold
        }

        // Build link entries
        data class NexusField(
            val label: String, val value: String, val intentUri: String,
            val brandColor: Color,
            val materialIcon: ImageVector? = null,
            val drawableRes: Int = 0, val gradientIcon: Boolean = false
        )
        val fields = buildList {
            if (c.phone.isNotBlank()) add(NexusField("Phone", c.phone, "tel:${c.phone}", Color(0xFF037A68), materialIcon = Icons.Default.Phone))
            if (c.email.isNotBlank()) add(NexusField("Email", c.email, "mailto:${c.email}", Color(0xFFFA5700), materialIcon = Icons.Default.Email))
            if (c.website.isNotBlank()) add(NexusField("Website", c.website, c.website, Color(0xFF037A68), materialIcon = Icons.Default.Language))
            if (c.instagram.isNotBlank()) add(NexusField("Instagram", c.instagram, c.instagram, Color(0xFFD62976), drawableRes = R.drawable.ic_social_instagram, gradientIcon = true))
            if (c.twitter.isNotBlank()) add(NexusField("X", c.twitter, c.twitter, Color(0xFFEFEFEF), drawableRes = R.drawable.ic_social_x))
            if (c.github.isNotBlank()) add(NexusField("GitHub", c.github, c.github, Color(0xFFEFEFEF), drawableRes = R.drawable.ic_social_github))
            if (c.linkedin.isNotBlank()) add(NexusField("LinkedIn", c.linkedin, c.linkedin, Color(0xFF0A66C2), drawableRes = R.drawable.ic_social_linkedin))
            if (c.facebook.isNotBlank()) add(NexusField("Facebook", c.facebook, c.facebook, Color(0xFF1877F2), drawableRes = R.drawable.ic_social_facebook))
            if (c.youtube.isNotBlank()) add(NexusField("YouTube", c.youtube, c.youtube, Color(0xFFFF0000), drawableRes = R.drawable.ic_social_youtube, gradientIcon = true))
            if (c.tiktok.isNotBlank()) add(NexusField("TikTok", c.tiktok, c.tiktok, Color(0xFFEE1D52), drawableRes = R.drawable.ic_social_tiktok))
            if (c.discord.isNotBlank()) add(NexusField("Discord", c.discord, c.discord, Color(0xFF5865F2), drawableRes = R.drawable.ic_social_discord, gradientIcon = true))
            if (c.twitch.isNotBlank()) add(NexusField("Twitch", c.twitch, c.twitch, Color(0xFF9146FF), drawableRes = R.drawable.ic_social_twitch, gradientIcon = true))
            if (c.whatsapp.isNotBlank()) add(NexusField("WhatsApp", c.whatsapp, "https://wa.me/${c.whatsapp.replace(Regex("[^0-9+]"), "")}", Color(0xFF25D366), drawableRes = R.drawable.ic_social_whatsapp))
        }

        val style = resolveCardStyle(null)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Main card with name
            item {
                NexusCardShell(
                    style = style,
                    shape = RoundedCornerShape(Dimens.cardRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.586f)
                        .neuRaised(cornerRadius = Dimens.cardRadius, elevation = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val titleBrush = style.titleBrush
                        Text(
                            text = c.name.ifBlank { "Unknown" },
                            style = if (titleBrush != null) {
                                MaterialTheme.typography.titleLarge.copy(brush = titleBrush, fontWeight = FontWeight.Bold)
                            } else {
                                MaterialTheme.typography.titleLarge.copy(color = style.titleColor, fontWeight = FontWeight.Bold)
                            }
                        )
                        val subtitle = listOfNotNull(
                            c.jobTitle.ifBlank { null },
                            c.company.ifBlank { null }
                        ).joinToString(" at ")
                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = style.subtitleColor
                            )
                        }
                    }
                }
            }

            // Sub-cards for each field
            items(fields) { field ->
                NexusCardShell(
                    style = resolveCardStyle("${NexusCardColors.palette[0].brightHex}:dark"),
                    shape = RoundedCornerShape(Dimens.cardRadius),
                    borderBrush = Brush.linearGradient(
                        listOf(field.brandColor.copy(alpha = 0.3f), field.brandColor.copy(alpha = 0.1f))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.586f)
                        .neuRaised(cornerRadius = Dimens.cardRadius, elevation = 10.dp)
                        .clickable { safeLaunchUrl(context, field.intentUri) }
                ) {
                    // Brand icon centered
                    val tint = if (field.gradientIcon) Color.Unspecified else field.brandColor
                    if (field.drawableRes != 0) {
                        Icon(
                            painter = painterResource(field.drawableRes),
                            contentDescription = field.label,
                            modifier = Modifier.size(48.dp).align(Alignment.Center),
                            tint = tint
                        )
                    } else if (field.materialIcon != null) {
                        Icon(
                            imageVector = field.materialIcon,
                            contentDescription = field.label,
                            modifier = Modifier.size(48.dp).align(Alignment.Center),
                            tint = tint
                        )
                    }
                    // Open link bottom-right
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = "Open",
                        tint = Color(0xFF555555),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).size(20.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Nexus") },
            text = { Text("Remove this Nexus from your contacts?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteContact(contactId)
                    showDeleteDialog = false
                    onNavigateBack()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
