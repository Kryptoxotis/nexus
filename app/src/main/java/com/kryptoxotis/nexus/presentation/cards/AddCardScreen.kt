package com.kryptoxotis.nexus.presentation.cards

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType

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
    var color by remember { mutableStateOf("") }
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
                        color = color.ifBlank { null },
                        imageUrl = url,
                        cardShape = cardShape
                    )
                } else {
                    viewModel.addCard(
                        cardType = selectedType!!,
                        title = title,
                        content = content.ifBlank { null },
                        icon = icon.ifBlank { null },
                        color = color.ifBlank { null },
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
                    }
                } else {
                    viewModel.addCard(
                        cardType = CardType.FILE,
                        title = title,
                        content = url,
                        icon = icon.ifBlank { null },
                        color = color.ifBlank { null },
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedType == null) "Choose Card Type" else "Add Card") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedType != null) selectedType = null
                        else onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (selectedType == null) {
            // Type selector
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (myCardOnly) "Create My Card" else "What type of card?",
                    style = MaterialTheme.typography.titleMedium
                )

                if (!myCardOnly) {
                    CardTypeOption(
                        icon = Icons.Default.Link,
                        title = "Link",
                        description = "Website URL - opens browser when tapped via NFC",
                        onClick = { selectedType = CardType.LINK }
                    )

                    CardTypeOption(
                        icon = Icons.Default.AttachFile,
                        title = "File",
                        description = "Upload a file to share",
                        onClick = { selectedType = CardType.FILE }
                    )

                    CardTypeOption(
                        icon = Icons.Default.Contacts,
                        title = "Contact",
                        description = "Contact info card",
                        onClick = { selectedType = CardType.CONTACT }
                    )

                    CardTypeOption(
                        icon = Icons.Default.Share,
                        title = "Social Media",
                        description = "Social media profile link",
                        onClick = { selectedType = CardType.SOCIAL_MEDIA }
                    )

                    CardTypeOption(
                        icon = Icons.Default.CreditCard,
                        title = "Custom",
                        description = "Custom text or data card",
                        onClick = { selectedType = CardType.CUSTOM }
                    )
                }

                CardTypeOption(
                    icon = Icons.Default.Badge,
                    title = "Business Card",
                    description = "Professional contact card with structured fields",
                    onClick = { selectedType = CardType.BUSINESS_CARD }
                )
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
                Text(
                    text = "Card Type: ${selectedType!!.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (selectedType == CardType.BUSINESS_CARD) {
                    // Structured business card fields
                    OutlinedTextField(
                        value = bcName,
                        onValueChange = { bcName = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("John Doe") }
                    )
                    OutlinedTextField(
                        value = bcJobTitle,
                        onValueChange = { bcJobTitle = it },
                        label = { Text("Job Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Software Engineer") }
                    )
                    OutlinedTextField(
                        value = bcCompany,
                        onValueChange = { bcCompany = it },
                        label = { Text("Company") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Acme Inc.") }
                    )
                    OutlinedTextField(
                        value = bcPhone,
                        onValueChange = { bcPhone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        placeholder = { Text("+1 555-123-4567") }
                    )
                    OutlinedTextField(
                        value = bcEmail,
                        onValueChange = { bcEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        placeholder = { Text("john@example.com") }
                    )
                    OutlinedTextField(
                        value = bcWebsite,
                        onValueChange = { bcWebsite = it },
                        label = { Text("Website") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        placeholder = { Text("https://example.com") }
                    )
                    OutlinedTextField(
                        value = bcLinkedin,
                        onValueChange = { bcLinkedin = it },
                        label = { Text("LinkedIn") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("https://linkedin.com/in/johndoe") }
                    )
                    OutlinedTextField(
                        value = bcInstagram,
                        onValueChange = { bcInstagram = it },
                        label = { Text("Instagram") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("https://instagram.com/johndoe") }
                    )
                    OutlinedTextField(
                        value = bcTwitter,
                        onValueChange = { bcTwitter = it },
                        label = { Text("Twitter / X") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("https://x.com/johndoe") }
                    )
                    OutlinedTextField(
                        value = bcGithub,
                        onValueChange = { bcGithub = it },
                        label = { Text("GitHub") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("https://github.com/johndoe") }
                    )
                } else {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                when (selectedType) {
                                    CardType.LINK -> "My Website"
                                    CardType.FILE -> "My Resume"
                                    CardType.CONTACT -> "John Doe"
                                    CardType.SOCIAL_MEDIA -> "Instagram"
                                    CardType.CUSTOM -> "My Card"
                                    else -> ""
                                }
                            )
                        }
                    )
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
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = {
                            Text(
                                when (selectedType) {
                                    CardType.LINK -> "URL *"
                                    CardType.CONTACT -> "Contact Info"
                                    CardType.SOCIAL_MEDIA -> "Profile URL *"
                                    CardType.CUSTOM -> "Content"
                                    else -> "Content"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = selectedType == CardType.LINK || selectedType == CardType.SOCIAL_MEDIA,
                        minLines = if (selectedType == CardType.CONTACT || selectedType == CardType.CUSTOM) 3 else 1,
                        placeholder = {
                            Text(
                                when (selectedType) {
                                    CardType.LINK -> "https://example.com"
                                    CardType.SOCIAL_MEDIA -> "https://instagram.com/username"
                                    CardType.CONTACT -> "Phone: ...\nEmail: ..."
                                    else -> ""
                                }
                            )
                        },
                        keyboardOptions = if (selectedType == CardType.LINK || selectedType == CardType.SOCIAL_MEDIA) {
                            KeyboardOptions(keyboardType = KeyboardType.Uri)
                        } else {
                            KeyboardOptions.Default
                        }
                    )
                }

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
                    FilterChip(
                        selected = cardShape == "card",
                        onClick = { cardShape = "card" },
                        label = { Text("Card") },
                        leadingIcon = if (cardShape == "card") {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = cardShape == "coin",
                        onClick = { cardShape = "coin" },
                        label = { Text("Coin") },
                        leadingIcon = if (cardShape == "coin") {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Card color (only when no image selected)
                if (selectedImageUri == null) {
                    Text(
                        text = "Card Color",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = color == "",
                            onClick = { color = "" },
                            label = { Text("Default") },
                            leadingIcon = if (color == "") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = color == "#FF6B35",
                            onClick = { color = "#FF6B35" },
                            label = { Text("Orange") },
                            leadingIcon = if (color == "#FF6B35") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = color == "#4A90D9",
                            onClick = { color = "#4A90D9" },
                            label = { Text("Blue") },
                            leadingIcon = if (color == "#4A90D9") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = color == "#2ECC71",
                            onClick = { color = "#2ECC71" },
                            label = { Text("Green") },
                            leadingIcon = if (color == "#2ECC71") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = color == "#9B59B6",
                            onClick = { color = "#9B59B6" },
                            label = { Text("Purple") },
                            leadingIcon = if (color == "#9B59B6") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = color == "#E74C3C",
                            onClick = { color = "#E74C3C" },
                            label = { Text("Red") },
                            leadingIcon = if (color == "#E74C3C") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                            if (content.startsWith("javascript:") || content.startsWith("data:")) {
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
                                val fileSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
                                if (fileSize > 10 * 1024 * 1024) {
                                    viewModel.setError("File must be under 10 MB")
                                    return@Button
                                }
                                val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                                val mimeType = context.contentResolver.getType(uri)
                                if (bytes != null) {
                                    viewModel.uploadFile(bytes, name, mimeType)
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
                            }
                        } else if (selectedType == CardType.BUSINESS_CARD) {
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
                                color = color.ifBlank { null },
                                cardShape = cardShape
                            )
                        } else {
                            viewModel.addCard(
                                cardType = selectedType!!,
                                title = title,
                                content = content.ifBlank { null },
                                icon = icon.ifBlank { null },
                                color = color.ifBlank { null },
                                cardShape = cardShape
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = (if (selectedType == CardType.BUSINESS_CARD) bcName.isNotBlank() else title.isNotBlank())
                            && uiState !is CardUiState.Loading
                            && (selectedType != CardType.FILE || selectedFileUri != null)
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
private fun CardTypeOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
