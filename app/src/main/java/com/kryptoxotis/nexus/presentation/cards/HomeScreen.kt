package com.kryptoxotis.nexus.presentation.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.kryptoxotis.nexus.presentation.theme.NexusAppearance
import com.kryptoxotis.nexus.presentation.theme.NexusEmptyState
import com.kryptoxotis.nexus.presentation.theme.NexusScaffold
import com.kryptoxotis.nexus.presentation.theme.NexusSurface
import com.kryptoxotis.nexus.presentation.theme.NexusControlText
import com.kryptoxotis.nexus.presentation.theme.NexusDotInactive
import com.kryptoxotis.nexus.presentation.theme.NexusMutedText
import com.kryptoxotis.nexus.presentation.theme.NexusPillSurface
import com.kryptoxotis.nexus.presentation.theme.NexusTeal
import com.kryptoxotis.nexus.presentation.theme.NexusTextTertiary
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

    val tabs = remember { listOf(HomeTab.CARDS, HomeTab.NEXUS, HomeTab.PASSES) }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = tabs.indexOf(initialTab).coerceAtLeast(0)
    ) { tabs.size }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
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
        if (!NexusAppearance.nfcSharing) {
            // NFC sharing is off — QR becomes the share path
            qrCard = card
            return
        }
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
                // Swiping is the navigation — just dots to stay oriented
                HomePagerDots(
                    count = tabs.size,
                    current = pagerState.currentPage,
                    onDot = { i -> scope.launch { pagerState.animateScrollToPage(i) } }
                )

                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (tabs[page]) {
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
                            totalCards = cards.count { it.cardType != CardType.BUSINESS_CARD },
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
        }

        val emCard = emulatingCard
        if (overlayVisible && emCard != null) {
            val isNexusCard = emulatingNexus
            EmulateOverlay(
                title = if (isNexusCard) (myCardData?.name ?: emCard.title) else emCard.title,
                subtitle = if (isNexusCard) (myCardData?.subtitle() ?: "") else "",
                storedColor = emCard.color,
                // Same tag as the card shows at rest — activated and idle look identical
                tag = if (isNexusCard) "My Nexus"
                      else emCard.cardType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
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
                    labelColor = NexusControlText
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

// Section accents: Cards teal, Nexus purple (the brand favorite), Passes teal
private val SectionAccents get() = listOf(NexusTeal, com.kryptoxotis.nexus.presentation.theme.NexusPurple, NexusTeal)

@Composable
private fun HomePagerDots(count: Int, current: Int, onDot: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.gapSmall),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = if (i == current) 18.dp else 7.dp, height = 7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (i == current) SectionAccents.getOrElse(current) { NexusTeal }
                        else NexusDotInactive
                    )
                    .clickable { onDot(i) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CardsTabContent(
    walletCards: List<PersonalCard>,
    totalCards: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCreate: () -> Unit,
    onTapCard: (PersonalCard) -> Unit,
    onHoldCard: (PersonalCard) -> Unit,
    onQrCard: (PersonalCard) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Dimens.screenPadding, end = Dimens.screenPadding, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.gap)
    ) {
        // Search only earns its place once there are enough cards to lose one
        if (totalCards > 6) item(key = "search") {
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
                    textStyle = TextStyle(color = NexusControlText, fontSize = 13.sp),
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
                                tint = NexusMutedText,
                                modifier = Modifier.size(18.dp)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text("Search cards", color = NexusTextTertiary, fontSize = 13.sp)
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
                Text("Create a card", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NexusTextPrimary)
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
        } else if (NexusAppearance.deckView) {
            item(key = "deck") {
                CardDeck(
                    cards = walletCards,
                    onTapFront = onTapCard,
                    onHoldFront = onHoldCard,
                    onQrFront = onQrCard
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
                    topRightIcon = Icons.Default.Edit,
                    onTopRightClick = { onHoldCard(card) },
                    onQrClick = { onQrCard(card) },
                    modifier = Modifier.combinedClickable(
                        onClick = { onTapCard(card) },
                        onLongClick = { onHoldCard(card) }
                    )
                )
            }
        }
    }
}

/**
 * Deck view: cards stacked with the front card on top. Tap a back card (or
 * its dot) to bring it forward; tap the front card to share it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CardDeck(
    cards: List<PersonalCard>,
    onTapFront: (PersonalCard) -> Unit,
    onHoldFront: (PersonalCard) -> Unit,
    onQrFront: (PersonalCard) -> Unit
) {
    var frontState by remember(cards.map { it.id }) { mutableStateOf(0) }
    val n = cards.size
    val front = frontState.coerceIn(0, n - 1)
    val stackStep = 26

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((230 + (n - 1).coerceAtMost(4) * stackStep).dp)
                // Swipe left/right to cycle through the deck
                .pointerInput(n) {
                    var total = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { total = 0f },
                        onDragEnd = {
                            if (total < -70f) frontState = (frontState + 1) % n
                            else if (total > 70f) frontState = (frontState - 1 + n) % n
                        }
                    ) { _, dragAmount -> total += dragAmount }
                }
        ) {
            // Draw back cards first, the front card last (on top)
            val order = (0 until n).filter { it != front } + front
            order.forEach { cardIdx ->
                val card = cards[cardIdx]
                val isFront = cardIdx == front
                // Depth 0 = front; back cards peek above the front card
                val depth = if (isFront) 0 else (
                    order.indexOf(cardIdx).let { drawPos -> (n - 1 - drawPos).coerceAtMost(4) }
                )
                CardPreview(
                    title = card.title,
                    subtitle = "",
                    cardShape = "card",
                    storedColor = card.color,
                    imageUri = card.imageUrl,
                    tag = card.cardType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                    glow = isFront,
                    // Same edit glyph on every card in the stack; only the front one is live
                    topRightIcon = Icons.Default.Edit,
                    onTopRightClick = if (isFront) ({ onHoldFront(card) }) else null,
                    onQrClick = if (isFront) ({ onQrFront(card) }) else null,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = (((n - 1).coerceAtMost(4) - depth) * stackStep).dp)
                        .graphicsLayer {
                            val s = 1f - depth * 0.04f
                            scaleX = s
                            scaleY = s
                            alpha = 1f - depth * 0.12f
                        }
                        .combinedClickable(
                            onClick = { if (isFront) onTapFront(card) else frontState = cardIdx },
                            onLongClick = { if (isFront) onHoldFront(card) }
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.gapSmall))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(n) { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = if (i == front) 18.dp else 7.dp, height = 7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (i == front) NexusTeal else NexusDotInactive)
                        .clickable { frontState = i }
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
        contentPadding = PaddingValues(start = Dimens.screenPadding, end = Dimens.screenPadding, bottom = 92.dp),
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
                    topRightIcon = Icons.Default.Edit,
                    onTopRightClick = onEditMyNexus,
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
        contentPadding = PaddingValues(start = Dimens.screenPadding, end = Dimens.screenPadding, bottom = 92.dp),
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
            .background(NexusPillSurface)
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
