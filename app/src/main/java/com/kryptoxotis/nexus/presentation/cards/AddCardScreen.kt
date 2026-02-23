package com.kryptoxotis.nexus.presentation.cards

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.domain.model.CardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    viewModel: PersonalCardViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf<CardType?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }

    // File picker state
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var fileUploadUrl by remember { mutableStateOf<String?>(null) }

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

    // Handle file upload completion -> create card
    LaunchedEffect(uiState) {
        if (uiState is CardUiState.FileUploaded) {
            val url = (uiState as CardUiState.FileUploaded).url
            fileUploadUrl = url
            viewModel.addCard(
                cardType = CardType.FILE,
                title = title,
                content = url,
                icon = icon.ifBlank { null },
                color = color.ifBlank { null }
            )
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
                    text = "What type of card?",
                    style = MaterialTheme.typography.titleMedium
                )

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
                } else {
                    // Standard content field for non-FILE types
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

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (selectedType == CardType.FILE) {
                            // Upload file first, then card is created in LaunchedEffect
                            val uri = selectedFileUri
                            val name = selectedFileName
                            if (uri != null && name != null) {
                                val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                                val mimeType = context.contentResolver.getType(uri)
                                if (bytes != null) {
                                    viewModel.uploadFile(bytes, name, mimeType)
                                }
                            }
                        } else {
                            viewModel.addCard(
                                cardType = selectedType!!,
                                title = title,
                                content = content.ifBlank { null },
                                icon = icon.ifBlank { null },
                                color = color.ifBlank { null }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank()
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
