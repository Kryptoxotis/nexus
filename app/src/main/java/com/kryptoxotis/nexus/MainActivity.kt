package com.kryptoxotis.nexus

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.cardemulation.CardEmulation
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kryptoxotis.nexus.data.local.NexusDatabase
import com.kryptoxotis.nexus.data.remote.AuthManager
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.repository.BusinessPassRepository
import com.kryptoxotis.nexus.data.repository.FileRepository
import com.kryptoxotis.nexus.data.repository.OrganizationRepository
import com.kryptoxotis.nexus.data.repository.PersonalCardRepository
import com.kryptoxotis.nexus.data.repository.ReceivedCardRepository
import com.kryptoxotis.nexus.domain.model.AccountType
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.presentation.admin.AdminDashboardScreen
import com.kryptoxotis.nexus.presentation.admin.AdminViewModel
import com.kryptoxotis.nexus.presentation.admin.BusinessRequestsScreen
import com.kryptoxotis.nexus.presentation.admin.OrgManagementScreen
import com.kryptoxotis.nexus.presentation.admin.UserManagementScreen
import com.kryptoxotis.nexus.presentation.auth.AuthState
import com.kryptoxotis.nexus.presentation.auth.AuthViewModel
import com.kryptoxotis.nexus.presentation.auth.LoginScreen
import com.kryptoxotis.nexus.presentation.business.BusinessDashboardScreen
import com.kryptoxotis.nexus.presentation.business.BusinessPassListScreen
import com.kryptoxotis.nexus.presentation.business.BusinessViewModel
import com.kryptoxotis.nexus.presentation.business.CreateOrgScreen
import com.kryptoxotis.nexus.presentation.business.EnrollmentScreen
import com.kryptoxotis.nexus.presentation.business.IssuePassScreen
import com.kryptoxotis.nexus.presentation.business.MemberListScreen
import com.kryptoxotis.nexus.presentation.business.OrgSettingsScreen
import com.kryptoxotis.nexus.service.NdefCache
import com.kryptoxotis.nexus.service.WidgetBridge
import com.kryptoxotis.nexus.widget.NexusCardWidget
import androidx.glance.appwidget.updateAll
import com.kryptoxotis.nexus.presentation.cards.AddCardScreen
import com.kryptoxotis.nexus.presentation.cards.CardDetailScreen
import com.kryptoxotis.nexus.presentation.cards.CardWalletScreen
import com.kryptoxotis.nexus.presentation.cards.ContactDetailScreen
import com.kryptoxotis.nexus.presentation.cards.ContactsScreen
import com.kryptoxotis.nexus.presentation.cards.EditCardScreen
import com.kryptoxotis.nexus.presentation.cards.ScanCardScreen
import com.kryptoxotis.nexus.presentation.cards.PersonalCardViewModel
import com.kryptoxotis.nexus.presentation.cards.SharedLinkScreen
import com.kryptoxotis.nexus.presentation.cards.ReceivedCardViewModel
import com.kryptoxotis.nexus.presentation.profile.AccountSwitcherScreen
import com.kryptoxotis.nexus.presentation.profile.ProfileSetupScreen
import com.kryptoxotis.nexus.presentation.theme.NexusTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val _sharedUrl = MutableStateFlow<String?>(null)

    private lateinit var cardRepository: PersonalCardRepository
    private lateinit var receivedCardRepository: ReceivedCardRepository
    private lateinit var businessPassRepository: BusinessPassRepository
    private lateinit var orgRepository: OrganizationRepository
    private lateinit var fileRepository: FileRepository
    private lateinit var cardViewModel: PersonalCardViewModel
    private lateinit var receivedCardViewModel: ReceivedCardViewModel
    private lateinit var businessViewModel: BusinessViewModel
    private lateinit var adminViewModel: AdminViewModel
    private lateinit var authManager: AuthManager
    private lateinit var authViewModel: AuthViewModel
    private var isNfcSupported: Boolean = false
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNfcSupport()
        initSupabase()

        val database = NexusDatabase.getDatabase(applicationContext)
        cardRepository = PersonalCardRepository(database.personalCardDao())
        receivedCardRepository = ReceivedCardRepository(database.receivedCardDao())
        businessPassRepository = BusinessPassRepository(database.businessPassDao())
        orgRepository = OrganizationRepository()
        fileRepository = FileRepository()

        authManager = AuthManager(applicationContext)
        authViewModel = AuthViewModel(authManager)

        cardViewModel = PersonalCardViewModel(cardRepository, fileRepository)
        receivedCardViewModel = ReceivedCardViewModel(receivedCardRepository)
        businessViewModel = BusinessViewModel(businessPassRepository, orgRepository)
        adminViewModel = AdminViewModel()

        authViewModel.checkSession()

        // Handle shared URL from ShareReceiverActivity trampoline
        handleShareIntent(intent)

        // Sync data when app returns to foreground
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                try {
                    val userId = authViewModel.getCurrentUserId()
                    if (userId != null) {
                        WidgetBridge.writeUserId(applicationContext, userId)
                        cardRepository.refreshUserId()
                        receivedCardRepository.refreshUserId()
                        cardRepository.syncFromSupabase()
                        businessPassRepository.syncFromSupabase()
                        receivedCardRepository.syncFromSupabase()
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Sync failed", e)
                }
            }
        }

        // Pre-cache NDEF bytes whenever the active card changes.
        // The HCE service reads from SharedPreferences (~1ms) instead of Room.
        // Skip if a shared URL is active — don't overwrite it.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cardViewModel.activeCard.collect { card ->
                    if (_sharedUrl.value != null) return@collect
                    try {
                        NdefCache.write(applicationContext, card)
                        NexusCardWidget().updateAll(applicationContext)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "NDEF cache write failed", e)
                    }
                }
            }
        }

        setContent {
            NexusTheme {
                val navController = rememberNavController()
                val authState by authViewModel.authState.collectAsState()
                val coroutineScope = rememberCoroutineScope()

                // Compute once — LaunchedEffect handles subsequent navigation
                val startDestination = remember { "login" }

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("login") {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onSignedIn = {
                                val state = authViewModel.authState.value
                                coroutineScope.launch {
                                    try {
                                        cardRepository.refreshUserId()
                                        receivedCardRepository.refreshUserId()
                                        val userId = authViewModel.getCurrentUserId()
                                        if (userId != null) {
                                            WidgetBridge.writeUserId(this@MainActivity, userId)
                                            cardRepository.migrateLocalUserCards(userId)
                                            cardRepository.syncFromSupabase()
                                            businessPassRepository.syncFromSupabase()
                                            receivedCardRepository.syncFromSupabase()
                                            NexusCardWidget().updateAll(this@MainActivity)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "Post-login sync failed", e)
                                    }
                                }
                                when (state) {
                                    is AuthState.Authenticated -> {
                                        val dest = when (state.accountType) {
                                            AccountType.BUSINESS -> "business_dashboard"
                                            AccountType.ADMIN -> "admin_dashboard"
                                            else -> "card_wallet"
                                        }
                                        navController.navigate(dest) {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        )
                    }

                    composable("not_allowed") {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Access denied",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Not Authorized",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your account is not authorized to use this app. Contact an administrator to request access.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    authViewModel.resetError()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            ) {
                                Text("Try Again")
                            }
                        }
                    }

                    composable("profile_setup") {
                        ProfileSetupScreen(
                            authViewModel = authViewModel,
                            onSetupComplete = {
                                navController.navigate("card_wallet") {
                                    popUpTo("profile_setup") { inclusive = true }
                                }
                            }
                        )
                    }

                    // ===== Individual screens =====
                    composable("card_wallet") {
                        CardWalletScreen(
                            viewModel = cardViewModel,
                            authViewModel = authViewModel,
                            onNavigateToAddCard = { navController.navigate("add_card") },
                            onNavigateToCardDetail = { id -> navController.navigate("card_detail/$id") },
                            onNavigateToEditCard = { id -> navController.navigate("edit_card/$id") },
                            onNavigateToAccounts = { navController.navigate("accounts") },
                            onNavigateToBusinessPasses = { navController.navigate("business_passes") },
                            onNavigateToContacts = { navController.navigate("contacts") }
                        )
                    }

                    composable(
                        "add_card?myCard={myCard}",
                        arguments = listOf(navArgument("myCard") {
                            type = NavType.BoolType
                            defaultValue = false
                        })
                    ) { backStackEntry ->
                        val myCardOnly = backStackEntry.arguments?.getBoolean("myCard") ?: false
                        val myOrg by businessViewModel.myOrganization.collectAsState()
                        AddCardScreen(
                            viewModel = cardViewModel,
                            organizationId = myOrg?.id,
                            myCardOnly = myCardOnly,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("card_detail/{cardId}") { backStackEntry ->
                        val cardId = backStackEntry.arguments?.getString("cardId") ?: ""
                        if (cardId.isNotEmpty()) {
                            CardDetailScreen(
                                cardId = cardId,
                                viewModel = cardViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        } else {
                            // Invalid card ID — go back
                            LaunchedEffect(Unit) { navController.popBackStack() }
                        }
                    }

                    composable("scan_card") {
                        ScanCardScreen(
                            receivedCardViewModel = receivedCardViewModel,
                            personalCardViewModel = cardViewModel,
                            businessViewModel = businessViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("contacts") {
                        ContactsScreen(
                            viewModel = receivedCardViewModel,
                            personalCardViewModel = cardViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDetail = { id -> navController.navigate("contact_detail/$id") },
                            onNavigateToScanCard = { navController.navigate("scan_card") },
                            onNavigateToCreateMyCard = { navController.navigate("add_card?myCard=true") },
                            onNavigateToEditCard = { id -> navController.navigate("edit_card/$id") },
                            onNavigateToCardDetail = { id -> navController.navigate("card_detail/$id") }
                        )
                    }

                    composable("contact_detail/{contactId}") { backStackEntry ->
                        val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                        if (contactId.isNotEmpty()) {
                            ContactDetailScreen(
                                contactId = contactId,
                                viewModel = receivedCardViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        } else {
                            LaunchedEffect(Unit) { navController.popBackStack() }
                        }
                    }

                    composable("edit_card/{cardId}") { backStackEntry ->
                        val cardId = backStackEntry.arguments?.getString("cardId") ?: ""
                        if (cardId.isNotEmpty()) {
                            EditCardScreen(
                                cardId = cardId,
                                viewModel = cardViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        } else {
                            LaunchedEffect(Unit) { navController.popBackStack() }
                        }
                    }

                    composable("business_passes") {
                        BusinessPassListScreen(
                            viewModel = businessViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToEnrollment = { navController.navigate("enrollment") }
                        )
                    }

                    composable("enrollment") {
                        EnrollmentScreen(
                            viewModel = businessViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ===== Business screens =====
                    composable("business_dashboard") {
                        BusinessDashboardScreen(
                            viewModel = businessViewModel,
                            authViewModel = authViewModel,
                            onNavigateToMembers = { navController.navigate("member_list") },
                            onNavigateToOrgSettings = { navController.navigate("org_settings") },
                            onNavigateToIssuePasses = { navController.navigate("issue_pass") },
                            onNavigateToCardWallet = { navController.navigate("card_wallet") },
                            onNavigateToAccounts = { navController.navigate("accounts") }
                        )
                    }

                    composable("create_org") {
                        CreateOrgScreen(
                            viewModel = businessViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("member_list") {
                        MemberListScreen(
                            viewModel = businessViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("org_settings") {
                        OrgSettingsScreen(
                            viewModel = businessViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("issue_pass") {
                        IssuePassScreen(
                            viewModel = businessViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ===== Admin screens =====
                    composable("admin_dashboard") {
                        AdminDashboardScreen(
                            viewModel = adminViewModel,
                            authViewModel = authViewModel,
                            onNavigateToRequests = { navController.navigate("admin_requests") },
                            onNavigateToUsers = { navController.navigate("admin_users") },
                            onNavigateToOrgs = { navController.navigate("admin_orgs") },
                            onNavigateToCardWallet = { navController.navigate("card_wallet") },
                            onNavigateToAccounts = { navController.navigate("accounts") }
                        )
                    }

                    composable("admin_requests") {
                        BusinessRequestsScreen(
                            viewModel = adminViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("admin_users") {
                        UserManagementScreen(
                            viewModel = adminViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("admin_orgs") {
                        OrgManagementScreen(
                            viewModel = adminViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("accounts") {
                        AccountSwitcherScreen(
                            authViewModel = authViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("share_link") {
                        val url = _sharedUrl.collectAsState().value ?: ""
                        SharedLinkScreen(
                            url = url,
                            onSave = {
                                cardViewModel.addCard(
                                    cardType = CardType.LINK,
                                    title = url,
                                    content = url
                                )
                                _sharedUrl.value = null
                                // Restore active card in NdefCache
                                NdefCache.write(this@MainActivity, cardViewModel.activeCard.value)
                                navController.navigate("card_wallet") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onDiscard = {
                                _sharedUrl.value = null
                                // Restore active card in NdefCache
                                NdefCache.write(this@MainActivity, cardViewModel.activeCard.value)
                                navController.popBackStack()
                            }
                        )
                    }
                }

                // Navigate to share screen when a URL is shared
                val sharedUrl by _sharedUrl.collectAsState()
                LaunchedEffect(sharedUrl) {
                    if (sharedUrl != null) {
                        navController.navigate("share_link")
                    }
                }

                LaunchedEffect(authState) {
                    if (authState is AuthState.NotAuthenticated) {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != null && currentRoute != "login") {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    } else if (authState is AuthState.NotAllowed) {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != null && currentRoute != "not_allowed") {
                            navController.navigate("not_allowed") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }

    @Suppress("NewApi")
    override fun onResume() {
        super.onResume()
        // Check for pending shared URL (from ShareReceiverActivity via SharedPreferences)
        val prefs = getSharedPreferences("nexus_share", MODE_PRIVATE)
        val pendingUrl = prefs.getString("pending_url", null)
        if (pendingUrl != null) {
            prefs.edit().remove("pending_url").apply()
            NdefCache.writeUri(this, pendingUrl) // ensure cache is fresh
            _sharedUrl.value = pendingUrl
        }
        if (isNfcSupported) {
            try {
                // Clean up any stale reader mode from ScanCardScreen
                nfcAdapter?.disableReaderMode(this)
            } catch (_: Exception) {}
            try {
                val cardEmulation = CardEmulation.getInstance(nfcAdapter!!)
                cardEmulation.setPreferredService(
                    this,
                    ComponentName(this, com.kryptoxotis.nexus.service.NFCPassService::class.java)
                )
            } catch (_: Exception) {}
            if (Build.VERSION.SDK_INT >= 35) {
                try {
                    // Disable polling, keep HCE listen mode — acts like a passive NFC card
                    nfcAdapter?.setDiscoveryTechnology(
                        this,
                        NfcAdapter.FLAG_READER_DISABLE,
                        NfcAdapter.FLAG_LISTEN_KEEP
                    )
                } catch (_: Exception) {}
            } else {
                try {
                    // Fallback: intercept tag reads silently to suppress dialog
                    val pendingIntent = PendingIntent.getActivity(
                        this, 0,
                        Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        PendingIntent.FLAG_MUTABLE
                    )
                    nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
                } catch (_: Exception) {}
            }
        }
    }

    @Suppress("NewApi")
    override fun onPause() {
        super.onPause()
        if (isNfcSupported) {
            if (Build.VERSION.SDK_INT >= 35) {
                try {
                    nfcAdapter?.resetDiscoveryTechnology(this)
                } catch (_: Exception) {}
            } else {
                try {
                    nfcAdapter?.disableForegroundDispatch(this)
                } catch (_: Exception) {}
            }
            try {
                val cardEmulation = CardEmulation.getInstance(nfcAdapter!!)
                cardEmulation.unsetPreferredService(this)
            } catch (_: Exception) {}
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle shared URL from trampoline
        handleShareIntent(intent)
        // NFC tag discovered — ignore it to release RF field
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag != null) {
            try {
                nfcAdapter?.ignore(tag, 500, null, null)
            } catch (_: Exception) {}
        }
    }

    private fun handleShareIntent(intent: Intent?) {
        // Try intent extra first, then check SharedPreferences fallback
        var url = intent?.getStringExtra("shared_url")
        if (url == null) {
            val prefs = getSharedPreferences("nexus_share", MODE_PRIVATE)
            url = prefs.getString("pending_url", null)
            if (url != null) prefs.edit().remove("pending_url").apply()
        }
        if (url != null) {
            _sharedUrl.value = url
        }
    }

    private fun initSupabase() {
        SupabaseClientProvider.supabaseUrl = BuildConfig.SUPABASE_URL
        SupabaseClientProvider.supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY
        AuthManager.googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
    }

    private fun checkNfcSupport() {
        val adapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter = adapter

        when {
            adapter == null -> {
                isNfcSupported = false
                Toast.makeText(this, "NFC is not supported on this device.", Toast.LENGTH_LONG).show()
            }
            !adapter.isEnabled -> {
                isNfcSupported = false
                Toast.makeText(this, "NFC is disabled. Please enable it in Settings.", Toast.LENGTH_LONG).show()
            }
            else -> {
                isNfcSupported = true
            }
        }
    }
}
