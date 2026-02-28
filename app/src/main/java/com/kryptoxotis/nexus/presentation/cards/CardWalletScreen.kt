package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kryptoxotis.nexus.domain.model.AccountType
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PersonalCard
import com.kryptoxotis.nexus.presentation.auth.AuthState
import com.kryptoxotis.nexus.presentation.auth.AuthViewModel
import com.kryptoxotis.nexus.presentation.theme.NexusOrange
import com.kryptoxotis.nexus.presentation.theme.NexusBlue
import com.kryptoxotis.nexus.util.QrCodeGenerator
import com.kryptoxotis.nexus.util.QrContentResolver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardWalletScreen(
    viewModel: PersonalCardViewModel,
    authViewModel: AuthViewModel?,
    onNavigateToAddCard: () -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    onNavigateToEditCard: (String) -> Unit,
    onNavigateToScanCard: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToBusinessPasses: () -> Unit,
    onNavigateToContacts: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val activeCard by viewModel.activeCard.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val authState = authViewModel?.authState?.collectAsState()?.value
    val businessRequest = authViewModel?.businessRequest?.collectAsState()?.value

    val isAuthenticated = authState is AuthState.Authenticated
    val accountType = (authState as? AuthState.Authenticated)?.accountType

    var selectedCard by remember { mutableStateOf<PersonalCard?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showQrSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Nexus")
                        if (accountType != null) {
                            Text(
                                text = accountType.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToContacts) {
                        Icon(Icons.Default.People, contentDescription = "Contacts")
                    }
                    IconButton(onClick = onNavigateToScanCard) {
                        Icon(Icons.Default.Nfc, contentDescription = "Scan Card")
                    }
                    if (isAuthenticated) {
                        IconButton(onClick = onNavigateToBusinessPasses) {
                            Icon(Icons.Default.Badge, contentDescription = "Business Passes")
                        }
                        IconButton(onClick = onNavigateToAccounts) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Accounts")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddCard) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Business request status banner
            if (businessRequest?.status == "pending") {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Business account request pending review",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Active card hero
            item {
                if (activeCard != null) {
                    ActiveCardHero(
                        card = activeCard!!,
                        onClick = {
                            onNavigateToCardDetail(activeCard!!.id)
                        }
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Nfc,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No active card",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Activate a card to use with NFC",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (cards.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No cards yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Tap + to add your first card",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                item(key = "my_cards_header") {
                    Text(
                        text = "My Cards",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(cards, key = { it.id }) { card ->
                    CardItem(
                        card = card,
                        isActive = card.isActive,
                        onClick = {
                            viewModel.activateCard(card.id)
                            onNavigateToCardDetail(card.id)
                        },
                        onLongClick = {
                            selectedCard = card
                            showEditSheet = true
                        },
                        onQrClick = {
                            selectedCard = card
                            showQrSheet = true
                        }
                    )
                }
            }
        }

        if (uiState is CardUiState.Success) {
            LaunchedEffect(uiState) {
                viewModel.resetState()
            }
        }
    }

    // Long-press bottom sheet (edit/delete)
    if (showEditSheet && selectedCard != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showEditSheet = false
                selectedCard = null
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            CardEditSheet(
                card = selectedCard!!,
                onEditClick = {
                    showEditSheet = false
                    onNavigateToEditCard(selectedCard!!.id)
                    selectedCard = null
                },
                onDeleteClick = {
                    showEditSheet = false
                    showDeleteDialog = true
                }
            )
        }
    }

    // QR button bottom sheet
    if (showQrSheet && selectedCard != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showQrSheet = false
                selectedCard = null
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            CardQrSheet(card = selectedCard!!)
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && selectedCard != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedCard = null
            },
            title = { Text("Delete Card") },
            text = { Text("Are you sure you want to delete \"${selectedCard!!.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCard(selectedCard!!.id)
                    showDeleteDialog = false
                    selectedCard = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    selectedCard = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CardEditSheet(
    card: PersonalCard,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Card info
        Text(
            text = card.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = card.cardType.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Edit
        Button(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Card")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Delete
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete Card")
        }
    }
}

@Composable
private fun CardQrSheet(card: PersonalCard) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = card.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // QR Code
        val qrContent = QrContentResolver.resolve(card)
        var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
        LaunchedEffect(qrContent) {
            qrBitmap = try {
                QrCodeGenerator.generate(qrContent, 400)
            } catch (e: Exception) {
                null
            }
        }

        val bitmap = qrBitmap
        if (bitmap != null) {
            Card(
                modifier = Modifier.size(240.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scan to share",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActiveCardHero(card: PersonalCard, onClick: () -> Unit) {
    val gradientColors = if (card.color != null) {
        try {
            val c = Color(android.graphics.Color.parseColor(card.color))
            listOf(c.copy(alpha = 0.9f), c.copy(alpha = 0.5f))
        } catch (_: Exception) {
            listOf(Color(0xFF1A1A1A), Color(0xFF3A3A3A))
        }
    } else {
        listOf(Color(0xFF1A1A1A), Color(0xFF3A3A3A))
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.linearGradient(listOf(NexusOrange, NexusOrange.copy(alpha = 0.5f)))
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(colors = gradientColors)
                )
        ) {
            if (card.imageUrl != null) {
                AsyncImage(
                    model = card.imageUrl,
                    contentDescription = "Card image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
                // Dark scrim over image for text readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.Black.copy(alpha = 0.4f))
                )
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Icon(
                        Icons.Default.Nfc,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                val heroSubtitle = if (card.cardType == CardType.BUSINESS_CARD && card.content != null) {
                    BusinessCardData.fromJson(card.content).subtitle()
                } else {
                    card.content
                }
                if (heroSubtitle != null) {
                    Text(
                        text = heroSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = card.cardType.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CardItem(
    card: PersonalCard,
    isActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onQrClick: () -> Unit
) {
    val isCoin = card.cardShape == "coin"
    val hasImage = card.imageUrl != null
    val cardTypeLabel = card.cardType.name.replace("_", " ").lowercase()
        .replaceFirstChar { it.uppercase() }
    val glowBorder = if (isActive) BorderStroke(
        width = 2.dp,
        brush = Brush.linearGradient(listOf(NexusOrange, NexusOrange.copy(alpha = 0.5f)))
    ) else null

    // Dark gradient by default, or use card.color if set
    val gradientColors = if (card.color != null) {
        try {
            val c = Color(android.graphics.Color.parseColor(card.color))
            listOf(c.copy(alpha = 0.9f), c.copy(alpha = 0.5f))
        } catch (_: Exception) {
            listOf(Color(0xFF1A1A1A), Color(0xFF3A3A3A))
        }
    } else {
        listOf(Color(0xFF1A1A1A), Color(0xFF3A3A3A))
    }

    if (isCoin) {
        // Coin (circle) layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.size(140.dp),
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasImage) {
                        AsyncImage(
                            model = card.imageUrl,
                            contentDescription = "Card image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f))
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = card.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (hasImage) Color.White else MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(
                                Icons.Default.Nfc,
                                contentDescription = "NFC Active",
                                modifier = Modifier.size(16.dp),
                                tint = if (hasImage) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Card layout (image and no-image unified)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            shape = RoundedCornerShape(12.dp),
            border = glowBorder
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.586f)
                    .then(
                        if (!hasImage) Modifier.background(
                            Brush.linearGradient(colors = gradientColors)
                        ) else Modifier
                    )
            ) {
                if (hasImage) {
                    AsyncImage(
                        model = card.imageUrl,
                        contentDescription = "Card image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )
                }
                // Content overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top: type label
                    Text(
                        text = cardTypeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    // Bottom row: title/content + QR button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = card.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            val itemSubtitle = if (card.cardType == CardType.BUSINESS_CARD && card.content != null) {
                                BusinessCardData.fromJson(card.content).subtitle()
                            } else {
                                card.content
                            }
                            if (itemSubtitle != null) {
                                Text(
                                    text = itemSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    maxLines = 1
                                )
                            }
                        }
                        IconButton(onClick = onQrClick) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = "QR Code",
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}
