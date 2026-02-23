package com.kryptoxotis.nexus.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.presentation.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    onNavigateToRequests: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToOrgs: () -> Unit,
    onNavigateToCardWallet: () -> Unit,
    onNavigateToAccounts: () -> Unit
) {
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val users by viewModel.users.collectAsState()
    val organizations by viewModel.organizations.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPendingRequests()
        viewModel.loadUsers()
        viewModel.loadOrganizations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Portal")
                        Text(
                            text = "Platform Management",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCardWallet) {
                        Icon(Icons.Default.CreditCard, contentDescription = "Card Wallet")
                    }
                    IconButton(onClick = onNavigateToAccounts) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Accounts")
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
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(
                    title = "Pending",
                    value = "${pendingRequests.size}",
                    icon = Icons.Default.Pending,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Users",
                    value = "${users.size}",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Orgs",
                    value = "${organizations.size}",
                    icon = Icons.Default.Business,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick actions
            Text(
                text = "Management",
                style = MaterialTheme.typography.titleMedium
            )

            AdminAction(
                icon = Icons.Default.Pending,
                title = "Business Requests",
                description = "${pendingRequests.size} pending approval",
                onClick = onNavigateToRequests
            )

            AdminAction(
                icon = Icons.Default.People,
                title = "User Management",
                description = "Search and manage user accounts",
                onClick = onNavigateToUsers
            )

            AdminAction(
                icon = Icons.Default.Business,
                title = "Organizations",
                description = "Manage registered organizations",
                onClick = onNavigateToOrgs
            )
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdminAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
