package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PersonalCard
import com.kryptoxotis.nexus.presentation.auth.AuthState
import com.kryptoxotis.nexus.presentation.auth.AuthViewModel
import com.kryptoxotis.nexus.presentation.theme.NexusTeal

import com.kryptoxotis.nexus.presentation.theme.NexusCardColors
import com.kryptoxotis.nexus.presentation.theme.neuRaised
import com.kryptoxotis.nexus.presentation.theme.neuInset
import com.kryptoxotis.nexus.presentation.theme.neonGlow
import com.kryptoxotis.nexus.presentation.theme.resolveCardAppearance
import com.kryptoxotis.nexus.util.QrCodeGenerator
import com.kryptoxotis.nexus.util.QrContentResolver
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kryptoxotis.nexus.presentation.theme.neuCircle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
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
            cards.filter { it.cardType != CardType.BUSINESS_CARD && (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true)) }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
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

            // Active card compact banner OR search + create
            item(key = "status_area") {
                if (activeCard != null) {
                    ActiveCardBanner(
                        card = activeCard!!,
                        onClick = { onNavigateToCardDetail(activeCard!!.id) }
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Search bar (inset well)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neuInset(cornerRadius = 16.dp)
                        ) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
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
                        // Create a Card button
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
            }

            // Card list
            if (cards.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
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
                        val dragHandle = Modifier.longPressDraggableHandle(
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
                        CardItem(
                            card = card,
                            isActive = card.isActive,
                            isDragging = isDragging,
                            dragEnabled = dragEnabled,
                            extraModifier = dragHandle,
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
                            },
                            onDragStarted = {},
                            onDragStopped = {}
                        )
                    }
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

        // QR Code — dark cards (black bg, colored text): black bg + colored QR
        //           light cards (colored bg, white text): card-color bg + black QR
        val qrContent = QrContentResolver.resolve(card)
        val (_, isDarkCard) = NexusCardColors.parse(card.color)
        val appearance = resolveCardAppearance(card.color)
        val cardColor = appearance.neonColor.copy(alpha = 1f)
        val qrFg = if (isDarkCard) cardColor.toArgb() else 0xFF000000.toInt()
        val qrBg = if (isDarkCard) 0xFF0A0A0A.toInt() else cardColor.toArgb()
        val boxBg = if (isDarkCard) Color(0xFF0A0A0A) else cardColor
        var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
        LaunchedEffect(qrContent, qrFg, qrBg) {
            val old = qrBitmap
            qrBitmap = try {
                QrCodeGenerator.generate(qrContent, 400, foregroundColor = qrFg, backgroundColor = qrBg)
            } catch (e: Exception) {
                null
            }
            old?.recycle()
        }
        DisposableEffect(Unit) {
            onDispose { qrBitmap?.recycle() }
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
                        .background(boxBg)
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
private fun ActiveCardBanner(card: PersonalCard, onClick: () -> Unit) {
    val appearance = resolveCardAppearance(card.color, hasImage = card.imageUrl != null)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .neuRaised(cornerRadius = 16.dp, neonColor = appearance.neonColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, appearance.borderColor.copy(alpha = 0.3f)),
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
                        text = card.title,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CardItem(
    card: PersonalCard,
    isActive: Boolean,
    isDragging: Boolean,
    isStackDropTarget: Boolean = false,
    dragEnabled: Boolean,
    onClick: () -> Unit,
    onLongPressEdit: () -> Unit,
    onQrClick: () -> Unit,
    onDragStarted: () -> Unit,
    onDragStopped: () -> Unit,
    extraModifier: Modifier = Modifier
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
    val activeGlow = if (isActive) Modifier.neonGlow(appearance.neonColor, cornerRadius = 16.dp, elevation = 10.dp) else Modifier

    // Drag visual feedback
    val targetScale = when {
        isDragging -> 1.05f
        isStackDropTarget -> 1.08f
        else -> 1f
    }
    val dragScale by animateFloatAsState(targetScale, label = "dragScale")
    val dragAlpha by animateFloatAsState(if (isDragging) 0.85f else 1f, label = "dragAlpha")
    val dragModifier = Modifier.graphicsLayer {
        scaleX = dragScale
        scaleY = dragScale
        alpha = dragAlpha
    }

    // Gesture modifier
    fun Modifier.cardGestures(): Modifier = if (dragEnabled) {
        this.combinedClickable(
            onClick = onClick,
            onLongClick = { } // consumed by longPressDraggableHandle in extraModifier
        ).then(extraModifier)
    } else {
        this.combinedClickable(
            onClick = onClick,
            onLongClick = onLongPressEdit
        )
    }

    if (isCoin) {
        // Coin (circle) layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(dragModifier)
                .cardGestures(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                border = BorderStroke(2.5.dp, appearance.borderColor.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(appearance.gradient),
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
        // Card layout
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(dragModifier)
                .then(activeGlow)
                .neuRaised(cornerRadius = 16.dp, elevation = 10.dp, neonColor = if (isActive) appearance.neonColor else null)
                .cardGestures(),
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
                // Title centered, QR bottom-right
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
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

