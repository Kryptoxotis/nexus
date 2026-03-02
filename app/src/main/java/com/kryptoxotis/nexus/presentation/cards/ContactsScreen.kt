package com.kryptoxotis.nexus.presentation.cards

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kryptoxotis.nexus.R
import com.kryptoxotis.nexus.data.local.ReceivedCardEntity
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.presentation.theme.neuRaised
import com.kryptoxotis.nexus.presentation.theme.neonGlow
import com.kryptoxotis.nexus.presentation.theme.resolveCardAppearance
import com.kryptoxotis.nexus.presentation.theme.NexusCardColors
import com.kryptoxotis.nexus.util.QrCodeGenerator
import com.kryptoxotis.nexus.util.QrContentResolver
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ReceivedCardViewModel,
    personalCardViewModel: PersonalCardViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToScanCard: () -> Unit,
    onNavigateToCreateMyCard: () -> Unit,
    onNavigateToEditCard: (String) -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    val cards by personalCardViewModel.cards.collectAsState()

    // Find the user's BUSINESS_CARD (= "My Card")
    val myCard = cards.firstOrNull { it.cardType == CardType.BUSINESS_CARD }

    // Parse original data once (stable, never modified)
    val originalContent = remember(myCard?.id) { myCard?.content ?: "" }

    // Emulation overlay state
    data class EmulatingCard(
        val label: String, val brandColor: Color, val nfcContent: String,
        val materialIcon: ImageVector? = null, val drawableRes: Int = 0,
        val gradientIcon: Boolean = false, val isMainCard: Boolean = false
    )
    var emulatingCard by remember { mutableStateOf<EmulatingCard?>(null) }
    var nexusExpanded by remember { mutableStateOf(false) }
    // Fields excluded from the "All Info" vCard share
    var excludedFromShare by remember { mutableStateOf(setOf<String>()) }

    // Deactivate card when overlay is dismissed (card content was never changed)
    val isEmulating = emulatingCard != null
    DisposableEffect(isEmulating) {
        onDispose {
            if (isEmulating && myCard != null) {
                personalCardViewModel.deactivateCard(myCard.id)
            }
        }
    }

    // Emulation overlay
    if (emulatingCard != null) {
        val ec = emulatingCard!!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(com.kryptoxotis.nexus.presentation.theme.NexusBackground)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { emulatingCard = null }
                .systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            // Close button
            IconButton(
                onClick = { emulatingCard = null },
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF888888))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (ec.isMainCard) {
                    // Main card emulation — show full card with name
                    val appearance = resolveCardAppearance(myCard?.color)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .neonGlow(appearance.neonColor, cornerRadius = 16.dp, elevation = 14.dp)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {},
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Brush.linearGradient(listOf(appearance.borderColor, appearance.borderColor.copy(alpha = 0.3f))))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.586f).background(appearance.gradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ec.label,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = appearance.textColor
                            )
                        }
                    }
                } else {
                    // Sub-card emulation — show brand icon
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .neonGlow(ec.brandColor, cornerRadius = 16.dp, elevation = 14.dp)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {},
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Brush.linearGradient(listOf(ec.brandColor, ec.brandColor.copy(alpha = 0.3f))))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.586f)
                                .background(Brush.linearGradient(listOf(Color(0xFF1A1A1A), Color(0xFF111111)))),
                            contentAlignment = Alignment.Center
                        ) {
                            val tint = if (ec.gradientIcon) Color.Unspecified else ec.brandColor
                            if (ec.drawableRes != 0) {
                                Icon(painter = painterResource(ec.drawableRes), contentDescription = ec.label, modifier = Modifier.size(64.dp), tint = tint)
                            } else if (ec.materialIcon != null) {
                                Icon(imageVector = ec.materialIcon, contentDescription = ec.label, modifier = Modifier.size(64.dp), tint = tint)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Ready to tap", style = MaterialTheme.typography.bodyMedium, color = ec.brandColor)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToScanCard) {
                Icon(Icons.Default.Nfc, contentDescription = "Scan Card")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── My Card section ──
            item(key = "my_card_header") {
                Text(
                    text = "My Nexus",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item(key = "my_card") {
                if (myCard != null) {
                    // Parse from saved original, not the live (possibly overridden) content
                    val data = if (originalContent.isNotBlank()) {
                        BusinessCardData.fromJson(originalContent)
                    } else null
                    var showQrSheet by remember { mutableStateOf(false) }
                    var qrContent by remember { mutableStateOf("") }
                    var qrLabel by remember { mutableStateOf("") }
                    var qrBrandColor by remember { mutableStateOf(Color(0xFF037A68)) }
                    val context = androidx.compose.ui.platform.LocalContext.current

                    val appearance = resolveCardAppearance(myCard.color)

                    // ── Main card: full credit-card style, tap=expand, tap again=emulate, long-press=edit ──
                    @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuRaised(cornerRadius = 16.dp, elevation = 10.dp)
                            .combinedClickable(
                                onClick = {
                                    if (nexusExpanded) {
                                        // Second tap: emulate filtered vCard
                                        val fullData = BusinessCardData.fromJson(originalContent)
                                        val filtered = fullData.copy(
                                            phone = if ("phone" in excludedFromShare) "" else fullData.phone,
                                            email = if ("email" in excludedFromShare) "" else fullData.email,
                                            website = if ("website" in excludedFromShare) "" else fullData.website,
                                            instagram = if ("instagram" in excludedFromShare) "" else fullData.instagram,
                                            twitter = if ("twitter" in excludedFromShare) "" else fullData.twitter,
                                            github = if ("github" in excludedFromShare) "" else fullData.github,
                                            linkedin = if ("linkedin" in excludedFromShare) "" else fullData.linkedin,
                                            facebook = if ("facebook" in excludedFromShare) "" else fullData.facebook,
                                            youtube = if ("youtube" in excludedFromShare) "" else fullData.youtube,
                                            tiktok = if ("tiktok" in excludedFromShare) "" else fullData.tiktok,
                                            discord = if ("discord" in excludedFromShare) "" else fullData.discord,
                                            twitch = if ("twitch" in excludedFromShare) "" else fullData.twitch,
                                            whatsapp = if ("whatsapp" in excludedFromShare) "" else fullData.whatsapp
                                        )
                                        val vcard = filtered.toVCard()
                                        personalCardViewModel.activateCardWithOverride(myCard.id, isUri = false, nfcContent = vcard, context = context)
                                        emulatingCard = EmulatingCard(
                                            label = myCard.title, brandColor = Color(0xFF037A68),
                                            nfcContent = "", isMainCard = true
                                        )
                                    } else {
                                        nexusExpanded = true
                                    }
                                },
                                onLongClick = { onNavigateToEditCard(myCard.id) }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(
                                listOf(appearance.borderColor.copy(alpha = 0.5f), appearance.borderColor.copy(alpha = 0.2f))
                            )
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.586f)
                                .background(appearance.gradient)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                // Name + subtitle centered
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = myCard.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = appearance.textColor
                                    )
                                    val subtitle = data?.subtitle()
                                    if (subtitle != null) {
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = appearance.textColor.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                // QR bottom-left
                                IconButton(
                                    onClick = {
                                        qrContent = QrContentResolver.resolve(myCard)
                                        qrLabel = myCard.title
                                        qrBrandColor = appearance.neonColor
                                        showQrSheet = true
                                    },
                                    modifier = Modifier.align(Alignment.BottomStart).size(32.dp)
                                ) {
                                    Icon(Icons.Default.QrCode2, contentDescription = "QR Code", tint = appearance.textColor.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                                }
                                // NFC bottom-right
                                Icon(Icons.Default.Nfc, contentDescription = null, tint = appearance.textColor.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.BottomEnd).size(22.dp))
                            }
                        }
                    }

                    // ── Sub-cards: shown when main card tapped ──
                    if (nexusExpanded && data != null) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Tap outside to collapse
                        Text(
                            text = "Tap a card to share  ·  Tap outside to close",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF555555),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { nexusExpanded = false }
                                .padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        data class NexusLink(
                            val key: String, val label: String, val nfcContent: String,
                            val brandColor: Color,
                            val materialIcon: ImageVector? = null,
                            val drawableRes: Int = 0, val gradientIcon: Boolean = false
                        )
                        val links = buildList {
                            if (data.phone.isNotBlank()) add(NexusLink("phone", "Phone", "tel:${data.phone}", Color(0xFF037A68), materialIcon = Icons.Default.Phone))
                            if (data.email.isNotBlank()) add(NexusLink("email", "Email", "mailto:${data.email}", Color(0xFFFA5700), materialIcon = Icons.Default.Email))
                            if (data.website.isNotBlank()) add(NexusLink("website", "Website", data.website, Color(0xFF037A68), materialIcon = Icons.Default.Language))
                            if (data.instagram.isNotBlank()) add(NexusLink("instagram", "Instagram", data.instagram, Color(0xFFD62976), drawableRes = R.drawable.ic_social_instagram, gradientIcon = true))
                            if (data.twitter.isNotBlank()) add(NexusLink("twitter", "X", data.twitter, Color(0xFFEFEFEF), drawableRes = R.drawable.ic_social_x))
                            if (data.github.isNotBlank()) add(NexusLink("github", "GitHub", data.github, Color(0xFFEFEFEF), drawableRes = R.drawable.ic_social_github))
                            if (data.linkedin.isNotBlank()) add(NexusLink("linkedin", "LinkedIn", data.linkedin, Color(0xFF0A66C2), drawableRes = R.drawable.ic_social_linkedin))
                            if (data.facebook.isNotBlank()) add(NexusLink("facebook", "Facebook", data.facebook, Color(0xFF1877F2), drawableRes = R.drawable.ic_social_facebook))
                            if (data.youtube.isNotBlank()) add(NexusLink("youtube", "YouTube", data.youtube, Color(0xFFFF0000), drawableRes = R.drawable.ic_social_youtube, gradientIcon = true))
                            if (data.tiktok.isNotBlank()) add(NexusLink("tiktok", "TikTok", data.tiktok, Color(0xFFEE1D52), drawableRes = R.drawable.ic_social_tiktok))
                            if (data.discord.isNotBlank()) add(NexusLink("discord", "Discord", data.discord, Color(0xFF5865F2), drawableRes = R.drawable.ic_social_discord, gradientIcon = true))
                            if (data.twitch.isNotBlank()) add(NexusLink("twitch", "Twitch", data.twitch, Color(0xFF9146FF), drawableRes = R.drawable.ic_social_twitch, gradientIcon = true))
                            if (data.whatsapp.isNotBlank()) add(NexusLink("whatsapp", "WhatsApp", "https://wa.me/${data.whatsapp.replace(Regex("[^0-9+]"), "")}", Color(0xFF25D366), drawableRes = R.drawable.ic_social_whatsapp))
                        }

                        links.forEach { link ->
                            val isExcluded = link.key in excludedFromShare
                            NexusSubCard(
                                label = link.label,
                                brandColor = link.brandColor,
                                materialIcon = link.materialIcon,
                                drawableRes = link.drawableRes,
                                gradientIcon = link.gradientIcon,
                                context = context,
                                isExcludedFromShare = isExcluded,
                                onToggleShare = {
                                    excludedFromShare = if (isExcluded) excludedFromShare - link.key else excludedFromShare + link.key
                                },
                                onTap = {
                                    // Activate as this link and show emulation overlay
                                    personalCardViewModel.activateCardWithOverride(myCard.id, isUri = true, nfcContent = link.nfcContent, context = context)
                                    emulatingCard = EmulatingCard(
                                        label = link.label, brandColor = link.brandColor,
                                        nfcContent = link.nfcContent,
                                        materialIcon = link.materialIcon,
                                        drawableRes = link.drawableRes,
                                        gradientIcon = link.gradientIcon
                                    )
                                },
                                onOpenLink = {
                                    try {
                                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(link.nfcContent)))
                                    } catch (_: Exception) { }
                                },
                                onQrClick = {
                                    qrContent = link.nfcContent
                                    qrLabel = link.label
                                    qrBrandColor = link.brandColor
                                    showQrSheet = true
                                }
                            )
                        }

                        // No cleanup needed — card content is never modified in DB
                    }

                    // QR Code bottom sheet
                    if (showQrSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showQrSheet = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            NexusQrSheet(content = qrContent, label = qrLabel, brandColor = qrBrandColor)
                        }
                    }
                } else {
                    OutlinedCard(
                        onClick = onNavigateToCreateMyCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Create My Nexus",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Set up your business card to share via NFC",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── Contacts section ──
            item(key = "contacts_header") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Contacts",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (contacts.isEmpty()) {
                item(key = "contacts_empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No contacts yet. Scan a card to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                items(contacts, key = { it.id }) { contact ->
                    ContactListItem(
                        contact = contact,
                        onClick = { onNavigateToDetail(contact.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactListItem(
    contact: ReceivedCardEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name.ifBlank { "Unknown" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = listOfNotNull(
                    contact.jobTitle.ifBlank { null },
                    contact.company.ifBlank { null }
                ).joinToString(" at ")
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun NexusSubCard(
    label: String,
    brandColor: Color,
    materialIcon: ImageVector? = null,
    drawableRes: Int = 0,
    gradientIcon: Boolean = false,
    context: android.content.Context? = null,
    isExcludedFromShare: Boolean = false,
    onToggleShare: () -> Unit = {},
    onTap: () -> Unit,
    onOpenLink: () -> Unit = {},
    onQrClick: () -> Unit
) {
    val cardGradient = Brush.linearGradient(listOf(Color(0xFF1A1A1A), Color(0xFF111111)))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .neuRaised(cornerRadius = 16.dp, elevation = 10.dp)
            .combinedClickable(onClick = onTap),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                listOf(brandColor.copy(alpha = 0.3f), brandColor.copy(alpha = 0.1f))
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.586f)
                .background(cardGradient)
        ) {
            // Share toggle top-right
            IconButton(
                onClick = onToggleShare,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp)
            ) {
                Icon(
                    if (isExcludedFromShare) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isExcludedFromShare) "Hidden from share" else "Included in share",
                    tint = if (isExcludedFromShare) Color(0xFF444444) else brandColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            // Brand icon centered
            val iconAlpha = if (isExcludedFromShare) 0.3f else 1f
            val tint = if (gradientIcon && !isExcludedFromShare) Color.Unspecified
                       else if (isExcludedFromShare) Color(0xFF444444)
                       else brandColor
            if (drawableRes != 0) {
                Icon(
                    painter = painterResource(drawableRes),
                    contentDescription = label,
                    modifier = Modifier.size(48.dp).align(Alignment.Center),
                    tint = tint
                )
            } else if (materialIcon != null) {
                Icon(
                    imageVector = materialIcon,
                    contentDescription = label,
                    modifier = Modifier.size(48.dp).align(Alignment.Center),
                    tint = tint
                )
            }
            // QR bottom-left
            IconButton(
                onClick = onQrClick,
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).size(32.dp)
            ) {
                Icon(Icons.Default.QrCode2, contentDescription = "QR Code", tint = Color(0xFF555555), modifier = Modifier.size(20.dp))
            }
            // Open link button bottom-right
            IconButton(
                onClick = onOpenLink,
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(32.dp)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = "Open", tint = Color(0xFF555555), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NexusQrSheet(content: String, label: String, brandColor: Color = Color(0xFF037A68)) {
    var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(content, brandColor) {
        val old = qrBitmap
        qrBitmap = QrCodeGenerator.generate(content, 400, foregroundColor = brandColor.toArgb(), backgroundColor = 0xFF0A0A0A.toInt())
        old?.recycle()
    }
    DisposableEffect(Unit) { onDispose { qrBitmap?.recycle() } }

    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        qrBitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.size(240.dp).clip(RoundedCornerShape(12.dp))
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
