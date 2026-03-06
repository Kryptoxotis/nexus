package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PersonalCard
import com.kryptoxotis.nexus.platform.QrGenerator
import com.kryptoxotis.nexus.presentation.auth.AuthState
import com.kryptoxotis.nexus.presentation.auth.AuthViewModel
import com.kryptoxotis.nexus.presentation.theme.*
import com.kryptoxotis.nexus.util.QrContentResolver
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CardWalletScreen(
    viewModel: PersonalCardViewModel,
    authViewModel: AuthViewModel?,
    onNavigateToAddCard: () -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    onNavigateToEditCard: (String) -> Unit,
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

    var searchQuery by remember { mutableStateOf("") }
    val filteredCards by remember(cards, searchQuery) {
        derivedStateOf {
            cards.filter {
                it.cardType != CardType.BUSINESS_CARD &&
                    (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true))
            }
        }
    }

    // Drag-and-drop reorder state
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    var dragDidMove by remember { mutableStateOf(false) }
    var reorderableCards by remember { mutableStateOf(filteredCards) }
    LaunchedEffect(filteredCards) { reorderableCards = filteredCards }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromIndex = reorderableCards.indexOfFirst { it.id == from.key }
        val toIndex = reorderableCards.indexOfFirst { it.id == to.key }
        if (fromIndex != -1 && toIndex != -1) {
            reorderableCards = reorderableCards.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
            dragDidMove = true
        }
    }

    val dragEnabled = searchQuery.isBlank()

    Scaffold { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item(key = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Nexus",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (accountType != null) {
                            Text(
                                text = accountType.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .neuCircle(elevation = 6.dp)
                                .clickable { onNavigateToContacts() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = "Contacts",
                                tint = Color(0xFF777777),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (isAuthenticated) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .neuCircle(elevation = 6.dp)
                                    .clickable { onNavigateToBusinessPasses() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Badge,
                                    contentDescription = "Business Passes",
                                    tint = Color(0xFF777777),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .neuCircle(elevation = 6.dp)
                                    .clickable { onNavigateToAccounts() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Accounts",
                                    tint = Color(0xFF777777),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

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
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
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

            // Active card banner (when a card is activated)
            if (activeCard != null) {
                item(key = "active_banner") {
                    val appearance = resolveCardAppearance(
                        activeCard!!.color,
                        hasImage = activeCard!!.imageUrl != null
                    )
                    Card(
                        onClick = { onNavigateToCardDetail(activeCard!!.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuRaised(
                                cornerRadius = 16.dp,
                                neonColor = appearance.neonColor
                            ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.5.dp,
                            appearance.borderColor.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(appearance.gradient)
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(NexusTeal)
                                )
                                Column {
                                    Text(
                                        text = activeCard!!.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = appearance.textColor
                                    )
                                    Text(
                                        text = "Ready to tap",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = appearance.textColor.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.Nfc,
                                contentDescription = "NFC Active",
                                tint = appearance.textColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // Search + create (always visible)
            item(key = "status_area") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Search bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuInset(cornerRadius = 16.dp)
                    ) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            textStyle = TextStyle(
                                color = Color(0xFFD4D4D4),
                                fontSize = 14.sp
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(NexusTeal),
                            decorationBox = { innerTextField ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color(0xFF444444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "Search cards...",
                                                color = Color(0xFF555555),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            }
                        )
                    }
                    // Create button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuRaised(cornerRadius = 16.dp)
                            .clickable { onNavigateToAddCard() }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .neuInset(cornerRadius = 22.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = NexusTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Create a Card",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Add a new pass to use with NFC",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Card list
            if (cards.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No cards yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                "Create a card above to get started",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                item(key = "my_cards_header") {
                    Text(
                        "My Nexus",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(reorderableCards, key = { it.id }) { card ->
                    ReorderableItem(reorderableLazyListState, key = card.id) { isDragging ->
                        val dragHandle = if (dragEnabled) {
                            Modifier.longPressDraggableHandle(
                                onDragStarted = {
                                    dragDidMove = false
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDragStopped = {
                                    if (dragDidMove) {
                                        viewModel.reorderCards(reorderableCards.map { it.id })
                                    } else {
                                        selectedCard = card
                                        showEditSheet = true
                                    }
                                    dragDidMove = false
                                }
                            )
                        } else Modifier

                        val dragScale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "ds")
                        val dragAlpha by animateFloatAsState(if (isDragging) 0.85f else 1f, label = "da")

                        Box(modifier = Modifier
                            .graphicsLayer {
                                scaleX = dragScale
                                scaleY = dragScale
                                alpha = dragAlpha
                            }
                            .then(dragHandle)
                        ) {
                            CardItem(
                                card = card,
                                isActive = activeCard?.id == card.id,
                                onClick = {
                                    viewModel.activateCard(card.id)
                                    onNavigateToCardDetail(card.id)
                                },
                                onLongPressEdit = {
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
            }
        }

        if (uiState is CardUiState.Success) {
            LaunchedEffect(uiState) { viewModel.resetState() }
        }
    }

    // Long-press bottom sheet
    if (showEditSheet && selectedCard != null) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false; selectedCard = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = selectedCard!!.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedCard!!.cardType.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        showEditSheet = false
                        onNavigateToEditCard(selectedCard!!.id)
                        selectedCard = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Card")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        showEditSheet = false
                        showDeleteDialog = true
                    },
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
    }

    // QR bottom sheet
    if (showQrSheet && selectedCard != null) {
        ModalBottomSheet(
            onDismissRequest = { showQrSheet = false; selectedCard = null },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedCard!!.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                val qrContent = QrContentResolver.resolve(selectedCard!!)
                val (_, isDarkCard) = NexusCardColors.parse(selectedCard!!.color)
                val qrAppearance = resolveCardAppearance(selectedCard!!.color)
                val cardColor = qrAppearance.neonColor.copy(alpha = 1f)
                val qrFg = if (isDarkCard) cardColor.toArgb() else 0xFF000000.toInt()
                val qrBg = if (isDarkCard) 0xFF0A0A0A.toInt() else cardColor.toArgb()
                val boxBg = if (isDarkCard) Color(0xFF0A0A0A) else cardColor

                var qrBitmap by remember(selectedCard?.id) { mutableStateOf<ImageBitmap?>(null) }
                LaunchedEffect(qrContent, qrFg, qrBg) {
                    qrBitmap = try {
                        QrGenerator.generate(qrContent, 400, qrFg, qrBg)
                    } catch (_: Exception) {
                        null
                    }
                }
                val bmp = qrBitmap
                if (bmp != null) {
                    Card(
                        modifier = Modifier.size(240.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(boxBg)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp,
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
    }

    // Delete dialog
    if (showDeleteDialog && selectedCard != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; selectedCard = null },
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CardItem(
    card: PersonalCard,
    isActive: Boolean,
    onClick: () -> Unit,
    onLongPressEdit: () -> Unit,
    onQrClick: () -> Unit
) {
    val isCoin = card.cardShape == "coin"
    val hasImage = card.imageUrl != null
    val appearance = resolveCardAppearance(card.color, hasImage = hasImage)
    val cardBorder = BorderStroke(
        width = if (isActive) 3.dp else 2.5.dp,
        brush = Brush.linearGradient(
            listOf(
                appearance.borderColor.copy(alpha = if (isActive) 0.8f else 0.5f),
                appearance.borderColor.copy(alpha = if (isActive) 0.4f else 0.2f)
            )
        )
    )
    val activeGlow = if (isActive) {
        Modifier.neonGlow(appearance.neonColor, cornerRadius = 16.dp, elevation = 10.dp)
    } else {
        Modifier
    }

    if (isCoin) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongPressEdit),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                border = BorderStroke(2.5.dp, appearance.borderColor.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(appearance.gradient),
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
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = appearance.textColor,
                        maxLines = 2,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(
                        onClick = onQrClick,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCode2,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(18.dp),
                            tint = appearance.textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(activeGlow)
                .neuRaised(
                    cornerRadius = 16.dp,
                    elevation = 10.dp,
                    neonColor = if (isActive) appearance.neonColor else null
                )
                .combinedClickable(onClick = onClick, onLongClick = onLongPressEdit),
            shape = RoundedCornerShape(16.dp),
            border = cardBorder,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.586f)
                    .background(appearance.gradient)
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
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = appearance.textColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onQrClick,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            Icons.Default.QrCode2,
                            contentDescription = "QR Code",
                            tint = appearance.textColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
