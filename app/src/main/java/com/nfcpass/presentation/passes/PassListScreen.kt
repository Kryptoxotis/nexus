package com.nfcpass.presentation.passes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nfcpass.presentation.auth.AuthState
import com.nfcpass.presentation.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassListScreen(
    viewModel: PassViewModel,
    authViewModel: AuthViewModel?,
    onNavigateToAddPass: () -> Unit,
    onNavigateToAccounts: () -> Unit
) {
    val passes by viewModel.passes.collectAsState()
    val activePass by viewModel.activePass.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val authState = authViewModel?.authState?.collectAsState()?.value
    val profile = authViewModel?.profile?.collectAsState()?.value

    var showDeleteDialog by remember { mutableStateOf(false) }
    var passToDelete by remember { mutableStateOf<String?>(null) }

    val isAuthenticated = authState is AuthState.Authenticated
    val roleBadge = when ((authState as? AuthState.Authenticated)?.role) {
        "business" -> "Business"
        "personal" -> "Personal"
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Nexus")
                        if (roleBadge != null) {
                            Text(
                                text = roleBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (!isAuthenticated) {
                            Text(
                                text = "Offline Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (isAuthenticated) {
                        IconButton(onClick = onNavigateToAccounts) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Accounts")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddPass) {
                Icon(Icons.Default.Add, contentDescription = "Add Pass")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Active pass section
            if (activePass != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTIVE PASS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activePass!!.passName,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = activePass!!.organization,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "ID: ${activePass!!.passId}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ready to tap NFC reader",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No active pass",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Activate a pass to use NFC",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Pass list
            if (passes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No passes yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Tap + to add your first pass",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(passes) { pass ->
                        PassItem(
                            pass = pass,
                            isActive = pass.id == activePass?.id,
                            onActivate = { viewModel.activatePass(pass.id) },
                            onDeactivate = { viewModel.deactivatePass(pass.id) },
                            onDelete = {
                                passToDelete = pass.id
                                showDeleteDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog && passToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Pass") },
                text = { Text("Are you sure you want to delete this pass?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletePass(passToDelete!!)
                            showDeleteDialog = false
                            passToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Show success message
        if (uiState is PassUiState.Success) {
            LaunchedEffect(uiState) {
                viewModel.resetState()
            }
        }
    }
}

@Composable
private fun PassItem(
    pass: com.nfcpass.domain.model.Pass,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pass.passName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = pass.organization,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "ID: ${pass.passId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!pass.link.isNullOrBlank()) {
                        Text(
                            text = "Link: ${pass.link}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = if (isActive) onDeactivate else onActivate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isActive) "Deactivate" else "Activate")
            }
        }
    }
}
