package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.R
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.presentation.theme.Dimens
import com.kryptoxotis.nexus.presentation.theme.NexusScaffold
import com.kryptoxotis.nexus.presentation.theme.NexusCardColors
import com.kryptoxotis.nexus.presentation.theme.displayBright
import com.kryptoxotis.nexus.presentation.theme.displayDark
import com.kryptoxotis.nexus.presentation.theme.neuRaised
import com.kryptoxotis.nexus.presentation.theme.neuInset
import com.kryptoxotis.nexus.presentation.theme.neonGlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    cardId: String,
    viewModel: PersonalCardViewModel,
    onNavigateBack: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val card = cards.find { it.id == cardId }
    val uiState by viewModel.uiState.collectAsState()

    if (card == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    var title by remember(card.id) { mutableStateOf(card.title) }
    var content by remember(card.id) { mutableStateOf(card.content ?: "") }
    val (initialHex, initialDark) = remember(card.id) { NexusCardColors.parse(card.color) }
    var selectedColorHex by remember(card.id) { mutableStateOf(initialHex) }
    var isDarkMode by remember(card.id) { mutableStateOf(initialDark) }
    var cardShape by remember(card.id) { mutableStateOf(card.cardShape) }

    // Business card fields (parsed from JSON content)
    val initialBc = remember(card.id) {
        if (card.cardType == CardType.BUSINESS_CARD) BusinessCardData.fromJson(card.content ?: "") else BusinessCardData()
    }
    var bcName by remember(card.id) { mutableStateOf(initialBc.name) }
    var bcJobTitle by remember(card.id) { mutableStateOf(initialBc.jobTitle) }
    var bcCompany by remember(card.id) { mutableStateOf(initialBc.company) }
    var bcPhone by remember(card.id) { mutableStateOf(initialBc.phone) }
    var bcEmail by remember(card.id) { mutableStateOf(initialBc.email) }
    var bcWebsite by remember(card.id) { mutableStateOf(initialBc.website) }
    var bcLinkedin by remember(card.id) { mutableStateOf(initialBc.linkedin) }
    var bcInstagram by remember(card.id) { mutableStateOf(initialBc.instagram) }
    var bcTwitter by remember(card.id) { mutableStateOf(initialBc.twitter) }
    var bcGithub by remember(card.id) { mutableStateOf(initialBc.github) }
    var bcFacebook by remember(card.id) { mutableStateOf(initialBc.facebook) }
    var bcYoutube by remember(card.id) { mutableStateOf(initialBc.youtube) }
    var bcTiktok by remember(card.id) { mutableStateOf(initialBc.tiktok) }
    var bcDiscord by remember(card.id) { mutableStateOf(initialBc.discord) }
    var bcTwitch by remember(card.id) { mutableStateOf(initialBc.twitch) }
    var bcWhatsapp by remember(card.id) { mutableStateOf(initialBc.whatsapp) }

    // Which fields the card carries — same toggles as the create form. Fields
    // that already have a value start enabled; toggling one off drops it on save.
    var enabledFields by remember(card.id) {
        mutableStateOf(
            buildSet {
                add("name")
                if (initialBc.jobTitle.isNotBlank()) add("jobTitle")
                if (initialBc.company.isNotBlank()) add("company")
                if (initialBc.phone.isNotBlank()) add("phone")
                if (initialBc.email.isNotBlank()) add("email")
                if (initialBc.website.isNotBlank()) add("website")
                if (initialBc.linkedin.isNotBlank()) add("linkedin")
                if (initialBc.instagram.isNotBlank()) add("instagram")
                if (initialBc.twitter.isNotBlank()) add("twitter")
                if (initialBc.github.isNotBlank()) add("github")
                if (initialBc.facebook.isNotBlank()) add("facebook")
                if (initialBc.youtube.isNotBlank()) add("youtube")
                if (initialBc.tiktok.isNotBlank()) add("tiktok")
                if (initialBc.discord.isNotBlank()) add("discord")
                if (initialBc.twitch.isNotBlank()) add("twitch")
                if (initialBc.whatsapp.isNotBlank()) add("whatsapp")
            }
        )
    }

    // Navigate back on successful update
    LaunchedEffect(uiState) {
        if (uiState is CardUiState.Success && (uiState as CardUiState.Success).message == "Card updated") {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    NexusScaffold(
        title = "Edit card",
        subtitle = "Details",
        onBack = onNavigateBack,
        bottomBar = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.screenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.gap)
        ) {
            Text(
                text = "Type: ${card.cardType.name.replace("_", " ").lowercase()
                    .replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            if (card.cardType == CardType.BUSINESS_CARD) {
                // Same eye toggles as the create form — turn a field off here and
                // it comes off the card, no quick-edit needed
                data class FieldToggle(
                    val key: String, val label: String, val brandColor: Color,
                    val materialIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
                    val drawableRes: Int = 0, val gradientIcon: Boolean = false
                )
                val fieldOptions = listOf(
                    FieldToggle("jobTitle", "Job Title", Color(0xFFB0BEC5), materialIcon = Icons.Default.Work),
                    FieldToggle("company", "Company", Color(0xFF90A4AE), materialIcon = Icons.Default.Business),
                    FieldToggle("phone", "Phone", Color(0xFF037A68), materialIcon = Icons.Default.Phone),
                    FieldToggle("email", "Email", Color(0xFF037A68), materialIcon = Icons.Default.Email),
                    FieldToggle("website", "Website", Color(0xFF037A68), materialIcon = Icons.Default.Language),
                    FieldToggle("linkedin", "LinkedIn", Color(0xFF0A66C2), drawableRes = R.drawable.ic_social_linkedin),
                    FieldToggle("instagram", "Instagram", Color(0xFFD62976), drawableRes = R.drawable.ic_social_instagram, gradientIcon = true),
                    FieldToggle("twitter", "Twitter / X", Color(0xFFEFEFEF), drawableRes = R.drawable.ic_social_x),
                    FieldToggle("github", "GitHub", Color(0xFFEFEFEF), drawableRes = R.drawable.ic_social_github),
                    FieldToggle("facebook", "Facebook", Color(0xFF1877F2), drawableRes = R.drawable.ic_social_facebook),
                    FieldToggle("youtube", "YouTube", Color(0xFFFF0000), drawableRes = R.drawable.ic_social_youtube, gradientIcon = true),
                    FieldToggle("tiktok", "TikTok", Color(0xFFEE1D52), drawableRes = R.drawable.ic_social_tiktok),
                    FieldToggle("discord", "Discord", Color(0xFF5865F2), drawableRes = R.drawable.ic_social_discord, gradientIcon = true),
                    FieldToggle("twitch", "Twitch", Color(0xFF9146FF), drawableRes = R.drawable.ic_social_twitch, gradientIcon = true),
                    FieldToggle("whatsapp", "WhatsApp", Color(0xFF25D366), drawableRes = R.drawable.ic_social_whatsapp)
                )
                Text(
                    text = "Fields to include",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    fieldOptions.forEach { field ->
                        val isOn = field.key in enabledFields
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isOn) field.brandColor.copy(alpha = 0.15f)
                                    else com.kryptoxotis.nexus.presentation.theme.NexusSurface
                                )
                                .then(
                                    if (isOn) Modifier.border(1.dp, field.brandColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    else Modifier
                                )
                                .clickable {
                                    enabledFields = if (isOn) enabledFields - field.key else enabledFields + field.key
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val iconTint = when {
                                !isOn -> com.kryptoxotis.nexus.presentation.theme.NexusMutedText
                                field.gradientIcon -> Color.Unspecified
                                else -> field.brandColor
                            }
                            if (field.drawableRes != 0) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(field.drawableRes),
                                    contentDescription = field.label,
                                    modifier = Modifier.size(18.dp),
                                    tint = iconTint
                                )
                            } else if (field.materialIcon != null) {
                                Icon(
                                    imageVector = field.materialIcon,
                                    contentDescription = field.label,
                                    modifier = Modifier.size(18.dp),
                                    tint = iconTint
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(value = bcName, onValueChange = { bcName = it }, label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if ("jobTitle" in enabledFields) OutlinedTextField(value = bcJobTitle, onValueChange = { bcJobTitle = it }, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if ("company" in enabledFields) OutlinedTextField(value = bcCompany, onValueChange = { bcCompany = it }, label = { Text("Company") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if ("phone" in enabledFields) OutlinedTextField(value = bcPhone, onValueChange = { bcPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                if ("email" in enabledFields) OutlinedTextField(value = bcEmail, onValueChange = { bcEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                if ("website" in enabledFields) OutlinedTextField(value = bcWebsite, onValueChange = { bcWebsite = it }, label = { Text("Website") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
                if ("linkedin" in enabledFields) OutlinedTextField(value = bcLinkedin, onValueChange = { bcLinkedin = it }, label = { Text("LinkedIn") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if ("instagram" in enabledFields) OutlinedTextField(value = bcInstagram, onValueChange = { bcInstagram = it }, label = { Text("Instagram") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if ("twitter" in enabledFields) OutlinedTextField(value = bcTwitter, onValueChange = { bcTwitter = it }, label = { Text("Twitter / X") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if ("github" in enabledFields) OutlinedTextField(value = bcGithub, onValueChange = { bcGithub = it }, label = { Text("GitHub") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if ("facebook" in enabledFields) OutlinedTextField(value = bcFacebook, onValueChange = { bcFacebook = it }, label = { Text("Facebook") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if ("youtube" in enabledFields) OutlinedTextField(value = bcYoutube, onValueChange = { bcYoutube = it }, label = { Text("YouTube") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if ("tiktok" in enabledFields) OutlinedTextField(value = bcTiktok, onValueChange = { bcTiktok = it }, label = { Text("TikTok") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if ("discord" in enabledFields) OutlinedTextField(value = bcDiscord, onValueChange = { bcDiscord = it }, label = { Text("Discord") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if ("twitch" in enabledFields) OutlinedTextField(value = bcTwitch, onValueChange = { bcTwitch = it }, label = { Text("Twitch") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if ("whatsapp" in enabledFields) OutlinedTextField(value = bcWhatsapp, onValueChange = { bcWhatsapp = it }, label = { Text("WhatsApp") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            } else {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = {
                        Text(
                            when (card.cardType) {
                                CardType.LINK -> "URL"
                                CardType.SOCIAL_MEDIA -> "Profile URL"
                                CardType.CONTACT -> "Contact Info"
                                else -> "Content"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = card.cardType == CardType.LINK || card.cardType == CardType.SOCIAL_MEDIA,
                    minLines = if (card.cardType == CardType.CONTACT || card.cardType == CardType.CUSTOM) 3 else 1,
                    keyboardOptions = if (card.cardType == CardType.LINK || card.cardType == CardType.SOCIAL_MEDIA) {
                        KeyboardOptions(keyboardType = KeyboardType.Uri)
                    } else {
                        KeyboardOptions.Default
                    }
                )
            }

            // Live preview
            val previewTitle = if (card.cardType == CardType.BUSINESS_CARD) bcName else title
            val previewSubtitle = if (card.cardType == CardType.BUSINESS_CARD) {
                listOfNotNull(
                    bcJobTitle.ifBlank { null },
                    bcCompany.ifBlank { null }
                ).joinToString(" at ")
            } else content
            CardPreview(
                title = previewTitle,
                subtitle = previewSubtitle,
                cardShape = cardShape,
                storedColor = NexusCardColors.encode(selectedColorHex, isDarkMode),
                imageUri = card.imageUrl,
                placeholders = true
            )

            // Card shape selector
            Text(
                text = "Card Shape",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(if (cardShape == "card") Modifier.neuInset(cornerRadius = 12.dp) else Modifier.neuRaised(cornerRadius = 12.dp))
                        .clickable { cardShape = "card" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Card", color = MaterialTheme.colorScheme.onSurface) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(if (cardShape == "coin") Modifier.neuInset(cornerRadius = 12.dp) else Modifier.neuRaised(cornerRadius = 12.dp))
                        .clickable { cardShape = "coin" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Coin", color = MaterialTheme.colorScheme.onSurface) }
            }

            // Light / Dark mode toggle
            Text(
                text = "Card Mode",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(if (!isDarkMode) Modifier.neuInset(cornerRadius = 12.dp) else Modifier.neuRaised(cornerRadius = 12.dp))
                        .clickable { isDarkMode = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) { Text(if (com.kryptoxotis.nexus.presentation.theme.NexusAppearance.dark) "Light" else "Colored", color = MaterialTheme.colorScheme.onSurface) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(if (isDarkMode) Modifier.neuInset(cornerRadius = 12.dp) else Modifier.neuRaised(cornerRadius = 12.dp))
                        .clickable { isDarkMode = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) { Text(if (com.kryptoxotis.nexus.presentation.theme.NexusAppearance.dark) "Dark" else "Light", color = MaterialTheme.colorScheme.onSurface) }
            }

            // Color palette
            Text(
                text = "Card Color",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NexusCardColors.palette.forEach { entry ->
                    val isSelected = selectedColorHex == entry.brightHex
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .then(
                                if (isSelected) Modifier.neonGlow(entry.displayBright, cornerRadius = 10.dp, elevation = 8.dp)
                                else Modifier
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(entry.displayBright, entry.displayDark)))
                            .then(
                                if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(10.dp))
                                else Modifier
                            )
                            .clickable { selectedColorHex = entry.brightHex }
                    )
                }
            }
            val selectedEntry = NexusCardColors.findByHex(selectedColorHex)
            if (selectedEntry != null) {
                Text(
                    text = selectedEntry.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    // Validate URLs for link/social types
                    val blockedSchemes = listOf("javascript:", "data:", "file:", "content:", "intent:", "blob:", "vbscript:")
                    if ((card.cardType == CardType.LINK || card.cardType == CardType.SOCIAL_MEDIA) && content.isNotBlank()) {
                        content = content.trim()
                        if (blockedSchemes.any { content.lowercase().startsWith(it) }) {
                            viewModel.setError("Invalid URL scheme")
                            return@Button
                        }
                        if (!content.startsWith("http://") && !content.startsWith("https://")) {
                            content = "https://$content"
                        }
                    }
                    if (card.cardType == CardType.BUSINESS_CARD) {
                        val urlFields = listOf(bcWebsite, bcLinkedin, bcInstagram, bcTwitter, bcGithub)
                        if (urlFields.any { it.isNotBlank() && blockedSchemes.any { scheme -> it.trim().lowercase().startsWith(scheme) } }) {
                            viewModel.setError("Invalid URL scheme in one of the link fields")
                            return@Button
                        }
                        // Only enabled fields make it onto the card
                        val bcData = BusinessCardData(
                            name = bcName,
                            jobTitle = if ("jobTitle" in enabledFields) bcJobTitle else "",
                            company = if ("company" in enabledFields) bcCompany else "",
                            phone = if ("phone" in enabledFields) bcPhone else "",
                            email = if ("email" in enabledFields) bcEmail else "",
                            website = if ("website" in enabledFields) bcWebsite else "",
                            linkedin = if ("linkedin" in enabledFields) bcLinkedin else "",
                            instagram = if ("instagram" in enabledFields) bcInstagram else "",
                            twitter = if ("twitter" in enabledFields) bcTwitter else "",
                            github = if ("github" in enabledFields) bcGithub else "",
                            facebook = if ("facebook" in enabledFields) bcFacebook else "",
                            youtube = if ("youtube" in enabledFields) bcYoutube else "",
                            tiktok = if ("tiktok" in enabledFields) bcTiktok else "",
                            discord = if ("discord" in enabledFields) bcDiscord else "",
                            twitch = if ("twitch" in enabledFields) bcTwitch else "",
                            whatsapp = if ("whatsapp" in enabledFields) bcWhatsapp else "",
                            organizationId = initialBc.organizationId
                        )
                        viewModel.updateCard(
                            cardId = card.id,
                            title = bcName,
                            content = bcData.toJson(),
                            color = NexusCardColors.encode(selectedColorHex, isDarkMode),
                            cardShape = cardShape
                        )
                    } else {
                        viewModel.updateCard(
                            cardId = card.id,
                            title = title,
                            content = content.ifBlank { null },
                            color = NexusCardColors.encode(selectedColorHex, isDarkMode),
                            cardShape = cardShape
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = (if (card.cardType == CardType.BUSINESS_CARD) bcName.isNotBlank() else title.isNotBlank()) && uiState !is CardUiState.Loading
            ) {
                if (uiState is CardUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Changes")
                }
            }

            if (uiState is CardUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (uiState as CardUiState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
