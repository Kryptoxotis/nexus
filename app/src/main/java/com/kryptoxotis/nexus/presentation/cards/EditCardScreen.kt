package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType

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
    var color by remember(card.id) { mutableStateOf(card.color ?: "") }
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

            // Card color
            if (card.imageUrl == null) {
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

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    // Validate URLs for link/social types
                    if ((card.cardType == CardType.LINK || card.cardType == CardType.SOCIAL_MEDIA) && content.isNotBlank()) {
                        val trimmed = content.trim()
                        if (trimmed.startsWith("javascript:") || trimmed.startsWith("data:")) {
                            viewModel.setError("Invalid URL scheme")
                            return@Button
                        }
                        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                            content = "https://$trimmed"
                        }
                    }
                    if (card.cardType == CardType.BUSINESS_CARD) {
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
                            color = color.ifBlank { null },
                            cardShape = cardShape
                        )
                    } else {
                        viewModel.updateCard(
                            cardId = card.id,
                            title = title,
                            content = content.ifBlank { null },
                            color = color.ifBlank { null },
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
