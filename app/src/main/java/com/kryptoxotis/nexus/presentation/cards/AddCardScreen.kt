package com.kryptoxotis.nexus.presentation.cards

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.presentation.theme.Dimens
import com.kryptoxotis.nexus.presentation.theme.NexusCardColors
import com.kryptoxotis.nexus.presentation.theme.displayBright
import com.kryptoxotis.nexus.presentation.theme.displayDark
import com.kryptoxotis.nexus.presentation.theme.NexusScaffold
import com.kryptoxotis.nexus.presentation.theme.neuRaised
import com.kryptoxotis.nexus.presentation.theme.neuInset
import com.kryptoxotis.nexus.presentation.theme.neonGlow
import com.kryptoxotis.nexus.presentation.theme.NexusSurface
import com.kryptoxotis.nexus.presentation.theme.neuCircle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.focus.onFocusChanged
import com.kryptoxotis.nexus.R
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    viewModel: PersonalCardViewModel,
    organizationId: String? = null,
    myCardOnly: Boolean = false,
    onNavigateBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf<CardType?>(null) }
    // Custom cards choose what they carry: free text, wifi credentials,
    // a phone number, an email, or a location. All are CUSTOM-typed cards
    // whose content is the matching standard payload (WIFI:/tel:/mailto:/maps).
    var customMode by remember { mutableStateOf("text") }
    var networkPassword by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf(NexusCardColors.palette[0].brightHex) }
    var isDarkMode by remember { mutableStateOf(false) }
    var cardShape by remember { mutableStateOf("card") }

    // Business card fields
    var bcName by remember { mutableStateOf("") }
    var bcJobTitle by remember { mutableStateOf("") }
    var bcCompany by remember { mutableStateOf("") }
    var bcPhone by remember { mutableStateOf("") }
    var bcEmail by remember { mutableStateOf("") }
    var bcWebsite by remember { mutableStateOf("") }
    var bcLinkedin by remember { mutableStateOf("") }
    var bcInstagram by remember { mutableStateOf("") }
    var bcTwitter by remember { mutableStateOf("") }
    var bcGithub by remember { mutableStateOf("") }
    var bcFacebook by remember { mutableStateOf("") }
    var bcYoutube by remember { mutableStateOf("") }
    var bcTiktok by remember { mutableStateOf("") }
    var bcDiscord by remember { mutableStateOf("") }
    var bcTwitch by remember { mutableStateOf("") }
    var bcWhatsapp by remember { mutableStateOf("") }

    // Toggle state for business card fields (name is always required)
    var enabledFields by remember {
        mutableStateOf(setOf("name", "jobTitle", "company", "phone", "email"))
    }

    // File picker state
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableStateOf<Long?>(null) }
    var fileUploadUrl by remember { mutableStateOf<String?>(null) }

    // Image picker state
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUploadUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            // Get filename + size from URI
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                it.moveToFirst()
                selectedFileName = if (nameIndex >= 0) it.getString(nameIndex) else uri.lastPathSegment
                selectedFileSize = if (sizeIndex >= 0 && !it.isNull(sizeIndex)) it.getLong(sizeIndex) else null
            } ?: run { selectedFileName = uri.lastPathSegment }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    // Handle file/image upload completion -> create card
    LaunchedEffect(uiState) {
        if (uiState is CardUiState.FileUploaded) {
            val url = (uiState as CardUiState.FileUploaded).url
            if (isUploadingImage) {
                // Image upload completed — now create the card
                isUploadingImage = false
                imageUploadUrl = url
                if (selectedType == CardType.FILE) {
                    // FILE card: file was already uploaded, image just finished
                    viewModel.addCard(
                        cardType = CardType.FILE,
                        title = title,
                        content = fileUploadUrl,
                        icon = icon.ifBlank { null },
                        color = NexusCardColors.encode(selectedColorHex, isDarkMode),
                        imageUrl = url,
                        cardShape = cardShape
                    )
                } else {
                    val type = selectedType ?: return@LaunchedEffect
                    viewModel.addCard(
                        cardType = type,
                        title = title,
                        content = content.ifBlank { null },
                        icon = icon.ifBlank { null },
                        color = NexusCardColors.encode(selectedColorHex, isDarkMode),
                        imageUrl = url,
                        cardShape = cardShape
                    )
                }
            } else {
                // File upload completed (for FILE type cards)
                fileUploadUrl = url
                if (selectedImageUri != null && imageUploadUrl == null) {
                    // Need to upload image next
                    isUploadingImage = true
                    val uri = selectedImageUri!!
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    val mimeType = context.contentResolver.getType(uri)
                    if (bytes != null) {
                        viewModel.uploadFile(bytes, "card-image-${System.currentTimeMillis()}.jpg", mimeType)
                    } else {
                        isUploadingImage = false
                    }
                } else {
                    viewModel.addCard(
                        cardType = CardType.FILE,
                        title = title,
                        content = url,
                        icon = icon.ifBlank { null },
                        color = NexusCardColors.encode(selectedColorHex, isDarkMode),
                        imageUrl = imageUploadUrl,
                        cardShape = cardShape
                    )
                }
            }
        }
        if (uiState is CardUiState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    NexusScaffold(
        title = when {
            selectedType == null && myCardOnly -> "Create My Nexus"
            selectedType == null -> "Create card"
            else -> "New card"
        },
        subtitle = if (selectedType == null) "Choose a type" else "Details",
        onBack = {
            if (selectedType == null) onNavigateBack()
            else {
                selectedType = null
                customMode = "text"
            }
        },
        bottomBar = false
    ) {
        if (selectedType == null) {
            // Type selector — 2-column grid
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.screenPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.gap)
            ) {
                if (!myCardOnly) {
                    // Row 1: Link + File
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardTypeOption(
                            icon = Icons.Default.Link,
                            title = "Link",
                            description = "Opens a URL when tapped via NFC",
                            onClick = { selectedType = CardType.LINK },
                            modifier = Modifier.weight(1f)
                        )
                        CardTypeOption(
                            icon = Icons.Default.AttachFile,
                            title = "File",
                            description = "Upload a file to share",
                            onClick = { selectedType = CardType.FILE },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Row 2: Custom (wifi, phone, email, location and free text live inside it)
                    CardTypeOption(
                        icon = Icons.Default.Notes,
                        title = "Custom",
                        description = "Wifi, phone, email, location or any text you want to send",
                        onClick = {
                            selectedType = CardType.CUSTOM
                            customMode = "text"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    CardTypeOption(
                        icon = Icons.Default.Badge,
                        title = "Nexus",
                        description = "Your digital identity card",
                        onClick = { selectedType = CardType.BUSINESS_CARD },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // Card form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.screenPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.gap)
            ) {
                if (selectedType == CardType.BUSINESS_CARD) {
                    // Field toggle icons with brand colors
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
                    @OptIn(ExperimentalLayoutApi::class)
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
                                        if (isOn) Modifier.border(1.dp, field.brandColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        else Modifier
                                    )
                                    .clickable {
                                        enabledFields = if (isOn) enabledFields - field.key else enabledFields + field.key
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val iconTint = when {
                                    !isOn -> Color(0xFF444444)
                                    field.gradientIcon -> Color.Unspecified
                                    else -> field.brandColor
                                }
                                if (field.drawableRes != 0) {
                                    Icon(
                                        painter = painterResource(field.drawableRes),
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

                    Spacer(modifier = Modifier.height(4.dp))

                    // Track previous auto-filled values so we only overwrite auto-filled fields
                    var autoUser by remember { mutableStateOf("") }
                    var autoEmail by remember { mutableStateOf("") }

                    fun deriveUsername(company: String) = company.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
                    fun deriveEmail(name: String, company: String): String {
                        val first = name.trim().split("\\s+".toRegex()).firstOrNull()?.lowercase()?.replace(Regex("[^a-z]"), "") ?: return ""
                        val domain = company.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
                        return if (first.isNotBlank() && domain.isNotBlank()) "$first@$domain.com" else ""
                    }

                    // Structured business card fields (name always shown)
                    NeuInput(value = bcName, onValueChange = { newName ->
                        bcName = newName
                        // Auto-fill email from name + company
                        val newAutoEmail = deriveEmail(newName, bcCompany)
                        if ("email" in enabledFields && (bcEmail.isBlank() || bcEmail == autoEmail)) {
                            bcEmail = newAutoEmail
                        }
                        autoEmail = newAutoEmail
                    }, label = "Full Name *")
                    if ("jobTitle" in enabledFields) {
                        NeuInput(value = bcJobTitle, onValueChange = { bcJobTitle = it }, label = "Job Title")
                    }
                    if ("company" in enabledFields) {
                        NeuInput(value = bcCompany, onValueChange = { newCompany ->
                            bcCompany = newCompany
                            val newUser = deriveUsername(newCompany)
                            val prevUser = autoUser
                            // Auto-fill social fields
                            val socials = mapOf(
                                "instagram" to { v: String -> bcInstagram = v }, "twitter" to { v: String -> bcTwitter = v },
                                "github" to { v: String -> bcGithub = v }, "linkedin" to { v: String -> bcLinkedin = v },
                                "facebook" to { v: String -> bcFacebook = v }, "youtube" to { v: String -> bcYoutube = v },
                                "tiktok" to { v: String -> bcTiktok = v }, "twitch" to { v: String -> bcTwitch = v }
                            )
                            val getters = mapOf(
                                "instagram" to bcInstagram, "twitter" to bcTwitter, "github" to bcGithub,
                                "linkedin" to bcLinkedin, "facebook" to bcFacebook, "youtube" to bcYoutube,
                                "tiktok" to bcTiktok, "twitch" to bcTwitch
                            )
                            socials.forEach { (key, setter) ->
                                if (key in enabledFields) {
                                    val cur = getters[key] ?: ""
                                    if (cur.isBlank() || cur == prevUser) setter(newUser)
                                }
                            }
                            autoUser = newUser
                            // Auto-fill email
                            val newAutoEmail = deriveEmail(bcName, newCompany)
                            if ("email" in enabledFields && (bcEmail.isBlank() || bcEmail == autoEmail)) {
                                bcEmail = newAutoEmail
                            }
                            autoEmail = newAutoEmail
                            // Auto-fill website
                            if ("website" in enabledFields) {
                                val domain = newCompany.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
                                val prevDomain = prevUser
                                if (bcWebsite.isBlank() || bcWebsite == "$prevDomain.com") {
                                    bcWebsite = if (domain.isNotBlank()) "$domain.com" else ""
                                }
                            }
                        }, label = "Company")
                    }
                    if ("phone" in enabledFields) {
                        NeuInput(
                            value = bcPhone,
                            onValueChange = {
                                bcPhone = it
                                // Auto-populate whatsapp if user hasn't typed in it
                                if (bcWhatsapp.isBlank() || bcWhatsapp == bcPhone.dropLast(1)) {
                                    bcWhatsapp = it
                                }
                            },
                            label = "Phone",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }
                    if ("email" in enabledFields) {
                        NeuInput(
                            value = bcEmail, onValueChange = { bcEmail = it }, label = "Email",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }
                    if ("website" in enabledFields) {
                        NeuInput(
                            value = bcWebsite, onValueChange = { bcWebsite = it }, label = "Website",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                        )
                    }
                    if ("linkedin" in enabledFields) {
                        NeuInput(value = bcLinkedin, onValueChange = { bcLinkedin = it }, label = "LinkedIn username")
                    }
                    if ("instagram" in enabledFields) {
                        NeuInput(value = bcInstagram, onValueChange = { bcInstagram = it }, label = "Instagram @username")
                    }
                    if ("twitter" in enabledFields) {
                        NeuInput(value = bcTwitter, onValueChange = { bcTwitter = it }, label = "X @username")
                    }
                    if ("github" in enabledFields) {
                        NeuInput(value = bcGithub, onValueChange = { bcGithub = it }, label = "GitHub username")
                    }
                    if ("facebook" in enabledFields) {
                        NeuInput(value = bcFacebook, onValueChange = { bcFacebook = it }, label = "Facebook username")
                    }
                    if ("youtube" in enabledFields) {
                        NeuInput(value = bcYoutube, onValueChange = { bcYoutube = it }, label = "YouTube @channel")
                    }
                    if ("tiktok" in enabledFields) {
                        NeuInput(value = bcTiktok, onValueChange = { bcTiktok = it }, label = "TikTok @username")
                    }
                    if ("discord" in enabledFields) {
                        NeuInput(value = bcDiscord, onValueChange = { bcDiscord = it }, label = "Discord username or invite")
                    }
                    if ("twitch" in enabledFields) {
                        NeuInput(value = bcTwitch, onValueChange = { bcTwitch = it }, label = "Twitch username")
                    }
                    if ("whatsapp" in enabledFields) {
                        NeuInput(
                            value = bcWhatsapp, onValueChange = { bcWhatsapp = it }, label = "WhatsApp",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }
                } else {
                    // Custom hosts wifi/phone/email/location/free-text — the point of
                    // Custom is you choose what it carries
                    if (selectedType == CardType.CUSTOM) {
                        Text(
                            text = "What it carries",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val customOptions = listOf(
                            "text" to "Text",
                            "wifi" to "Wifi",
                            "phone" to "Phone",
                            "email" to "Email",
                            "location" to "Location"
                        )
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            customOptions.forEach { (key, label) ->
                                val selected = customMode == key
                                Box(
                                    modifier = Modifier
                                        .then(if (selected) Modifier.neuInset(cornerRadius = 12.dp) else Modifier.neuRaised(cornerRadius = 12.dp))
                                        .clickable { customMode = key }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) { Text(label, color = MaterialTheme.colorScheme.onSurface) }
                            }
                        }
                    }
                    when {
                        selectedType == CardType.CUSTOM && customMode == "wifi" -> {
                            NeuInput(value = title, onValueChange = { title = it }, label = "Network name (SSID) *")
                            NeuInput(
                                value = networkPassword,
                                onValueChange = { networkPassword = it },
                                label = "Password",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )
                        }
                        selectedType == CardType.CUSTOM && customMode == "phone" -> {
                            NeuInput(value = title, onValueChange = { title = it }, label = "Title *")
                            NeuInput(
                                value = content, onValueChange = { content = it }, label = "Phone number *",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }
                        selectedType == CardType.CUSTOM && customMode == "email" -> {
                            NeuInput(value = title, onValueChange = { title = it }, label = "Title *")
                            NeuInput(
                                value = content, onValueChange = { content = it }, label = "Email address *",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                        }
                        selectedType == CardType.CUSTOM && customMode == "location" -> {
                            NeuInput(value = title, onValueChange = { title = it }, label = "Title *")
                            NeuInput(value = content, onValueChange = { content = it }, label = "Address or place *")
                        }
                        else -> {
                            NeuInput(value = title, onValueChange = { title = it }, label = "Title *")
                        }
                    }
                }

                if (selectedType == CardType.FILE) {
                    // Dashed drop-target row: tap to browse, shows chosen name + size
                    FileDropTarget(
                        fileName = selectedFileName,
                        fileSize = selectedFileSize,
                        enabled = uiState !is CardUiState.Loading,
                        onClick = { filePickerLauncher.launch("*/*") }
                    )

                    if (uiState is CardUiState.Loading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = "Uploading file...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (selectedType != CardType.BUSINESS_CARD &&
                    (selectedType != CardType.CUSTOM || customMode == "text")
                ) {
                    // Standard content field for non-FILE, non-BUSINESS_CARD types
                    NeuInput(
                        value = content,
                        onValueChange = { content = it },
                        label = when (selectedType) {
                            CardType.LINK -> "URL *"
                            CardType.CONTACT -> "Contact Info"
                            CardType.SOCIAL_MEDIA -> "Profile URL *"
                            else -> "Content"
                        },
                        singleLine = selectedType == CardType.LINK || selectedType == CardType.SOCIAL_MEDIA,
                        minLines = if (selectedType == CardType.CONTACT || selectedType == CardType.CUSTOM) 3 else 1,
                        keyboardOptions = if (selectedType == CardType.LINK || selectedType == CardType.SOCIAL_MEDIA) {
                            KeyboardOptions(keyboardType = KeyboardType.Uri)
                        } else {
                            KeyboardOptions.Default
                        }
                    )
                }

                // Live preview
                val previewTitle = if (selectedType == CardType.BUSINESS_CARD) bcName else title
                val previewSubtitle = when {
                    selectedType == CardType.BUSINESS_CARD -> listOfNotNull(
                        bcJobTitle.ifBlank { null },
                        bcCompany.ifBlank { null }
                    ).joinToString(" at ")
                    selectedType == CardType.CUSTOM && customMode == "wifi" -> "Wifi network"
                    selectedType == CardType.CUSTOM && customMode == "phone" -> "Phone"
                    selectedType == CardType.CUSTOM && customMode == "email" -> "Email"
                    selectedType == CardType.CUSTOM && customMode == "location" -> "Location"
                    selectedType == CardType.FILE -> selectedFileName ?: ""
                    else -> content
                }
                CardPreview(
                    title = previewTitle,
                    subtitle = previewSubtitle,
                    cardShape = cardShape,
                    storedColor = NexusCardColors.encode(selectedColorHex, isDarkMode),
                    imageUri = selectedImageUri,
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
                    ) { Text("Light", color = MaterialTheme.colorScheme.onSurface) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(if (isDarkMode) Modifier.neuInset(cornerRadius = 12.dp) else Modifier.neuRaised(cornerRadius = 12.dp))
                            .clickable { isDarkMode = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("Dark", color = MaterialTheme.colorScheme.onSurface) }
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
                // Selected color name
                val selectedEntry = NexusCardColors.findByHex(selectedColorHex)
                if (selectedEntry != null) {
                    Text(
                        text = selectedEntry.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Card image (optional, all card types)
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is CardUiState.Loading
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedImageUri != null) "Change Card Image" else "Add Card Image (Optional)")
                }

                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Card image preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        // Validate URLs for link/social types
                        if ((selectedType == CardType.LINK || selectedType == CardType.SOCIAL_MEDIA) && content.isNotBlank()) {
                            content = content.trim()
                            val blockedSchemes = listOf("javascript:", "data:", "file:", "content:", "intent:", "blob:", "vbscript:")
                            if (blockedSchemes.any { content.lowercase().startsWith(it) }) {
                                viewModel.setError("Invalid URL scheme")
                                return@Button
                            }
                            if (!content.startsWith("http://") && !content.startsWith("https://")) {
                                content = "https://$content"
                            }
                        }
                        if (selectedType == CardType.FILE) {
                            // Upload file first, then card is created in LaunchedEffect
                            val uri = selectedFileUri
                            val name = selectedFileName
                            if (uri != null && name != null) {
                                val fileSize = try {
                                    context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
                                } catch (_: Exception) {
                                    viewModel.setError("Could not access file")
                                    return@Button
                                }
                                if (fileSize > 10 * 1024 * 1024) {
                                    viewModel.setError("File must be under 10 MB")
                                    return@Button
                                }
                                val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                                val mimeType = context.contentResolver.getType(uri)
                                if (bytes != null) {
                                    viewModel.uploadFile(bytes, name, mimeType)
                                } else {
                                    viewModel.setError("Could not read file")
                                }
                            }
                        } else if (selectedImageUri != null) {
                            // Upload image first, then card is created in LaunchedEffect
                            isUploadingImage = true
                            val uri = selectedImageUri!!
                            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                            val mimeType = context.contentResolver.getType(uri)
                            if (bytes != null) {
                                viewModel.uploadFile(bytes, "card-image-${System.currentTimeMillis()}.jpg", mimeType)
                            } else {
                                isUploadingImage = false
                                viewModel.setError("Could not read image")
                            }
                        } else if (selectedType == CardType.BUSINESS_CARD) {
                            // Convert usernames to full URLs
                            fun toUrl(value: String, base: String, stripAt: Boolean = true): String {
                                val v = value.trim()
                                if (v.isBlank()) return v
                                if (v.startsWith("http://") || v.startsWith("https://")) return v
                                val clean = if (stripAt) v.removePrefix("@") else v
                                return "$base$clean"
                            }

                            val blockedSchemes = listOf("javascript:", "data:", "file:", "content:", "intent:", "blob:", "vbscript:")
                            val urlFields = listOfNotNull(
                                if ("website" in enabledFields) bcWebsite else null,
                                if ("linkedin" in enabledFields) toUrl(bcLinkedin, "https://linkedin.com/in/") else null,
                                if ("instagram" in enabledFields) toUrl(bcInstagram, "https://instagram.com/") else null,
                                if ("twitter" in enabledFields) toUrl(bcTwitter, "https://x.com/") else null,
                                if ("github" in enabledFields) toUrl(bcGithub, "https://github.com/") else null,
                                if ("facebook" in enabledFields) toUrl(bcFacebook, "https://facebook.com/") else null,
                                if ("youtube" in enabledFields) toUrl(bcYoutube, "https://youtube.com/@", stripAt = false) else null,
                                if ("tiktok" in enabledFields) toUrl(bcTiktok, "https://tiktok.com/@", stripAt = false) else null,
                                if ("discord" in enabledFields) bcDiscord else null,
                                if ("twitch" in enabledFields) toUrl(bcTwitch, "https://twitch.tv/") else null
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
                                website = if ("website" in enabledFields) bcWebsite.trim().let { if (it.isNotBlank() && !it.startsWith("http")) "https://$it" else it } else "",
                                linkedin = if ("linkedin" in enabledFields) toUrl(bcLinkedin, "https://linkedin.com/in/") else "",
                                instagram = if ("instagram" in enabledFields) toUrl(bcInstagram, "https://instagram.com/") else "",
                                twitter = if ("twitter" in enabledFields) toUrl(bcTwitter, "https://x.com/") else "",
                                github = if ("github" in enabledFields) toUrl(bcGithub, "https://github.com/") else "",
                                facebook = if ("facebook" in enabledFields) toUrl(bcFacebook, "https://facebook.com/") else "",
                                youtube = if ("youtube" in enabledFields) toUrl(bcYoutube, "https://youtube.com/@", stripAt = false) else "",
                                tiktok = if ("tiktok" in enabledFields) toUrl(bcTiktok, "https://tiktok.com/@", stripAt = false) else "",
                                discord = if ("discord" in enabledFields) bcDiscord else "",
                                twitch = if ("twitch" in enabledFields) toUrl(bcTwitch, "https://twitch.tv/") else "",
                                whatsapp = if ("whatsapp" in enabledFields) bcWhatsapp else "",
                                organizationId = organizationId ?: ""
                            )
                            viewModel.addCard(
                                cardType = CardType.BUSINESS_CARD,
                                title = bcName,
                                content = bcData.toJson(),
                                icon = icon.ifBlank { null },
                                color = NexusCardColors.encode(selectedColorHex, isDarkMode),
                                cardShape = cardShape
                            )
                        } else {
                            val type = selectedType ?: return@Button
                            // Custom cards carry the standard payload for their mode —
                            // WIFI:/tel:/mailto:/maps links that NFC and QR readers understand
                            val cardContent = if (type == CardType.CUSTOM) {
                                when (customMode) {
                                    "wifi" -> "WIFI:T:WPA;S:${title.trim()};P:${networkPassword};;"
                                    "phone" -> "tel:${content.trim()}"
                                    "email" -> "mailto:${content.trim()}"
                                    "location" -> "https://maps.google.com/?q=" + java.net.URLEncoder.encode(content.trim(), "UTF-8")
                                    else -> content.ifBlank { null }
                                }
                            } else {
                                content.ifBlank { null }
                            }
                            viewModel.addCard(
                                cardType = type,
                                title = title,
                                content = cardContent,
                                icon = icon.ifBlank { null },
                                color = NexusCardColors.encode(selectedColorHex, isDarkMode),
                                cardShape = cardShape
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = (if (selectedType == CardType.BUSINESS_CARD) bcName.isNotBlank() else title.isNotBlank())
                            && uiState !is CardUiState.Loading
                            && (selectedType != CardType.FILE || selectedFileUri != null)
                            && (selectedType != CardType.LINK || content.isNotBlank())
                            && (selectedType != CardType.SOCIAL_MEDIA || content.isNotBlank())
                ) {
                    if (uiState is CardUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (selectedType == CardType.FILE) "Upload & Add Card" else "Add Card")
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
}

/** Dashed drop-target for the File card type — taps open the system picker. */
@Composable
private fun FileDropTarget(
    fileName: String?,
    fileSize: Long?,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (fileName != null) Color(0xFF037A68).copy(alpha = 0.6f) else Color(0xFF3A3A3A)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(10.dp.toPx() / 2, 8.dp.toPx() / 2)
                        )
                    )
                )
            }
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .neuInset(cornerRadius = 21.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (fileName != null) Icons.Default.InsertDriveFile else Icons.Default.UploadFile,
                contentDescription = null,
                tint = Color(0xFF037A68),
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName ?: "Choose a file",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = com.kryptoxotis.nexus.presentation.theme.NexusTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (fileName != null) formatFileSize(fileSize) else "Tap to browse — any file type",
                fontSize = 12.sp,
                color = com.kryptoxotis.nexus.presentation.theme.NexusTextSecondary
            )
        }
        if (fileName != null) {
            Text(
                text = "Change",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF037A68)
            )
        }
    }
}

private fun formatFileSize(bytes: Long?): String = when {
    bytes == null -> "Unknown size"
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024 -> "%.0f KB".format(bytes / 1_024.0)
    else -> "$bytes B"
}

@Composable
private fun NeuInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    var isFocused by remember { mutableStateOf(false) }
    val isActive = isFocused || value.isNotEmpty()
    val hasAsterisk = "*" in label
    val cleanLabel = label.replace(" *", "").replace("*", "").trim()
    val labelColor by animateColorAsState(
        if (isFocused) Color(0xFF037A68) else com.kryptoxotis.nexus.presentation.theme.NexusTextSecondary, label = "lc"
    )
    val labelSize by animateFloatAsState(if (isActive) 11f else 14f, label = "ls")
    val shape = RoundedCornerShape(16.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        minLines = if (!singleLine) minLines else 1,
        modifier = modifier
            .fillMaxWidth()
            .neuInset(cornerRadius = 16.dp)
            .then(
                if (isFocused) Modifier.border(
                    1.5.dp, Color(0xFF037A68).copy(alpha = 0.5f), shape
                ) else Modifier
            )
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        textStyle = TextStyle(color = com.kryptoxotis.nexus.presentation.theme.NexusControlText, fontSize = 15.sp),
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(Color(0xFF037A68)),
        decorationBox = { innerTextField ->
            Column {
                Row {
                    Text(cleanLabel, color = labelColor, fontSize = labelSize.sp)
                    if (hasAsterisk) {
                        Text(" *", color = Color(0xFFF95B1A), fontSize = labelSize.sp)
                    }
                }
                if (isActive) Spacer(Modifier.height(4.dp))
                innerTextField()
            }
        }
    )
}

@Composable
private fun CardTypeOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .neuRaised(cornerRadius = 18.dp)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Icon in a dark circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(com.kryptoxotis.nexus.presentation.theme.NexusDeep),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = com.kryptoxotis.nexus.presentation.theme.NexusTextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = com.kryptoxotis.nexus.presentation.theme.NexusTextSecondary
            )
        }
    }
}
