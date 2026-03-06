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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.presentation.theme.NexusCardColors
import com.kryptoxotis.nexus.presentation.theme.SocialIcons
import com.kryptoxotis.nexus.presentation.theme.neuRaised
import com.kryptoxotis.nexus.presentation.theme.neuInset
import com.kryptoxotis.nexus.presentation.theme.neonGlow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    // Enabled fields — initialized from which fields have content
    var enabledFields by remember(card.id) {
        mutableStateOf(buildSet {
            add("name") // always on
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
        })
    }

    // Navigate back on successful update
    LaunchedEffect(uiState) {
        if (uiState is CardUiState.Success && (uiState as CardUiState.Success).message == "Card updated") {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Card") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Type: ${card.cardType.name.replace("_", " ").lowercase()
                    .replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            if (card.cardType == CardType.BUSINESS_CARD) {
                // Field toggle icons
                data class FieldToggle(
                    val key: String,
                    val label: String,
                    val brandColor: Color,
                    val icon: ImageVector
                )

                val fieldOptions = listOf(
                    FieldToggle("jobTitle", "Job Title", Color(0xFFB0BEC5), Icons.Default.Work),
                    FieldToggle("company", "Company", Color(0xFF90A4AE), Icons.Default.Business),
                    FieldToggle("phone", "Phone", Color(0xFF037A68), Icons.Default.Phone),
                    FieldToggle("email", "Email", Color(0xFFFA5700), Icons.Default.Email),
                    FieldToggle("website", "Website", Color(0xFF037A68), Icons.Default.Language),
                    FieldToggle("linkedin", "LinkedIn", Color(0xFF0A66C2), SocialIcons.LinkedIn),
                    FieldToggle("instagram", "Instagram", Color(0xFFD62976), SocialIcons.Instagram),
                    FieldToggle("twitter", "Twitter / X", Color(0xFFEFEFEF), SocialIcons.X),
                    FieldToggle("github", "GitHub", Color(0xFFEFEFEF), SocialIcons.GitHub),
                    FieldToggle("facebook", "Facebook", Color(0xFF1877F2), SocialIcons.Facebook),
                    FieldToggle("youtube", "YouTube", Color(0xFFFF0000), SocialIcons.YouTube),
                    FieldToggle("tiktok", "TikTok", Color(0xFFEE1D52), SocialIcons.TikTok),
                    FieldToggle("discord", "Discord", Color(0xFF5865F2), SocialIcons.Discord),
                    FieldToggle("twitch", "Twitch", Color(0xFF9146FF), SocialIcons.Twitch),
                    FieldToggle("whatsapp", "WhatsApp", Color(0xFF25D366), SocialIcons.WhatsApp)
                )

                Text(
                    text = "Fields to include",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
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
                                    else Color(0xFF1A1A1A)
                                )
                                .then(
                                    if (isOn) Modifier.border(
                                        1.dp,
                                        field.brandColor.copy(alpha = 0.4f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    else Modifier
                                )
                                .clickable {
                                    enabledFields = if (isOn) {
                                        enabledFields - field.key
                                    } else {
                                        enabledFields + field.key
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = field.icon,
                                contentDescription = field.label,
                                modifier = Modifier.size(18.dp),
                                tint = if (isOn) field.brandColor else Color(0xFF444444)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Business card fields (name always shown)
                IconTextField(value = bcName, onValueChange = { bcName = it }, label = "Full Name *", icon = Icons.Default.Person)
                if ("jobTitle" in enabledFields) {
                    IconTextField(value = bcJobTitle, onValueChange = { bcJobTitle = it }, label = "Job Title", icon = Icons.Default.Work)
                }
                if ("company" in enabledFields) {
                    IconTextField(value = bcCompany, onValueChange = { bcCompany = it }, label = "Company", icon = Icons.Default.Business)
                }
                if ("phone" in enabledFields) {
                    IconTextField(value = bcPhone, onValueChange = { bcPhone = it }, label = "Phone", icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
                }
                if ("email" in enabledFields) {
                    IconTextField(value = bcEmail, onValueChange = { bcEmail = it }, label = "Email", icon = Icons.Default.Email, keyboardType = KeyboardType.Email)
                }
                if ("website" in enabledFields) {
                    IconTextField(value = bcWebsite, onValueChange = { bcWebsite = it }, label = "Website", icon = Icons.Default.Language, keyboardType = KeyboardType.Uri)
                }
                if ("linkedin" in enabledFields) {
                    IconTextField(value = bcLinkedin, onValueChange = { bcLinkedin = it }, label = "LinkedIn", icon = SocialIcons.LinkedIn)
                }
                if ("instagram" in enabledFields) {
                    IconTextField(value = bcInstagram, onValueChange = { bcInstagram = it }, label = "Instagram", icon = SocialIcons.Instagram)
                }
                if ("twitter" in enabledFields) {
                    IconTextField(value = bcTwitter, onValueChange = { bcTwitter = it }, label = "Twitter / X", icon = SocialIcons.X)
                }
                if ("github" in enabledFields) {
                    IconTextField(value = bcGithub, onValueChange = { bcGithub = it }, label = "GitHub", icon = SocialIcons.GitHub)
                }
                if ("facebook" in enabledFields) {
                    IconTextField(value = bcFacebook, onValueChange = { bcFacebook = it }, label = "Facebook", icon = SocialIcons.Facebook)
                }
                if ("youtube" in enabledFields) {
                    IconTextField(value = bcYoutube, onValueChange = { bcYoutube = it }, label = "YouTube", icon = SocialIcons.YouTube)
                }
                if ("tiktok" in enabledFields) {
                    IconTextField(value = bcTiktok, onValueChange = { bcTiktok = it }, label = "TikTok", icon = SocialIcons.TikTok)
                }
                if ("discord" in enabledFields) {
                    IconTextField(value = bcDiscord, onValueChange = { bcDiscord = it }, label = "Discord", icon = SocialIcons.Discord)
                }
                if ("twitch" in enabledFields) {
                    IconTextField(value = bcTwitch, onValueChange = { bcTwitch = it }, label = "Twitch", icon = SocialIcons.Twitch)
                }
                if ("whatsapp" in enabledFields) {
                    IconTextField(value = bcWhatsapp, onValueChange = { bcWhatsapp = it }, label = "WhatsApp", icon = SocialIcons.WhatsApp, keyboardType = KeyboardType.Phone)
                }
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
                imageUri = card.imageUrl
            )

            // Card appearance selector
            CardAppearanceSelector(
                cardShape = cardShape,
                onCardShapeChange = { cardShape = it },
                isDarkMode = isDarkMode,
                onDarkModeChange = { isDarkMode = it },
                selectedColorHex = selectedColorHex,
                onColorSelected = { selectedColorHex = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
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
                        fun toUrl(value: String, base: String, stripAt: Boolean = true): String {
                            val v = value.trim()
                            if (v.isBlank()) return v
                            if (v.startsWith("http://") || v.startsWith("https://")) return v
                            val clean = if (stripAt) v.removePrefix("@") else v
                            return "$base$clean"
                        }

                        val urlFields = listOfNotNull(
                            if ("website" in enabledFields) bcWebsite else null,
                            if ("linkedin" in enabledFields) bcLinkedin else null,
                            if ("instagram" in enabledFields) bcInstagram else null,
                            if ("twitter" in enabledFields) bcTwitter else null,
                            if ("github" in enabledFields) bcGithub else null,
                            if ("facebook" in enabledFields) bcFacebook else null,
                            if ("youtube" in enabledFields) bcYoutube else null,
                            if ("tiktok" in enabledFields) bcTiktok else null,
                            if ("twitch" in enabledFields) bcTwitch else null
                        )
                        if (urlFields.any { it.isNotBlank() && blockedSchemes.any { scheme -> it.trim().lowercase().startsWith(scheme) } }) {
                            viewModel.setError("Invalid URL scheme in one of the link fields")
                            return@Button
                        }
                        val bcData = BusinessCardData(
                            name = bcName,
                            jobTitle = if ("jobTitle" in enabledFields) bcJobTitle else "",
                            company = if ("company" in enabledFields) bcCompany else "",
                            phone = if ("phone" in enabledFields) bcPhone else "",
                            email = if ("email" in enabledFields) bcEmail else "",
                            website = if ("website" in enabledFields) bcWebsite.trim().let {
                                if (it.isNotBlank() && !it.startsWith("http")) "https://$it" else it
                            } else "",
                            linkedin = if ("linkedin" in enabledFields) toUrl(bcLinkedin, "https://linkedin.com/in/") else "",
                            instagram = if ("instagram" in enabledFields) toUrl(bcInstagram, "https://instagram.com/") else "",
                            twitter = if ("twitter" in enabledFields) toUrl(bcTwitter, "https://x.com/") else "",
                            github = if ("github" in enabledFields) toUrl(bcGithub, "https://github.com/") else "",
                            facebook = if ("facebook" in enabledFields) toUrl(bcFacebook, "https://facebook.com/") else "",
                            youtube = if ("youtube" in enabledFields) toUrl(bcYoutube, "https://youtube.com/@", stripAt = false) else "",
                            tiktok = if ("tiktok" in enabledFields) toUrl(bcTiktok, "https://tiktok.com/@", stripAt = false) else "",
                            discord = if ("discord" in enabledFields) bcDiscord else "",
                            twitch = if ("twitch" in enabledFields) toUrl(bcTwitch, "https://twitch.tv/") else "",
                            whatsapp = if ("whatsapp" in enabledFields) bcWhatsapp else ""
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

@Composable
private fun IconTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}
