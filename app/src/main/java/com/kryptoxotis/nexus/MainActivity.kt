package com.kryptoxotis.nexus

import android.content.ComponentName
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.cardemulation.CardEmulation
import android.os.Bundle
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kryptoxotis.nexus.data.local.NexusDatabase
import com.kryptoxotis.nexus.data.remote.AuthManager
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.repository.BusinessPassRepository
import com.kryptoxotis.nexus.data.repository.FileRepository
import com.kryptoxotis.nexus.data.repository.OrganizationRepository
import com.kryptoxotis.nexus.data.repository.PersonalCardRepository
import com.kryptoxotis.nexus.domain.model.AccountType
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
import com.kryptoxotis.nexus.presentation.cards.AddCardScreen
import com.kryptoxotis.nexus.presentation.cards.CardDetailScreen
import com.kryptoxotis.nexus.presentation.cards.CardWalletScreen
import com.kryptoxotis.nexus.presentation.cards.EditCardScreen
import com.kryptoxotis.nexus.presentation.cards.ScanCardScreen
import com.kryptoxotis.nexus.presentation.cards.PersonalCardViewModel
import com.kryptoxotis.nexus.presentation.profile.AccountSwitcherScreen
import com.kryptoxotis.nexus.presentation.profile.ProfileSetupScreen
import com.kryptoxotis.nexus.presentation.theme.NexusTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var cardRepository: PersonalCardRepository
    private lateinit var businessPassRepository: BusinessPassRepository
    private lateinit var orgRepository: OrganizationRepository
    private lateinit var fileRepository: FileRepository
    private lateinit var cardViewModel: PersonalCardViewModel
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
        businessPassRepository = BusinessPassRepository(database.businessPassDao())
        orgRepository = OrganizationRepository()
        fileRepository = FileRepository()

        authManager = AuthManager(applicationContext)
        authViewModel = AuthViewModel(authManager)

        cardViewModel = PersonalCardViewModel(cardRepository, fileRepository)
        businessViewModel = BusinessViewModel(businessPassRepository, orgRepository)
        adminViewModel = AdminViewModel()

        authViewModel.checkSession()

        // Sync data when app returns to foreground
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (authViewModel.getCurrentUserId() != null) {
                    cardRepository.syncFromSupabase()
                    businessPassRepository.syncFromSupabase()
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
                                    val userId = authViewModel.getCurrentUserId()
                                    if (userId != null) {
                                        cardRepository.migrateLocalUserCards(userId)
                                        cardRepository.syncFromSupabase()
                                        businessPassRepository.syncFromSupabase()
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
                            onNavigateToScanCard = { navController.navigate("scan_card") },
                            onNavigateToAccounts = { navController.navigate("accounts") },
                            onNavigateToBusinessPasses = { navController.navigate("business_passes") }
                        )
                    }

                    composable("add_card") {
                        AddCardScreen(
                            viewModel = cardViewModel,
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
                            onNavigateBack = { navController.popBackStack() }
                        )
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
                            onNavigateToCreateOrg = { navController.navigate("create_org") },
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

    override fun onResume() {
        super.onResume()
        // Tell Android to prefer our HCE service for card emulation
        // This biases the NFC controller toward listen mode (card) instead of poll mode (reader)
        if (isNfcSupported) {
            try {
                val cardEmulation = CardEmulation.getInstance(nfcAdapter!!)
                cardEmulation.setPreferredService(
                    this,
                    ComponentName(this, com.kryptoxotis.nexus.service.NFCPassService::class.java)
                )
            } catch (_: Exception) {}
        }
    }

    override fun onPause() {
        super.onPause()
        if (isNfcSupported) {
            try {
                val cardEmulation = CardEmulation.getInstance(nfcAdapter!!)
                cardEmulation.unsetPreferredService(this)
            } catch (_: Exception) {}
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Silently consume any NFC intents that arrive
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
