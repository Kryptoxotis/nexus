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
                    viewModel.updateCard(
                        cardId = card.id,
                        title = title,
                        content = content.ifBlank { null },
                        color = color.ifBlank { null },
                        cardShape = cardShape
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && uiState !is CardUiState.Loading
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
