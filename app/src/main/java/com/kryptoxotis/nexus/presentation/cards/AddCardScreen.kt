package com.kryptoxotis.nexus.presentation.cards

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.kryptoxotis.nexus.presentation.theme.NexusCardColors
import com.kryptoxotis.nexus.presentation.theme.neuRaised
import com.kryptoxotis.nexus.presentation.theme.neuInset
import com.kryptoxotis.nexus.presentation.theme.neonGlow
import com.kryptoxotis.nexus.presentation.theme.NexusSurface
import com.kryptoxotis.nexus.presentation.theme.neuCircle
import androidx.compose.ui.focus.onFocusChanged
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

    // File picker state
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
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
            // Get filename from URI
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            selectedFileName = cursor?.use {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                it.moveToFirst()
                if (nameIndex >= 0) it.getString(nameIndex) else uri.lastPathSegment
            } ?: uri.lastPathSegment
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

    Scaffold { paddingValues ->
        if (selectedType == null) {
            // Type selector — 2-column grid
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with back button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .neuCircle(elevation = 6.dp)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF777777),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            if (myCardOnly) "Create My Card" else "Create Card",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Choose a type",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (!myCardOnly) {
                    // Row 1: Link + Contact
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
                            icon = Icons.Default.Contacts,
                            title = "Contact",
                            description = "Share your contact info card",
                            onClick = { selectedType = CardType.CONTACT },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Row 2: Social Media + Business Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardTypeOption(
                            icon = Icons.Default.Share,
                            title = "Social Media",
                            description = "Link to your social profile",
                            onClick = { selectedType = CardType.SOCIAL_MEDIA },
                            modifier = Modifier.weight(1f)
                        )
                        CardTypeOption(
                            icon = Icons.Default.Badge,
                            title = "Business Card",
                            description = "Professional card with structured fields",
                            onClick = { selectedType = CardType.BUSINESS_CARD },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Row 3: File + Custom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardTypeOption(
                            icon = Icons.Default.AttachFile,
                            title = "File",
                            description = "Upload a file to share",
                            onClick = { selectedType = CardType.FILE },
                            modifier = Modifier.weight(1f)
                        )
                        CardTypeOption(
                            icon = Icons.Default.CreditCard,
                            title = "Custom",
                            description = "Custom text or data",
                            onClick = { selectedType = CardType.CUSTOM },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    CardTypeOption(
                        icon = Icons.Default.Badge,
                        title = "Business Card",
                        description = "Professional card with structured fields",
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
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with back button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .neuCircle(elevation = 6.dp)
                            .clickable { selectedType = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF777777),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Add Card",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (selectedType == CardType.BUSINESS_CARD) {
                    // Structured business card fields
                    NeuInput(value = bcName, onValueChange = { bcName = it }, label = "Full Name *")
                    NeuInput(value = bcJobTitle, onValueChange = { bcJobTitle = it }, label = "Job Title")
                    NeuInput(value = bcCompany, onValueChange = { bcCompany = it }, label = "Company")
                    NeuInput(
                        value = bcPhone, onValueChange = { bcPhone = it }, label = "Phone",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    NeuInput(
                        value = bcEmail, onValueChange = { bcEmail = it }, label = "Email",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    NeuInput(
                        value = bcWebsite, onValueChange = { bcWebsite = it }, label = "Website",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                    NeuInput(value = bcLinkedin, onValueChange = { bcLinkedin = it }, label = "LinkedIn")
                    NeuInput(value = bcInstagram, onValueChange = { bcInstagram = it }, label = "Instagram")
                    NeuInput(value = bcTwitter, onValueChange = { bcTwitter = it }, label = "Twitter / X")
                    NeuInput(value = bcGithub, onValueChange = { bcGithub = it }, label = "GitHub")
                } else {
                    NeuInput(value = title, onValueChange = { title = it }, label = "Title *")
                }

                if (selectedType == CardType.FILE) {
                    // File picker UI
                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is CardUiState.Loading
                    ) {
                        Icon(
                            Icons.Default.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedFileName != null) "Change File" else "Choose File")
                    }

                    if (selectedFileName != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedFileName!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (uiState is CardUiState.Loading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = "Uploading file...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (selectedType != CardType.BUSINESS_CARD) {
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
                val previewSubtitle = when (selectedType) {
                    CardType.BUSINESS_CARD -> listOfNotNull(
                        bcJobTitle.ifBlank { null },
                        bcCompany.ifBlank { null }
                    ).joinToString(" at ")
                    else -> content
                }
                CardPreview(
                    title = previewTitle,
                    subtitle = previewSubtitle,
                    cardShape = cardShape,
                    storedColor = NexusCardColors.encode(selectedColorHex, isDarkMode),
                    imageUri = selectedImageUri
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
                                    if (isSelected) Modifier.neonGlow(entry.bright, cornerRadius = 10.dp, elevation = 8.dp)
                                    else Modifier
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(entry.bright, entry.dark)))
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
                            val blockedSchemes = listOf("javascript:", "data:", "file:", "content:", "intent:", "blob:", "vbscript:")
                            val urlFields = listOf(bcWebsite, bcLinkedin, bcInstagram, bcTwitter, bcGithub)
                            if (urlFields.any { it.isNotBlank() && blockedSchemes.any { scheme -> it.trim().lowercase().startsWith(scheme) } }) {
                                viewModel.setError("Invalid URL scheme in one of the link fields")
                                return@Button
                            }
                            val bcData = BusinessCardData(
                                name = bcName, jobTitle = bcJobTitle, company = bcCompany,
                                phone = bcPhone, email = bcEmail, website = bcWebsite,
                                linkedin = bcLinkedin, instagram = bcInstagram,
                                twitter = bcTwitter, github = bcGithub,
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
                            viewModel.addCard(
                                cardType = type,
                                title = title,
                                content = content.ifBlank { null },
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
        if (isFocused) Color(0xFF037A68) else Color(0xFF666666), label = "lc"
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
        textStyle = TextStyle(color = Color(0xFFD4D4D4), fontSize = 15.sp),
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
                    .background(Color(0xFF0C0C0C)),
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
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
