package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.domain.model.AccountType
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
    onNavigateToAccounts: () -> Unit,
    onNavigateToBusinessPasses: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val activeCard by viewModel.activeCard.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val authState = authViewModel?.authState?.collectAsState()?.value
    val businessRequest = authViewModel?.businessRequest?.collectAsState()?.value

    val isAuthenticated = authState is AuthState.Authenticated
    val accountType = (authState as? AuthState.Authenticated)?.accountType

    var selectedCard by remember { mutableStateOf<PersonalCard?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
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
                            selectedCard = activeCard
                            showBottomSheet = true
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
                item {
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
                item {
                    Text(
                        text = "My Cards",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(cards) { card ->
                    CardItem(
                        card = card,
                        isActive = card.id == activeCard?.id,
                        onClick = {
                            viewModel.activateCard(card.id)
                            selectedCard = card
                            showBottomSheet = true
                        },
                        onActivate = { viewModel.activateCard(card.id) },
                        onDeactivate = { viewModel.deactivateCard(card.id) }
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

    // Bottom Sheet
    if (showBottomSheet && selectedCard != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                selectedCard = null
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            CardQuickActionSheet(
                card = selectedCard!!,
                isActive = selectedCard!!.id == activeCard?.id,
                onDetailsClick = {
                    showBottomSheet = false
                    onNavigateToCardDetail(selectedCard!!.id)
                    selectedCard = null
                },
                onDeactivateClick = {
                    viewModel.deactivateCard(selectedCard!!.id)
                    showBottomSheet = false
                    selectedCard = null
                }
            )
        }
    }
}

@Composable
private fun CardQuickActionSheet(
    card: PersonalCard,
    isActive: Boolean,
    onDetailsClick: () -> Unit,
    onDeactivateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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

        Spacer(modifier = Modifier.height(8.dp))

        // NFC status
        if (isActive) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Nfc,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "NFC Active",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

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
                modifier = Modifier.size(200.dp),
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

        Spacer(modifier = Modifier.height(20.dp))

        // Actions
        Button(
            onClick = onDetailsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Details")
        }

        if (isActive) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onDeactivateClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Deactivate")
            }
        }
    }
}

@Composable
private fun ActiveCardHero(card: PersonalCard, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(NexusOrange, NexusBlue)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
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
                if (card.content != null) {
                    Text(
                        text = card.content,
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

@Composable
private fun CardItem(
    card: PersonalCard,
    isActive: Boolean,
    onClick: () -> Unit,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (card.cardType) {
                CardType.LINK -> Icons.Default.Link
                CardType.FILE -> Icons.Default.AttachFile
                CardType.CONTACT -> Icons.Default.Contacts
                CardType.SOCIAL_MEDIA -> Icons.Default.Share
                CardType.CUSTOM -> Icons.Default.CreditCard
            }

            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (card.content != null) {
                    Text(
                        text = card.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Text(
                    text = card.cardType.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            IconButton(
                onClick = if (isActive) onDeactivate else onActivate
            ) {
                Icon(
                    if (isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isActive) "Deactivate" else "Activate",
                    tint = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
