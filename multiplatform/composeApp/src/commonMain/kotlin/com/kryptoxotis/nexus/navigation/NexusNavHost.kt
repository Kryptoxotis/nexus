package com.kryptoxotis.nexus.navigation

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
import com.kryptoxotis.nexus.presentation.cards.AddCardScreen
import com.kryptoxotis.nexus.presentation.cards.CardDetailScreen
import com.kryptoxotis.nexus.presentation.cards.CardWalletScreen
import com.kryptoxotis.nexus.presentation.cards.ContactDetailScreen
import com.kryptoxotis.nexus.presentation.cards.ContactsScreen
import com.kryptoxotis.nexus.presentation.cards.EditCardScreen
import com.kryptoxotis.nexus.presentation.cards.PersonalCardViewModel
import com.kryptoxotis.nexus.presentation.cards.ReceivedCardViewModel
import com.kryptoxotis.nexus.presentation.cards.ScanCardScreen
import com.kryptoxotis.nexus.presentation.cards.SharedLinkScreen
import com.kryptoxotis.nexus.presentation.profile.AccountSwitcherScreen
import com.kryptoxotis.nexus.presentation.profile.ProfileSetupScreen
import kotlinx.coroutines.launch

@Composable
fun NexusNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    cardViewModel: PersonalCardViewModel,
    receivedCardViewModel: ReceivedCardViewModel,
    businessViewModel: BusinessViewModel,
    adminViewModel: AdminViewModel,
    onPostLoginSync: suspend () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onSignInClick = onSignInClick,
                onSignedIn = {
                    val state = authViewModel.authState.value
                    coroutineScope.launch { onPostLoginSync() }
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
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Access denied",
                    modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Not Authorized", style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your account is not authorized to use this app. Contact an administrator to request access.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = {
                    authViewModel.resetError()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }) { Text("Try Again") }
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
                    cardId = cardId, viewModel = cardViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable("scan_card") {
            ScanCardScreen(
                receivedCardViewModel = receivedCardViewModel,
                personalCardViewModel = cardViewModel,
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
                    contactId = contactId, viewModel = receivedCardViewModel,
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
                    cardId = cardId, viewModel = cardViewModel,
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
            SharedLinkScreen(
                url = "",
                onSave = {
                    navController.navigate("card_wallet") { popUpTo(0) { inclusive = true } }
                },
                onDiscard = { navController.popBackStack() }
            )
        }
    }

    // Global auth state observer — navigate to login/not_allowed when state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.NotAuthenticated -> {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute != null && currentRoute != "login") {
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            }
            is AuthState.NotAllowed -> {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute != null && currentRoute != "not_allowed") {
                    navController.navigate("not_allowed") { popUpTo(0) { inclusive = true } }
                }
            }
            else -> {}
        }
    }
}
