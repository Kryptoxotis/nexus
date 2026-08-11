package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kryptoxotis.nexus.R
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PassStatus
import com.kryptoxotis.nexus.domain.model.PersonalCard
import com.kryptoxotis.nexus.presentation.auth.AuthState
import com.kryptoxotis.nexus.presentation.auth.AuthViewModel
import com.kryptoxotis.nexus.presentation.business.BusinessViewModel
import com.kryptoxotis.nexus.domain.model.AccountType
import com.kryptoxotis.nexus.presentation.theme.Dimens
import com.kryptoxotis.nexus.presentation.theme.NexusEmptyState
import com.kryptoxotis.nexus.presentation.theme.NexusScaffold
import com.kryptoxotis.nexus.presentation.theme.NexusSurface
import com.kryptoxotis.nexus.presentation.theme.NexusTeal
import com.kryptoxotis.nexus.presentation.theme.NexusTextPrimary
import com.kryptoxotis.nexus.presentation.theme.NexusTextSecondary
import com.kryptoxotis.nexus.presentation.theme.neuInset
import com.kryptoxotis.nexus.presentation.theme.neuRaised

/** Home tabs. */
object HomeTab {
    const val CARDS = "cards"
    const val NEXUS = "nexus"
    const val PASSES = "passes"
}

/**
 * The one home screen: Cards · Nexus · Passes behind a segmented switcher.
 * Tapping any card starts NFC emulation immediately (no activate step);
 * the overlay's outside-tap stops it.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    initialTab: String,
    cardViewModel: PersonalCardViewModel,
    receivedCardViewModel: ReceivedCardViewModel,
    businessViewModel: BusinessViewModel,
    authViewModel: AuthViewModel?,
    onNavigateToAddCard: () -> Unit,
    onNavigateToCreateMyCard: () -> Unit,
    onNavigateToEditCard: (String) -> Unit,
    onNavigateToContactDetail: (String) -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToEnrollment: () -> Unit,
    onNavigateToBusinessDashboard: () -> Unit
) {
    val cards by cardViewModel.cards.collectAsState()
    val contacts by receivedCardViewModel.contacts.collectAsState()
    val passes by businessViewModel.userPasses.collectAsState()
    val authState = authViewModel?.authState?.collectAsState()?.value
    val accountType = (authState as? AuthState.Authenticated)?.accountType
    val context = LocalContext.current

    var tab by rememberSaveable(initialTab) { mutableStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }

    val myCard = cards.firstOrNull { it.cardType == CardType.BUSINESS_CARD }
    val myCardData = remember(myCard?.id, myCard?.content) {
        myCard?.content?.takeIf { it.isNotBlank() }?.let { BusinessCardData.fromJson(it) }
    }
    val walletCards = cards.filter {
        it.cardType != CardType.BUSINESS_CARD &&
            (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true))
    }

    // ── Emulation state ──
    var emulatingCard by remember { mutableStateOf<PersonalCard?>(null) }
    var emulatingNexus by remember { mutableStateOf(false) }
    var quickEditOpen by remember { mutableStateOf(false) }
    var excludedFromShare by remember { mutableStateOf(setOf<String>()) }
    var qrCard by remember { mutableStateOf<PersonalCard?>(null) }
    var editSheetCard by remember { mutableStateOf<PersonalCard?>(null) }
    var deleteDialogCard by remember { mutableStateOf<PersonalCard?>(null) }
    var removeContactId by remember { mutableStateOf<String?>(null) }

    fun filteredNexusVcard(data: BusinessCardData): String = data.copy(
        phone = if ("phone" in excludedFromShare) "" else data.phone,
        email = if ("email" in excludedFromShare) "" else data.email,
        website = if ("website" in excludedFromShare) "" else data.website,
        instagram = if ("instagram" in excludedFromShare) "" else data.instagram,
        twitter = if ("twitter" in excludedFromShare) "" else data.twitter,
        github = if ("github" in excludedFromShare) "" else data.github,
        linkedin = if ("linkedin" in excludedFromShare) "" else data.linkedin,
        facebook = if ("facebook" in excludedFromShare) "" else data.facebook,
        youtube = if ("youtube" in excludedFromShare) "" else data.youtube,
        tiktok = if ("tiktok" in excludedFromShare) "" else data.tiktok,
        discord = if ("discord" in excludedFromShare) "" else data.discord,
        twitch = if ("twitch" in excludedFromShare) "" else data.twitch,
        whatsapp = if ("whatsapp" in excludedFromShare) "" else data.whatsapp
    ).toVCard()

    fun startEmulating(card: PersonalCard, isNexus: Boolean) {
        if (isNexus && myCardData != null) {
            cardViewModel.activateCardWithOverride(
                card.id, isUri = false, nfcContent = filteredNexusVcard(myCardData), context = context
            )
        } else {
            cardViewModel.activateCard(card.id)
        }
        emulatingCard = card
        emulatingNexus = isNexus
    }

    fun stopEmulating() {
        emulatingCard?.let { cardViewModel.deactivateCard(it.id) }
        emulatingCard = null
        emulatingNexus = false
        quickEditOpen = false
    }

    DisposableEffect(Unit) {
        onDispose { emulatingCard?.let { cardViewModel.deactivateCard(it.id) } }
    }

    val overlayVisible = emulatingCard != null && !quickEditOpen && qrCard == null

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (overlayVisible) Modifier.blur(14.dp) else Modifier)
        ) {
            NexusScaffold(
                title = "Nexus",
                subtitle = accountType?.name?.lowercase()?.replaceFirstChar { it.uppercase() },
                actions = listOf<Pair<androidx.compose.ui.graphics.vector.ImageVector, () -> Unit>>(
                    Icons.Default.AccountCircle to onNavigateToAccounts
                ),
                bottomBar = true
            ) {
                HomeTabSwitcher(tab = tab, onTab = { tab = it })

                when (tab) {
                    HomeTab.NEXUS -> NexusTabContent(
                        myCard = myCard,
                        myCardData = myCardData,
                        contacts = contacts,
                        onEmulateMyNexus = { myCard?.let { startEmulating(it, true) } },
                        onEditMyNexus = { myCard?.let { onNavigateToEditCard(it.id) } },
                        onMyNexusQr = { qrCard = myCard },
                        onCreateMyNexus = onNavigateToCreateMyCard,
                        onOpenContact = onNavigateToContactDetail,
                        onHoldContact = { removeContactId = it }
                    )
                    HomeTab.PASSES -> PassesTabContent(
                        passes = passes,
                        isBusinessAccount = accountType == AccountType.BUSINESS,
                        onEnroll = onNavigateToEnrollment,
                        onBusinessTools = onNavigateToBusinessDashboard
                    )
                    else -> CardsTabContent(
                        walletCards = walletCards,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onCreate = onNavigateToAddCard,
                        onTapCard = { startEmulating(it, false) },
                        onHoldCard = { editSheetCard = it },
                        onQrCard = { qrCard = it }
                    )
                }
            }
        }

        val emCard = emulatingCard
        if (overlayVisible && emCard != null) {
            val isNexusCard = emulatingNexus
            EmulateOverlay(
                title = if (isNexusCard) (myCardData?.name ?: emCard.title) else emCard.title,
                subtitle = if (isNexusCard) (myCardData?.subtitle() ?: "") else "",
                storedColor = emCard.color,
                isNexus = isNexusCard,
                onDismiss = { stopEmulating() },
                onShowQr = { qrCard = emCard },
                onQuickEdit = { quickEditOpen = true }
            )
        }
    }

    // ── QR sheet (from wallet or from inside the overlay) ──
    val qr = qrCard
    if (qr != null) {
        ModalBottomSheet(
            onDismissRequest = { qrCard = null },
            containerColor = NexusSurface,
            tonalElevation = 0.dp,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF333333)) }
        ) {
            CardQrSheet(card = qr)
        }
    }

    // ── Quick edit what's shared (pauses emulation) ──
    if (quickEditOpen && myCardData != null) {
        QuickEditSheet(
            fields = rememberNexusQuickEditFields(myCardData),
            excluded = excludedFromShare,
            onToggle = { key ->
                excludedFromShare =
                    if (key in excludedFromShare) excludedFromShare - key
                    else excludedFromShare + key
            },
            onShareAgain = {
                quickEditOpen = false
                myCard?.let { startEmulating(it, true) }
            },
            onDismiss = { stopEmulating() }
        )
    }

    // ── Long-press: edit / delete ──
    val editCard = editSheetCard
    if (editCard != null) {
        ModalBottomSheet(
            onDismissRequest = { editSheetCard = null },
            containerColor = NexusSurface,
            tonalElevation = 0.dp,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF333333)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPadding)
                    .padding(bottom = 34.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.gapSmall)
            ) {
                Text(
                    text = editCard.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NexusTextPrimary
                )
                Text(
                    text = editCard.cardType.name.replace('_', ' ').lowercase()
                        .replaceFirstChar { it.uppercase() },
                    fontSize = 12.5.sp,
                    color = NexusTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                SheetActionRow(
                    icon = Icons.Default.Edit,
                    label = "Edit card",
                    tint = NexusTeal,
                    labelColor = Color(0xFFE0E0E0)
                ) {
                    editSheetCard = null
                    onNavigateToEditCard(editCard.id)
                }
                SheetActionRow(
                    icon = Icons.Default.Delete,
                    label = "Delete card",
                    tint = Color(0xFFFF3B30),
                    labelColor = Color(0xFFFF3B30)
                ) {
                    editSheetCard = null
                    deleteDialogCard = editCard
                }
            }
        }
    }

    val deleteCard = deleteDialogCard
    if (deleteCard != null) {
        AlertDialog(
            onDismissRequest = { deleteDialogCard = null },
            title = { Text("Delete card") },
            text = { Text("Are you sure you want to delete \"${deleteCard.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    cardViewModel.deleteCard(deleteCard.id)
                    deleteDialogCard = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogCard = null }) { Text("Cancel") }
            }
        )
    }

    val removeId = removeContactId
    if (removeId != null) {
        AlertDialog(
            onDismissRequest = { removeContactId = null },
            title = { Text("Remove contact") },
            text = { Text("Remove this Nexus from your contacts?") },
            confirmButton = {
                TextButton(onClick = {
                    receivedCardViewModel.deleteContact(removeId)
                    removeContactId = null
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { removeContactId = null }) { Text("Cancel") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeTabSwitcher(tab: String, onTab: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenPadding)
            .padding(bottom = Dimens.gap)
            .neuInset(cornerRadius = 16.dp)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        HomeTabSegment("Cards", Icons.Default.Link, tab == HomeTab.CARDS, Modifier.weight(1f)) { onTab(HomeTab.CARDS) }
        HomeTabSegment("Nexus", Icons.Default.Badge, tab == HomeTab.NEXUS, Modifier.weight(1f)) { onTab(HomeTab.NEXUS) }
        HomeTabSegment("Passes", Icons.Default.ConfirmationNumber, tab == HomeTab.PASSES, Modifier.weight(1f)) { onTab(HomeTab.PASSES) }
    }
}

@Composable
private fun HomeTabSegment(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) Modifier.neuRaised(cornerRadius = 12.dp, elevation = 6.dp, surfaceColor = Color(0xFF1E1E1E))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) NexusTextPrimary else Color(0xFF6F6F6F),
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) NexusTextPrimary else Color(0xFF6F6F6F)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CardsTabContent(
    walletCards: List<PersonalCard>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCreate: () -> Unit,
    onTapCard: (PersonalCard) -> Unit,
    onHoldCard: (PersonalCard) -> Unit,
    onQrCard: (PersonalCard) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Dimens.screenPadding, end = Dimens.screenPadding, bottom = Dimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.gap)
    ) {
        item(key = "search") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuInset(cornerRadius = Dimens.controlRadius)
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    textStyle = TextStyle(color = Color(0xFFD4D4D4), fontSize = 13.sp),
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
                                tint = Color(0xFF4A4A4A),
                                modifier = Modifier.size(18.dp)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text("Search cards", color = Color(0xFF7D7D7D), fontSize = 13.sp)
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }
        }

        item(key = "create") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuRaised(cornerRadius = Dimens.cardRadius)
                    .clickable(onClick = onCreate)
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .neuInset(cornerRadius = 21.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = NexusTeal, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("Create a card", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NexusTextPrimary)
                    Text("A link, a file or custom data to send", fontSize = 12.sp, color = NexusTextSecondary)
                }
            }
        }

        item(key = "saved_header") {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "SAVED CARDS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.4.sp,
                    color = NexusTextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${walletCards.size} cards",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF444444)
                )
            }
        }

        if (walletCards.isEmpty()) {
            item(key = "cards_empty") {
                NexusEmptyState(
                    icon = Icons.Default.Link,
                    title = "No cards yet",
                    body = "Create a card to share a link, file or data over NFC.",
                    actionLabel = "Create a card",
                    onAction = onCreate
                )
            }
        } else {
            items(walletCards, key = { it.id }) { card ->
                CardPreview(
                    title = card.title,
                    subtitle = "",
                    cardShape = card.cardShape,
                    storedColor = card.color,
                    imageUri = card.imageUrl,
                    tag = card.cardType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                    onQrClick = { onQrCard(card) },
                    modifier = Modifier.combinedClickable(
                        onClick = { onTapCard(card) },
                        onLongClick = { onHoldCard(card) }
                    )
                )
            }
            item(key = "cards_hint") {
                Text(
                    text = "Tap a card to share · hold to edit",
                    fontSize = 11.5.sp,
                    color = Color(0xFF5A5A5A),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NexusTabContent(
    myCard: PersonalCard?,
    myCardData: BusinessCardData?,
    contacts: List<com.kryptoxotis.nexus.data.local.ReceivedCardEntity>,
    onEmulateMyNexus: () -> Unit,
    onEditMyNexus: () -> Unit,
    onMyNexusQr: () -> Unit,
    onCreateMyNexus: () -> Unit,
    onOpenContact: (String) -> Unit,
    onHoldContact: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Dimens.screenPadding, end = Dimens.screenPadding, bottom = Dimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.gap)
    ) {
        item(key = "my_nexus") {
            if (myCard != null) {
                CardPreview(
                    title = myCardData?.name?.ifBlank { myCard.title } ?: myCard.title,
                    subtitle = myCardData?.subtitle() ?: "",
                    cardShape = "card",
                    storedColor = myCard.color,
                    tag = "My Nexus",
                    glow = true,
                    onQrClick = onMyNexusQr,
                    modifier = Modifier.combinedClickable(
                        onClick = onEmulateMyNexus,
                        onLongClick = onEditMyNexus
                    )
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neuRaised(cornerRadius = Dimens.cardRadius)
                        .clickable(onClick = onCreateMyNexus)
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .neuInset(cornerRadius = 21.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = NexusTeal, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Create My Nexus", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NexusTextPrimary)
                        Text("Your contact card to share via NFC", fontSize = 12.sp, color = NexusTextSecondary)
                    }
                }
            }
        }

        item(key = "their_header") {
            Text(
                text = "THEIR NEXUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp,
                color = NexusTextSecondary
            )
        }

        if (contacts.isEmpty()) {
            item(key = "contacts_empty") {
                NexusEmptyState(
                    icon = Icons.Default.People,
                    title = "No contacts yet",
                    body = "Tap another Nexus phone or scan a QR to save their Nexus card here."
                )
            }
        } else {
            items(contacts, key = { it.id }) { contact ->
                val subtitle = listOfNotNull(
                    contact.jobTitle.ifBlank { null },
                    contact.company.ifBlank { null }
                ).joinToString(" at ")
                CardPreview(
                    title = contact.name.ifBlank { "Unknown" },
                    subtitle = subtitle,
                    cardShape = "card",
                    storedColor = null,
                    tag = "Nexus",
                    variant = CardVariant.COMPACT,
                    modifier = Modifier.combinedClickable(
                        onClick = { onOpenContact(contact.id) },
                        onLongClick = { onHoldContact(contact.id) }
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PassesTabContent(
    passes: List<com.kryptoxotis.nexus.domain.model.BusinessPass>,
    isBusinessAccount: Boolean,
    onEnroll: () -> Unit,
    onBusinessTools: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Dimens.screenPadding, end = Dimens.screenPadding, bottom = Dimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.gap)
    ) {
        if (passes.isEmpty()) {
            item(key = "passes_empty") {
                NexusEmptyState(
                    icon = Icons.Default.Badge,
                    title = "No passes yet",
                    body = "Enroll in an organization to get your first pass.",
                    actionLabel = "Enroll",
                    onAction = onEnroll
                )
            }
        } else {
            items(passes, key = { it.id }) { pass ->
                val storedColor = when (pass.status) {
                    PassStatus.ACTIVE -> "#0A7968"
                    PassStatus.SUSPENDED -> "#F95B1A"
                    PassStatus.REVOKED -> "#FF1744"
                    PassStatus.EXPIRED -> "#0A7968:dark"
                }
                val subtitle = buildString {
                    append(pass.status.name.lowercase().replaceFirstChar { it.uppercase() })
                    if (pass.expiresAt != null) append(" · expires ${pass.expiresAt}")
                }
                CardPreview(
                    title = pass.organizationName ?: "Organization",
                    subtitle = subtitle,
                    cardShape = "card",
                    storedColor = storedColor,
                    tag = "Pass"
                )
            }
        }

        if (isBusinessAccount) {
            item(key = "business_tools") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neuRaised(cornerRadius = Dimens.cardRadius)
                        .clickable(onClick = onBusinessTools)
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .neuInset(cornerRadius = 21.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = NexusTeal, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Business tools", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = NexusTextPrimary)
                        Text("Manage your organization and passes", fontSize = 12.sp, color = NexusTextSecondary)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SheetActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    labelColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E1E))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = labelColor)
    }
}

/** Builds the quick-edit tile set from the My Nexus data. */
@Composable
internal fun rememberNexusQuickEditFields(data: BusinessCardData): List<QuickEditField> = remember(data) {
    listOf(
        QuickEditField("phone", "Phone", data.phone, Color(0xFF0A7968), materialIcon = Icons.Default.Phone),
        QuickEditField("email", "Email", data.email, Color(0xFF0A7968), materialIcon = Icons.Default.Email),
        QuickEditField("website", "Website", data.website, Color(0xFF0A7968), materialIcon = Icons.Default.Language),
        QuickEditField("linkedin", "LinkedIn", data.linkedin, Color(0xFF0A66C2), drawableRes = R.drawable.ic_social_linkedin),
        QuickEditField("instagram", "Instagram", data.instagram, Color(0xFFD62976), drawableRes = R.drawable.ic_social_instagram, gradientIcon = true),
        QuickEditField("twitter", "X", data.twitter, Color(0xFFEFEFEF), drawableRes = R.drawable.ic_social_x),
        QuickEditField("github", "GitHub", data.github, Color(0xFFEFEFEF), drawableRes = R.drawable.ic_social_github),
        QuickEditField("facebook", "Facebook", data.facebook, Color(0xFF1877F2), drawableRes = R.drawable.ic_social_facebook),
        QuickEditField("youtube", "YouTube", data.youtube, Color(0xFFFF0000), drawableRes = R.drawable.ic_social_youtube, gradientIcon = true),
        QuickEditField("tiktok", "TikTok", data.tiktok, Color(0xFFEE1D52), drawableRes = R.drawable.ic_social_tiktok),
        QuickEditField("discord", "Discord", data.discord, Color(0xFF5865F2), drawableRes = R.drawable.ic_social_discord, gradientIcon = true),
        QuickEditField("twitch", "Twitch", data.twitch, Color(0xFF9146FF), drawableRes = R.drawable.ic_social_twitch, gradientIcon = true),
        QuickEditField("whatsapp", "WhatsApp", data.whatsapp, Color(0xFF25D366), drawableRes = R.drawable.ic_social_whatsapp)
    )
}
