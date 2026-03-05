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
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.presentation.theme.NexusCardColors
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
                OutlinedTextField(value = bcName, onValueChange = { bcName = it }, label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = bcJobTitle, onValueChange = { bcJobTitle = it }, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = bcCompany, onValueChange = { bcCompany = it }, label = { Text("Company") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = bcPhone, onValueChange = { bcPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = bcEmail, onValueChange = { bcEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                OutlinedTextField(value = bcWebsite, onValueChange = { bcWebsite = it }, label = { Text("Website") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
                OutlinedTextField(value = bcLinkedin, onValueChange = { bcLinkedin = it }, label = { Text("LinkedIn") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = bcInstagram, onValueChange = { bcInstagram = it }, label = { Text("Instagram") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = bcTwitter, onValueChange = { bcTwitter = it }, label = { Text("Twitter / X") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = bcGithub, onValueChange = { bcGithub = it }, label = { Text("GitHub") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
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
                        val bcData = BusinessCardData(
                            name = bcName, jobTitle = bcJobTitle, company = bcCompany,
                            phone = bcPhone, email = bcEmail, website = bcWebsite,
                            linkedin = bcLinkedin, instagram = bcInstagram,
                            twitter = bcTwitter, github = bcGithub
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
